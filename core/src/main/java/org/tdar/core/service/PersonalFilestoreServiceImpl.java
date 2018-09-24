package org.tdar.core.service;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.ImportFileStatus;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.CurationState;
import org.tdar.core.bean.file.FileComment;
import org.tdar.core.bean.file.Mark;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.dao.DirSummary;
import org.tdar.core.dao.DirSummaryPart;
import org.tdar.core.dao.FileOrder;
import org.tdar.core.dao.FileProcessingDao;
import org.tdar.core.dao.RecentFileSummary;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.exception.FileUploadException;
import org.tdar.core.exception.TdarAuthorizationException;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.DatasetImportService;
import org.tdar.core.service.resource.ErrorHandling;
import org.tdar.db.model.TargetDatabase;
import org.tdar.fileprocessing.workflows.HasDatabaseConverter;
import org.tdar.fileprocessing.workflows.RequiredOptionalPairs;
import org.tdar.fileprocessing.workflows.Workflow;
import org.tdar.fileprocessing.workflows.WorkflowContext;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.personal.BagitPersonalFilestore;
import org.tdar.filestore.personal.PersonalFileType;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.filestore.personal.PersonalFilestoreFile;
import org.tdar.utils.FileStoreFileUtils;
import org.tdar.utils.PersistableUtils;

/**
 * Manages adding and saving files in the @link PersonalFilestore
 * 
 * @author <a href='jim.devos@asu.edu'>Jim Devos</a>, <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Service
public class PersonalFilestoreServiceImpl implements PersonalFilestoreService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private GenericDao genericDao;
    private FileProcessingDao fileProcessingDao;
    private FileAnalyzer analyzer;
    private AuthorizationService authorizationService;
    private ResourceCollectionService resourceCollectionService;
    private TargetDatabase tdarDataImportDatabase;

    private DatasetImportService datasetImportService;

    @Autowired
    public PersonalFilestoreServiceImpl(GenericDao genericDao, FileProcessingDao fileProcessingDao, FileAnalyzer analyzer,
            AuthorizationService authorizationService, ResourceCollectionService resourceCollectionService,
            @Qualifier("target") TargetDatabase tdarDataImportDatabase, DatasetImportService datasetImportService) {
        this.genericDao = genericDao;
        this.fileProcessingDao = fileProcessingDao;
        this.analyzer = analyzer;
        this.authorizationService = authorizationService;
        this.resourceCollectionService = resourceCollectionService;
        this.tdarDataImportDatabase = tdarDataImportDatabase;
        this.datasetImportService = datasetImportService;

    }

    // FIXME: double check that won't leak memory
    private Map<TdarUser, PersonalFilestore> personalFilestoreCache = new WeakHashMap<TdarUser, PersonalFilestore>();

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#createPersonalFilestoreTicket(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional
    public PersonalFilestoreTicket createPersonalFilestoreTicket(TdarUser person) {
        return createPersonalFilestoreTicket(person, PersonalFileType.UPLOAD);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#createPersonalFilestoreTicket(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.filestore.personal.PersonalFileType)
     */
    @Override
    @Transactional
    public PersonalFilestoreTicket createPersonalFilestoreTicket(TdarUser person, PersonalFileType fileType) {
        PersonalFilestoreTicket tfg = new PersonalFilestoreTicket();
        tfg.setSubmitter(person);
        tfg.setPersonalFileType(fileType);
        genericDao.save(tfg);

        // FIXME: it uses the ID as the ticket, but needs to check whether the ticket actually exists
        return tfg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#getPersonalFilestore(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public synchronized PersonalFilestore getPersonalFilestore(TdarUser submitter) {
        PersonalFilestore personalFilestore = personalFilestoreCache.get(submitter);
        if (personalFilestore == null) {
            personalFilestore = new BagitPersonalFilestore();
            personalFilestoreCache.put(submitter, personalFilestore);
        }
        return personalFilestore;
    }

    @Transactional(readOnly = false)
    @Override
    public TdarFile store(PersonalFilestoreTicket ticket, File file, String fileName, BillingAccount account, TdarUser user, TdarDir dir)
            throws FileUploadException {
        PersonalFilestore filestore = getPersonalFilestore(ticket);
        try {
            // if we're not unfiled then require uniqueness
            if (dir == null || !StringUtils.equals(dir.getName(), TdarDir.UNFILED)) {
                List<AbstractFile> listFiles = listFiles(dir, account, null, null, user);
                for (AbstractFile f : listFiles) {
                    if (StringUtils.equalsIgnoreCase(f.getName(), fileName)) {
                        throw new FileAlreadyExistsException(fileName);
                    }
                }

            }
            PersonalFilestoreFile store = filestore.store(ticket, file, fileName);
            TdarFile tdarFile = new TdarFile();
            tdarFile.setInternalName(store.getFile().getName());
            tdarFile.setLocalPath(store.getFile().getPath());
            tdarFile.setFilename(fileName);
            tdarFile.setExtension(FilenameUtils.getExtension(fileName));
            tdarFile.setSize(file.length());
            tdarFile.setDateCreated(new Date());
            if (account != null) {
                tdarFile.setAccount(account);
            }
            tdarFile.setUploader(user);
            if (dir != null) {
                tdarFile.setParent(dir);
            }
            tdarFile.setMd5(store.getMd5());
            tdarFile.setStatus(ImportFileStatus.UPLOADED);
            genericDao.saveOrUpdate(tdarFile);
            return tdarFile;
        } catch (Exception e) {
            throw new FileUploadException("uploadController.could_not_store", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#findPersonalFilestoreTicket(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public PersonalFilestoreTicket findPersonalFilestoreTicket(Long ticketId) {
        return genericDao.find(PersonalFilestoreTicket.class, ticketId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#retrieveAllPersonalFilestoreFiles(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public List<PersonalFilestoreFile> retrieveAllPersonalFilestoreFiles(Long ticketId) {
        PersonalFilestoreTicket ticket = findPersonalFilestoreTicket(ticketId);
        if (ticket == null) {
            return Collections.emptyList();
        }
        return getPersonalFilestore(ticket.getSubmitter()).retrieveAll(ticket);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#getPersonalFilestore(org.tdar.core.bean.PersonalFilestoreTicket)
     */
    @Override
    public synchronized PersonalFilestore getPersonalFilestore(PersonalFilestoreTicket ticket) {
        return getPersonalFilestore(ticket.getSubmitter());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#getPersonalFilestore(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public synchronized PersonalFilestore getPersonalFilestore(Long ticketId) {
        PersonalFilestoreTicket ticket = findPersonalFilestoreTicket(ticketId);
        return getPersonalFilestore(ticket);
    }

    @Override
    @Transactional(readOnly = false)
    public TdarDir createDirectory(TdarDir parent, String name, BillingAccount account, TdarUser authenticatedUser) throws FileAlreadyExistsException {
        List<AbstractFile> listFiles = listFiles(parent, account, null, null, authenticatedUser);
        for (AbstractFile f : listFiles) {
            if (f instanceof TdarDir && StringUtils.equalsIgnoreCase(f.getName(), name)) {
                throw new FileAlreadyExistsException(name);
            }
        }
        TdarDir dir = new TdarDir();
        dir.setAccount(account);
        dir.setFilename(name);
        dir.setInternalName(name);
        dir.setParent(parent);
        dir.setDateCreated(new Date());
        dir.setUploader(authenticatedUser);
        genericDao.saveOrUpdate(dir);
        return dir;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbstractFile> listFiles(TdarDir parent, BillingAccount account, String term, FileOrder sort, TdarUser authenticatedUser) {
        return fileProcessingDao.listFilesFor(parent, account, term, sort, authenticatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TdarDir> listDirectories(TdarDir parent, BillingAccount account, TdarUser authenticatedUser) {
        return fileProcessingDao.listDirectoriesFor(parent, account, authenticatedUser);
    }

    @Transactional(readOnly = false)
    public void groupTdarFiles(Collection<TdarFile> files) throws Throwable {
   
        TdarFileReconciller reconciler = new TdarFileReconciller(analyzer.getExtensionsForType(ResourceType.values()));
        Map<TdarFile, RequiredOptionalPairs> reconcile = reconciler.reconcile(files);
        genericDao.saveOrUpdate(reconcile.keySet());
        
        for (Entry<TdarFile, RequiredOptionalPairs> entry : reconcile.entrySet()) {
            validate(entry.getKey(), entry.getValue());
            process(entry.getKey(), entry.getValue());

        }
    }

    @Transactional(readOnly = false)
    public void validate(TdarFile file, RequiredOptionalPairs pair) {
        // validate that the package is complete and the files have a size
        List<String> extensions = new ArrayList<>();
        extensions.add(file.getExtension());
        for (TdarFile part : file.getParts()) {
            extensions.add(part.getExtension());
        }
        if (pair.isValid(extensions)) {
            file.setStatus(ImportFileStatus.VALIDATED);
        } else {
            file.setStatus(ImportFileStatus.VALIDATION_FAILED);
        }
        genericDao.saveOrUpdate(file);

    }

    @Transactional(readOnly = false)
    public void process(TdarFile file, RequiredOptionalPairs pair) throws Throwable {
        WorkflowContext ctx = new WorkflowContext();
        ctx.setTdarFile(true);
        ctx.setPrimaryExtension(file.getExtension());
        ctx.setOriginalFile(FileStoreFileUtils.copyTdarFile(file));
        for (TdarFile f : file.getParts()) {
            FileStoreFile fsf = FileStoreFileUtils.copyTdarFile(f);
            ctx.getOriginalFile().getParts().add(fsf);
        }
        ctx.setTargetDatabase(tdarDataImportDatabase);
        ctx.setHasDimensions(pair.isHasDimensions());
        ctx.setOkToStoreInFilestore(false);
        if (pair.getDatasetConverter() != null) {
            ctx.setDatasetConverter(pair.getDatasetConverter());
            ctx.setDataTableSupported(true);
        }
        ctx.setFilestore(TdarConfiguration.getInstance().getFilestore());
        ctx.setWorkflowClass(pair.getWorkflowClass());
        Workflow workflow_ = ctx.getWorkflowClass().newInstance();
        if (ctx.isCodingSheet() == false && ctx.isDataTableSupported() && workflow_ instanceof HasDatabaseConverter) {
            ctx.setDatasetConverter(((HasDatabaseConverter) workflow_).getDatabaseConverterForExtension(ctx.getPrimaryExtension()));
        }
        boolean success = workflow_.run(ctx);
        if (success) {
            for (FileStoreFile f : ctx.getVersions()) {
                file.getVersions().add(FileStoreFileUtils.copyToTdarFileVersion(f));
            }
            file.setLength(ctx.getNumPages());
            file.setHeight(ctx.getOriginalFile().getHeight());
            file.setWidth(ctx.getOriginalFile().getWidth());
            datasetImportService.reconcileDataset(file, ctx.getDataTables(), ctx.getRelationships());
            file.setStatus(ImportFileStatus.PROCESSED);
        } else {
            file.setStatus(ImportFileStatus.PROCESING_FAILED);
        }
        genericDao.saveOrUpdate(file);

    }

    @Override
    @Transactional(readOnly = false)
    public void deleteFile(AbstractFile file, TdarUser authenticatedUser) throws FileUploadException {
        if (file instanceof TdarFile) {
            fileProcessingDao.delete(((TdarFile) file).getParts());
        }
        if (file instanceof TdarDir) {
            if (CollectionUtils.isNotEmpty(listFiles((TdarDir) file, file.getAccount(), null, null, authenticatedUser))) {
                throw new FileUploadException("personalFilestoreService.directory.not_empty");
            }
        }
        fileProcessingDao.delete(file);
    }

    @Override
    @Transactional(readOnly = false)
    public void moveFiles(List<AbstractFile> files, TdarDir dir, TdarUser authenticatedUser) {
        for (AbstractFile f : files) {
            f.setParent(dir);
            if (f instanceof TdarFile) {
                for (TdarFile part : ((TdarFile) f).getParts()) {
                    part.setParent(dir);
                    genericDao.saveOrUpdate(part);
                }
            }
            genericDao.saveOrUpdate(f);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public TdarDir findUnfileDir(TdarUser authenticatedUser) {
        return fileProcessingDao.findUnfiledDirByName(authenticatedUser);

    }

    @Override
    @Transactional(readOnly = false)
    public void editMetadata(TdarFile file, String note, boolean needsOcr, CurationState curate, TdarUser user) {
        file.setNote(note);
        file.setCuration(curate);
        file.setRequiresOcr(needsOcr);
        genericDao.saveOrUpdate(file);
    }

    @Override
    @Transactional(readOnly = false)
    public void mark(List<TdarFile> files, Mark action, TdarUser user) {
        for (TdarFile file : files) {
            switch (action) {
                case CURATED:
                    file.setCuratedBy(user);
                    file.setDateCurated(new Date());
                    break;
                case EXTERNAL_REVIEWED:
                    file.setExternalReviewedBy(user);
                    file.setDateExternalReviewed(new Date());
                    break;
                case REVIEWED:
                    file.setReviewedBy(user);
                    file.setDateReviewed(new Date());
                    break;
                case INITIAL_REVIEWED:
                    file.setInitialReviewedBy(user);
                    file.setDateInitialReviewed(new Date());
                    break;
            }
            genericDao.saveOrUpdate(file);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void unMark(List<TdarFile> files, Mark action, TdarUser user) {
        for (TdarFile file : files) {
            switch (action) {
                case CURATED:
                    file.setCuratedBy(null);
                    file.setDateCurated(null);
                    break;
                case EXTERNAL_REVIEWED:
                    file.setExternalReviewedBy(null);
                    file.setDateExternalReviewed(null);
                    break;
                case REVIEWED:
                    file.setReviewedBy(null);
                    file.setDateReviewed(null);
                    break;
                case INITIAL_REVIEWED:
                    file.setInitialReviewedBy(null);
                    file.setDateInitialReviewed(null);
                    break;
            }
            genericDao.saveOrUpdate(file);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public FileComment addComment(AbstractFile file, String comment, TdarUser authenticatedUser) {
        FileComment comm = new FileComment(authenticatedUser, comment);
        file.getComments().add(comm);
        genericDao.saveOrUpdate(file);
        genericDao.saveOrUpdate(comm);
        return comm;
    }

    @Override
    @Transactional(readOnly = false)
    public FileComment resolveComment(AbstractFile file, FileComment comment, TdarUser authenticatedUser) {
        comment.setResolved(true);
        comment.setDateResolved(new Date());
        comment.setResolver(authenticatedUser);
        genericDao.saveOrUpdate(comment);
        return comment;
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceType getResourceTypeForFiles(TdarFile files) {
        // THIS IS A TEMPORARY FIX UNTIL WE HAVE BETTER LOGIC FOR DETERMINING TYPE
        List<ResourceType> types = new ArrayList<>(ResourceType.activeValues());
        types.remove(ResourceType.CODING_SHEET);
        // types.remove(ResourceType.GEOSPATIAL);
        ResourceType resourceType = analyzer.suggestTypeForFileName(files.getName(), types.toArray(new ResourceType[0]));
        if (resourceType == ResourceType.GEOSPATIAL &&
                (files.getExtension().equalsIgnoreCase("jpg") || files.getExtension().equalsIgnoreCase("tif"))) {
            // FIXME: need better logic here
            resourceType = ResourceType.IMAGE;
        }
        return resourceType;
    }

    @Override
    @Transactional(readOnly = false)
    public List<AbstractFile> moveFilesBetweenAccounts(List<AbstractFile> files, BillingAccount account, TdarUser authenticatedUser) {

        List<AbstractFile> allFiles = new ArrayList<>();
        List<AbstractFile> toProcess = new ArrayList<>(files);
        // for each of the initial files, set the parent to NULL because we're not moving the "dir"
        files.forEach(f -> {
            f.setParent(null);
        });
        while (CollectionUtils.isNotEmpty(toProcess)) {
            AbstractFile file = toProcess.remove(0);
            logger.debug("moving {} to {}", file, account);
            if (file instanceof TdarDir) {
                List<AbstractFile> listFiles = listFiles((TdarDir) file, file.getAccount(), null, null, authenticatedUser);
                logger.debug("subdir {} ", listFiles);
                toProcess.addAll(listFiles);
            }

            if (file instanceof TdarFile) {
                for (TdarFile part : ((TdarFile) file).getParts()) {
                    part.setAccount(account);
                    allFiles.add(part);
                }
            }
            file.setAccount(account);
            allFiles.add(file);
        }
        genericDao.saveOrUpdate(allFiles);
        return allFiles;
    }

    @Override
    @Transactional(readOnly = false)
    public void renameDirectory(TdarDir file, BillingAccount account, String name, TdarUser authenticatedUser) throws FileAlreadyExistsException {
        List<AbstractFile> listFiles = listFiles(file.getParent(), account, null, null, authenticatedUser);
        for (AbstractFile f : listFiles) {
            if (f instanceof TdarDir && StringUtils.equalsIgnoreCase(f.getName(), name)) {
                throw new FileAlreadyExistsException(name);
            }
        }
        file.setInternalName(name);
        file.setFilename(name);
        genericDao.saveOrUpdate(file);

    }

    @Override
    @Transactional(readOnly = true)
    public DirSummary summarizeAccountBy(BillingAccount account, Date date, TdarUser authenticatedUser) {
        List<Object[]> resultList = fileProcessingDao.summerizeByAccount(account, date, authenticatedUser);

        List<TdarDir> dirs = fileProcessingDao.listDirectoriesFor(null, account, authenticatedUser);
        Map<Long, TdarDir> dirIdMap = PersistableUtils.createIdMap(dirs);
        Map<Long, Set<Long>> dirChildMap = new HashMap<>();
        Set<Long> topLevel = new HashSet<>();

        // get the structure of the hierarchy tree
        buildChildMapAndTopLevelNodes(dirs, dirChildMap, topLevel);
        DirSummary summary = new DirSummary();

        // setup each dirSummaryPart
        Map<Long, DirSummaryPart> partMap = new HashMap<>();
        for (Object[] row : resultList) {
            DirSummaryPart part = summary.addPart(row);
            setupPart(dirIdMap, partMap, part);
            logger.debug("{}, {}", part.getDirPath(), row);
        }
        // there may be parts that are mid-level directories that are empty, we don't add them, but they should be in the parts array

        summarizeChildren(dirChildMap, topLevel, partMap, dirIdMap);

        summary.getParts().addAll(partMap.values());
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public RecentFileSummary recentByAccount(BillingAccount account, Date dateStart, Date dateEnd, TdarDir dir, TdarUser actor, TdarUser authenticatedUser) {
        return fileProcessingDao.recentByAccount(account, dateStart, dateEnd, dir, actor, authenticatedUser);

    }

    private void setupPart(Map<Long, TdarDir> dirIdMap, Map<Long, DirSummaryPart> partMap, DirSummaryPart part) {
        part.setDir(dirIdMap.get(part.getId()));
        part.setDirPath(buildDirTree(part));
        partMap.put(part.getId(), part);
    }

    /**
     * Find all of the children of a given node, also find "top" level nodes
     * 
     * @param dirs
     * @param dirChildMap
     * @param topLevel
     */
    private void buildChildMapAndTopLevelNodes(List<TdarDir> dirs, Map<Long, Set<Long>> dirChildMap, Set<Long> topLevel) {
        for (TdarDir dir : dirs) {
            if (dir.getParentId() == null) {
                topLevel.add(dir.getId());
                continue;
            }
            TdarDir dir_ = dir;
            while (dir_ != null) {
                Long parentId = dir_.getParentId();
                Set<Long> children = dirChildMap.getOrDefault(parentId, new HashSet<>());
                children.add(dir_.getId());
                dirChildMap.put(parentId, children);
                dir_ = dir_.getParent();
            }
        }
    }

    /**
     * Recursively summarize all children from top down so we don't double count
     * 
     * @param dirChildMap
     * @param topLevel
     * @param partMap
     */
    private void summarizeChildren(Map<Long, Set<Long>> dirChildMap, Set<Long> topLevel, Map<Long, DirSummaryPart> partMap, Map<Long, TdarDir> dirMap) {
        for (Long id : topLevel) {
            Set<Long> working = new HashSet<>(dirChildMap.getOrDefault((id), new HashSet<>()));
            Set<Long> allChildren = new HashSet<>(working);
            while (!working.isEmpty()) {
                Iterator<Long> iterator = working.iterator();
                Long next = iterator.next();
                iterator.remove();

                Set<Long> nextChild = dirChildMap.get(next);
                if (CollectionUtils.isNotEmpty(nextChild)) {
                    working.addAll(nextChild);
                    allChildren.addAll(nextChild);
                }
            }
            DirSummaryPart part = partMap.get(id);
            if (part == null) {
                // logger.debug("creating part {}", id);
                // logger.debug(" chilren {}", dirChildMap.get(id));

                part = new DirSummaryPart(null);
                part.setId(id);
                setupPart(dirMap, partMap, part);
            }

            // part.addAll(allChildren, partMap);
            if (CollectionUtils.isNotEmpty(dirChildMap.get(id))) {
                summarizeChildren(dirChildMap, dirChildMap.get(id), partMap, dirMap);
            }
        }
    }

    private String buildDirTree(DirSummaryPart part) {
        TdarDir parent = part.getDir();
        if (parent == null) {
            return "/";
        }
        StringBuilder path = new StringBuilder();
        TdarDir d = parent;
        while (d != null) {
            path.insert(0, "/" + d.getName());
            d = d.getParent();
        }
        return path.toString();
    }

    @Override
    @Transactional(readOnly = false)
    public void linkCollection(TdarDir file, ResourceCollection collection, TdarUser user) {
        if (!authorizationService.canAddToCollection(user, collection)) {
            throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
        }
        file.setCollection(collection);
        genericDao.saveOrUpdate(file);
        updateLinkedCollection(file, user);
    }

    @Override
    @Transactional(readOnly = false)
    public void updateLinkedCollection(TdarDir file, TdarUser user) {
        if (!authorizationService.canAddToCollection(user, file.getCollection())) {
            throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
        }

        for (AbstractFile f : listFiles(file, file.getAccount(), null, null, user)) {
            if (f instanceof TdarFile == false) {
                continue;
            }
            TdarFile tdarFile = (TdarFile) f;
            if (tdarFile.getResource() == null) {
                continue;
            }
            Resource resource = tdarFile.getResource();
            if (resource.getManagedResourceCollections().contains(file.getCollection())) {
                continue;
            }
            resourceCollectionService.addResourceCollectionToResource(resource, resource.getManagedResourceCollections(), user, true,
                    ErrorHandling.VALIDATE_WITH_EXCEPTION, file.getCollection(),
                    CollectionResourceSection.MANAGED);
        }

    }

    @Override
    @Transactional(readOnly = false)
    public void unlinkLinkedCollection(TdarDir file, TdarUser user) {
        if (!authorizationService.canAddToCollection(user, file.getCollection())) {
            throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
        }

        file.setCollection(null);
        genericDao.saveOrUpdate(file);
    }

}
