/**
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.AsyncUpdateReceiver.DefaultReceiver;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.workflow.ActionMessageErrorListener;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.ResourceCreatorProxy;
import org.tdar.utils.Pair;
import org.tdar.utils.activity.Activity;
import org.tdar.utils.bulkUpload.BulkManifestProxy;
import org.tdar.utils.bulkUpload.BulkUploadTemplate;
import org.tdar.utils.bulkUpload.CellMetadata;

/**
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class BulkUploadService {

    private static final String THE_FIRST_COLUMN_MUST_BE_THE_FILENAME = "the first column must be the filename";
    private static final String THE_FOLLOWING_COLUMNS_ARE_REQUIRED_S = "the following columns are required: %s";
    private static final String DUPLICATE_FILENAME_S_WAS_FOUND_IN_MANIFEST_FILE = "Duplicate Filename %s was found in manifest file";
    private static final String FIELDNAME_IS_NOT_VALID_FOR_TYPE = "%s : the fieldname %s is not valid for the resource type:%s";
    private static final String RESOURCE_CREATOR_IS_NOT_VALID_FOR_TYPE = "resource creator is not valid %s, %s, %s (check appropriate role for type)";
    private static final String SKIPPING_LINE_FILENAME_NOT_FOUND = "skipping line in excel file as resource with the filename \"%s\" was not found in the import batch";
    private static final String THE_SYSTEM_HAS_NOT_RECEIVED_ANY_FILES = "The system has not received any files.";
    private static final String AN_EXCEPTION_OCCURED_WHILE_PROCESSING_THE_MANIFEST_FILE = "an exception occured while processing the manifest file";
    private static final String PLEASE_INCLUDE_THE_FILE_EXTENSION_IN_THE_FILENAME = "please include the file extension in the filename";
    private static final String THE_MANIFEST_FILE_UPLOADED_APPEARS_TO_BE_EMPTY_NO_COLUMNS_FOUND = "the manifest file uploaded appears to be empty, no columns found";

    @Autowired
    private EntityService entityService;

    @Autowired
    private PersonalFilestoreService filestoreService;

    @Autowired
    @Qualifier("genericDao")
    private GenericDao genericDao;

    @Autowired
    private InformationResourceService informationResourceService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private FileAnalyzer analyzer;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ReflectionService reflectionService;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private DataFormatter formatter = new HSSFDataFormatter();

    private Map<Long, AsyncUpdateReceiver> asyncStatusMap = new WeakHashMap<Long, AsyncUpdateReceiver>();

    @Async
    public void saveAsync(final InformationResource image,
            final Person submitter, final Long ticketId,
            final File excelManifest, final Collection<FileProxy> fileProxies, Long accountId) {
        save(image, submitter, ticketId, excelManifest, fileProxies, accountId);
    }

    public void save(final InformationResource image, final Person submitter,
            final Long ticketId, final File excelManifest,
            final Collection<FileProxy> fileProxies, Long accountId) {
        Map<String, Resource> resourcesCreated = new HashMap<String, Resource>();

        logger.debug("BEGIN ASYNC: " + image + fileProxies);

        // in an async method the image's persistent associations will have
        // become detached from the hibernate session that loaded them,
        // and their lazy-init fields will be unavailable. So we reload them to
        // make them part of the current session and regain access
        // to any lazy-init associations.
        if (image.getProject() != null) {
            Project p = genericDao.find(Project.class, image.getProject()
                    .getId());
            image.setProject(p);
        }
        logger.debug("ticketID:" + ticketId);
        Activity activity = new Activity();
        activity.setName(String.format("bulk upload for %s of %s resources", submitter.getName(), fileProxies.size()));
        activity.start();
        ActivityManager.getInstance().addActivityToQueue(activity);
        AsyncUpdateReceiver receiver = new DefaultReceiver();
        asyncStatusMap.put(ticketId, receiver);
        float count = 0f;
        image.setDescription("");
        image.setDate(-1);
        if (CollectionUtils.isEmpty(fileProxies)) {
            TdarRecoverableRuntimeException throwable = new TdarRecoverableRuntimeException(THE_SYSTEM_HAS_NOT_RECEIVED_ANY_FILES);
            receiver.addError(throwable);
            throw throwable;
        }
        logger.debug("mapping metadata with excelManifest:" + excelManifest);
        BulkManifestProxy manifestProxy = null;
        // try {

        FileInputStream stream = null;
        if (excelManifest != null && excelManifest.exists()) {
            logger.debug("processing manifest:" + excelManifest.getName());
            try {
                stream = new FileInputStream(excelManifest);
                Workbook workbook = WorkbookFactory.create(stream);
                manifestProxy = validateManifestFile(workbook.getSheetAt(0));
            } catch (Exception e) {
                logger.debug("exception happened when reading excel file", e);
                receiver.addError(e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }

        // If there are errors, then stop...
        if (StringUtils.isNotBlank(receiver.getAsyncErrors())) {
            completeBulkUpload(image, accountId, resourcesCreated, activity, receiver, stream, ticketId);
            return;
        }

        if (manifestProxy != null && !manifestProxy.isCaseSensitive()) {
            resourcesCreated = new TreeMap<String, Resource>(String.CASE_INSENSITIVE_ORDER);
        }

        logger.info("bulk: creating individual resources");
        count = processFileProxiesIntoResources(image, submitter, manifestProxy, fileProxies, receiver, resourcesCreated, count);

        logger.info("bulk: applying manifest file data to resources");
        try {
            readExcelFile(manifestProxy, resourcesCreated, receiver);
        } catch (Exception e) {
            logger.warn(AN_EXCEPTION_OCCURED_WHILE_PROCESSING_THE_MANIFEST_FILE, e);
        }

        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            Account account = genericDao.find(Account.class, accountId);
            try {
            accountService.updateQuota(account, resourcesCreated.values().toArray(new Resource[0]));
            } catch (Throwable t) {
                logger.error(t.getMessage(),t);
                throw t;
            }
        }

        logger.info("bulk: setting final statuses and logging");
        for (Resource resource : resourcesCreated.values()) {
            receiver.update(receiver.getPercentComplete(), String.format("saving %s", resource.getTitle()));
            String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]", resource.getResourceType().getLabel(), submitter,
                    resource.getId(), StringUtils.left(resource.getTitle(), 100));

            try {
                resourceService.saveRecordToFilestore(resource);
                resourceService.logResourceModification(resource, submitter, logMessage);
                // FIXME: saveRecordToFilestore doesn't distinguish 'recoverable' from 'disastrous' exceptions. Until it does we just have to assume the worst.
                resource.setReadyToIndex(true);
                genericDao.saveOrUpdate(resource);
            } catch (TdarRecoverableRuntimeException trex) {
                receiver.addError(trex);
                receiver.setCompleted();
                // rethrow so that hibernate will rollback these saves
                throw trex;
            }
        }

        completeBulkUpload(image, accountId, resourcesCreated, activity, receiver, stream, ticketId);

    }

    private void completeBulkUpload(InformationResource image, Long accountId, Map<String, Resource> resourcesCreated,
            Activity activity, AsyncUpdateReceiver receiver, FileInputStream stream, Long ticketId) {

        try {
            PersonalFilestoreTicket findPersonalFilestoreTicket = filestoreService.findPersonalFilestoreTicket(ticketId);
            filestoreService.getPersonalFilestore(findPersonalFilestoreTicket).purgeQuietly(findPersonalFilestoreTicket);
        } catch (Exception e) {
            receiver.addError(e);
        }

        receiver.setCompleted();
        logger.info("bulk upload complete");
        logger.info("remaining: " + image.getInternalResourceCollection());
        activity.end();
        image.setStatus(Status.DELETED);
        try {
            IOUtils.closeQuietly(stream);
        } catch (Exception e) {
            logger.debug("an exception occured when closing import stream", e);
        }

    }

    public BulkManifestProxy validateManifestFile(Sheet sheet) {
        List<String> columnNames = new ArrayList<String>();
        List<String> errorColumns = new ArrayList<String>();

        LinkedHashSet<CellMetadata> allValidFields = getAllValidFieldNames();
        Map<String, CellMetadata> cellLookupMap = getCellLookupMap(allValidFields);
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        Row columnNamesRow = sheet.getRow(ExcelService.FIRST_ROW);

        // capture all of the column names and make sure they're valid in general

        Set<CellMetadata> required = new HashSet<CellMetadata>();
        for (int i = columnNamesRow.getFirstCellNum(); i <= columnNamesRow.getLastCellNum(); i++) {
            String name = excelService.getCellValue(formatter, evaluator, columnNamesRow, i);
            name = StringUtils.replace(name, "*", "").trim(); // remove required char
            columnNames.add(name);

            CellMetadata cellMetadata = cellLookupMap.get(name);
            if (cellMetadata != null) {
                if (cellMetadata.isRequired()) {
                    required.add(cellMetadata);
                }

            } else if (!StringUtils.isBlank(name)) {
                logger.debug("error name: {}", name);
                errorColumns.add(name);
            }
        }

        // if (!errorColumns.isEmpty()) {
        // logger.info("error columns" + errorColumns);
        // throw new TdarRecoverableRuntimeException("the following column names are not 'valid' tDar field names:" + StringUtils.join(errorColumns, ", "));
        // }

        List<String> requiredErrors = new ArrayList<String>();
        for (CellMetadata field : allValidFields) {
            if (field.isRequired() && !required.contains(field)) {
                requiredErrors.add(field.getDisplayName());
            }
        }

        if (columnNames.isEmpty()) {
            logger.info("the manifest file uploaded appears to be empty");
            throw new TdarRecoverableRuntimeException(THE_MANIFEST_FILE_UPLOADED_APPEARS_TO_BE_EMPTY_NO_COLUMNS_FOUND);
        }

        if (!columnNames.get(ExcelService.FIRST_COLUMN).equals(BulkUploadTemplate.FILENAME)) {
            throw new TdarRecoverableRuntimeException(THE_FIRST_COLUMN_MUST_BE_THE_FILENAME);
        }

        if (CollectionUtils.isNotEmpty(requiredErrors)) {
            throw new TdarRecoverableRuntimeException(String.format(THE_FOLLOWING_COLUMNS_ARE_REQUIRED_S, StringUtils.join(requiredErrors.toArray(), ", ")));
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        BulkManifestProxy proxy = new BulkManifestProxy();
        proxy.setColumnNames(columnNames);
        proxy.setColumnNamesRow(columnNamesRow);
        proxy.setSheet(sheet);
        proxy.setAllValidFields(allValidFields);
        proxy.setCellLookupMap(cellLookupMap);
        Map<String, String> caseTest = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell cell = row.getCell(ExcelService.FIRST_COLUMN);
            if (cell == null) {
                continue;
            }
            String filename = cell.getStringCellValue();
            // if not the label, then...
            if (filename.equalsIgnoreCase(BulkUploadTemplate.FILENAME))
                continue;

            proxy.addFilename(filename);
            List<String> list = proxy.getRowFilenameMap().get(row);
            if (list == null) {
                list = new ArrayList<String>();
                proxy.getRowFilenameMap().put(row, list);
            }
            if (caseTest.containsKey(filename)) {
                String testFile = caseTest.get(filename);
                if (testFile.equals(filename)) {
                    throw new TdarRecoverableRuntimeException(String.format(DUPLICATE_FILENAME_S_WAS_FOUND_IN_MANIFEST_FILE, filename));
                }
                if (testFile.equalsIgnoreCase(filename)) {
                    proxy.setCaseSensitive(true);
                }
            }
            list.add(filename);
        }

        return proxy;
    }

    private float processFileProxiesIntoResources(final InformationResource image, final Person submitter, final BulkManifestProxy manifestProxy,
            final Collection<FileProxy> fileProxies, AsyncUpdateReceiver receiver, Map<String, Resource> resourcesCreated, float count) {

        if (StringUtils.isBlank(image.getTitle())) {
            image.setTitle(BulkUploadTemplate.BULK_TEMPLATE_TITLE);
        }

        for (FileProxy fileProxy : fileProxies) {
            logger.debug("processing:" + fileProxy + " |" + fileProxy.getAction());
            try {
                if (fileProxy == null || fileProxy.getAction() != FileAction.ADD) {
                    // || (excelManifest != null && fileProxy.getFilename()
                    // .equals(excelManifest.getName()))
                    continue;
                }
                String fileName = fileProxy.getFilename();
                // if there is not an exact match in the manifest file then,
                // skip it. If there is no manifest file, then go merrily along
                if (manifestProxy != null && !manifestProxy.containsFilename(fileName)) {
                    logger.info("skipping {} filenames: {} ", fileName, manifestProxy.listFilenames());
                    continue;
                }

                logger.info("inspecting ..." + fileName);
                count++;
                float percent = (count / Float.valueOf(fileProxies.size())) * 50;
                receiver.update(percent, " processing " + fileName);
                String extension = FilenameUtils.getExtension((fileName.toLowerCase()));
                ResourceType suggestTypeForFile = analyzer.suggestTypeForFileExtension(extension, getResourceTypesSupportingBulkUpload());
                Class<? extends Resource> resourceClass = suggestTypeForFile.getResourceClass();

                ActionMessageErrorListener listener = new ActionMessageErrorListener();
                if (InformationResource.class.isAssignableFrom(resourceClass)) {
                    logger.info("saving " + fileName + "..." + suggestTypeForFile);
                    InformationResource informationResource = (InformationResource) resourceService.createResourceFrom(image, resourceClass);
                    informationResource.setReadyToIndex(false);
                    informationResource.setTitle(fileName);
                    informationResource.markUpdated(submitter);
                    informationResource.setDescription(" ");
                    genericDao.saveOrUpdate(informationResource);
                    informationResourceService.importFileProxiesAndProcessThroughWorkflow(informationResource, submitter, null, listener,
                            Arrays.asList(fileProxy));

                    receiver.getDetails().add(new Pair<Long, String>(informationResource.getId(), fileName));
                    resourcesCreated.put(fileName, informationResource);
                    if (listener.hasActionErrors()) {
                        receiver.addError(new Exception(String.format("Errors: %s", listener)));
                    }
                }
            } catch (Exception e) {
                logger.error("something happend", e);
                receiver.addError(e);
            }
        }
        return count;
    }

    public ResourceType[] getResourceTypesSupportingBulkUpload() {
        List<ResourceType> types = new ArrayList<ResourceType>();
        for (ResourceType type : ResourceType.values()) {
            if (type.supportBulkUpload())
                types.add(type);
        }
        return types.toArray(new ResourceType[0]);

    }

    public LinkedHashSet<CellMetadata> getAllValidFieldNames(ResourceType... resourceTypes) {
        List<ResourceType> resourceClasses = new ArrayList<ResourceType>(Arrays.asList(resourceTypes));
        if (ArrayUtils.isEmpty(resourceTypes)) {
            resourceClasses = Arrays.asList(getResourceTypesSupportingBulkUpload());
        }
        CellMetadata filename = CellMetadata.FILENAME;

        LinkedHashSet<CellMetadata> nameSet = new LinkedHashSet<CellMetadata>();
        nameSet.add(filename);
        for (ResourceType clas : resourceClasses) {
            nameSet.addAll(reflectionService.findBulkAnnotationsOnClass(clas.getResourceClass()));
        }

        Iterator<CellMetadata> fields = nameSet.iterator();
        while (fields.hasNext()) {
            CellMetadata field = fields.next();
            logger.trace(field.getName() + " " + field.getDisplayName());
            if (!TdarConfiguration.getInstance().getLicenseEnabled()) {
                if (StringUtils.isNotBlank(field.getDisplayName())
                        && (field.getDisplayName().equals(BulkImportField.LICENSE_TEXT) || field.getDisplayName().equals(BulkImportField.LICENSE_TYPE))) {
                    fields.remove();
                }
            }
            if (!TdarConfiguration.getInstance().getCopyrightMandatory()) {
                if (field.getName().contains("copyrightHolder") || StringUtils.isNotBlank(field.getDisplayName())
                        && (field.getDisplayName().contains(BulkImportField.COPYRIGHT_HOLDER))) {
                    fields.remove();
                }
            }
        }

        return nameSet;
    }

    public Set<CellMetadata> getRequiredFields(ResourceType... resourceTypes) {
        Set<CellMetadata> toReturn = new HashSet<CellMetadata>();
        for (CellMetadata cell : getAllValidFieldNames(resourceTypes)) {
            if (cell.isRequired()) {
                toReturn.add(cell);
            }
        }
        return toReturn;
    }

    /*
     * Special Case lookup: (a) look for exact match (b) look for case where
     * person forgot file extension
     */
    public Resource findResource(String filename, Map<String, Resource> filenameResourceMap) {
        /*
         * DEPENDING ON WHETHER THE MANIFEST IS CASE SENSITIVE OR NOT, the MAP
         * WILL EITHER BE (A) a TreeMap(case insensitive) or a HashMap
         */
        Resource toReturn = filenameResourceMap.get(filename);
        if (toReturn != null) {
            return toReturn;
        }

        for (String name : filenameResourceMap.keySet()) {
            String base = FilenameUtils.getBaseName(name);
            if (base.equals(filename)) {
                if (toReturn != null) {
                    throw new TdarRecoverableRuntimeException(PLEASE_INCLUDE_THE_FILE_EXTENSION_IN_THE_FILENAME);
                }
                toReturn = filenameResourceMap.get(name);
            }
        }

        return toReturn;
    }

    public Map<String, CellMetadata> getCellLookupMap(Set<CellMetadata> set) {
        Map<String, CellMetadata> map = new HashMap<String, CellMetadata>();
        for (CellMetadata meta : set) {
            if (!StringUtils.isEmpty(meta.getName())) {
                map.put(meta.getName(), meta);
            }
            if (!StringUtils.isEmpty(meta.getDisplayName())) {
                map.put(meta.getDisplayName(), meta);
            }
        }
        return map;
    }

    public Set<CellMetadata> getRequiredFields(Set<CellMetadata> set) {
        Set<CellMetadata> map = new HashSet<CellMetadata>();
        for (CellMetadata meta : set) {
            if (meta.isRequired()) {
                map.add(meta);
            }
        }
        logger.trace("{}", map);
        return map;
    }

    @Transactional
    public <R extends Resource> void readExcelFile(BulkManifestProxy manifestProxy, Map<String, Resource> filenameResourceMap, AsyncUpdateReceiver receiver)
            throws InvalidFormatException, IOException {

        if (manifestProxy == null) {
            return;
        }
        FormulaEvaluator evaluator = manifestProxy.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        int rowNum = 0;
        Map<String, CellMetadata> cellLookupMap = manifestProxy.getCellLookupMap();

        // if we use a manifest file, then keep track of all resources that have
        // errors
        for (Row row : manifestProxy.getSheet()) {
            if (row == null) {
                logger.warn("null row.");
                continue;
            }
            if (rowNum != ExcelService.FIRST_ROW) {
                // find the resource for the identifier (based on current title

                int startColumnIndex = manifestProxy.getFirstCellNum();
                int endColumnIndex = manifestProxy.getLastCellNum();

                String filename = excelService.getCellValue(formatter, evaluator, row, startColumnIndex);
                // filename = StringUtils.replace(filename, "*", ""); // remove
                // required char

                // look in the hashmap for the filename, skip the examples
                Resource resourceToProcess = findResource(filename, filenameResourceMap);
                logger.debug("fn: {} resource to Process: {}", filename, resourceToProcess);
                if (StringUtils.isBlank(filename) || filename.equalsIgnoreCase(BulkUploadTemplate.EXAMPLE_PDF)
                        || filename.equalsIgnoreCase(BulkUploadTemplate.EXAMPLE_TIFF)) {
                    continue;
                }
                if (resourceToProcess == null) {
                    receiver.addError(new TdarRecoverableRuntimeException(String.format(SKIPPING_LINE_FILENAME_NOT_FOUND, filename)));
                    continue;
                }
                logger.info("processing:" + filename);

                receiver.setPercentComplete(receiver.getPercentComplete() + 1f);
                receiver.setStatus("processing metadata for:" + filename);

                ResourceCreatorProxy creatorProxy = new ResourceCreatorProxy();

                // there has to be a smarter way to do this generically... iterate through valid field names for class
                boolean seenCreatorFields = false;

                Set<CellMetadata> requiredFields = getRequiredFields(manifestProxy.getAllValidFields());
                requiredFields.remove(cellLookupMap.get(BulkUploadTemplate.FILENAME));
                // iterate through the spreadsheet
                try {
                    for (int columnIndex = (startColumnIndex + 1); columnIndex < endColumnIndex; ++columnIndex) {
                        String value = excelService.getCellValue(formatter, evaluator, row, columnIndex);
                        String name = manifestProxy.getColumnNames().get(columnIndex);
                        CellMetadata cellMetadata = cellLookupMap.get(name);
                        logger.trace("cell metadata: {}", cellMetadata);

                        if (StringUtils.isBlank(name) || StringUtils.isBlank(value) || cellMetadata == null)
                            continue;

                        Class<?> mappedClass = cellMetadata.getMappedClass();
                        boolean creatorAssignableFrom = Creator.class.isAssignableFrom(mappedClass);
                        boolean resourceSubtypeAssignableFrom = false;
                        if (mappedClass != null && resourceToProcess != null) {
                            resourceSubtypeAssignableFrom = mappedClass.isAssignableFrom(resourceToProcess.getClass());
                        }
                        boolean resourceAssignableFrom = Resource.class.isAssignableFrom(mappedClass);
                        boolean resourceCreatorAssignableFrom = ResourceCreator.class.isAssignableFrom(mappedClass);
                        if (cellMetadata == null
                                || !(mappedClass != null && (resourceSubtypeAssignableFrom || resourceCreatorAssignableFrom || creatorAssignableFrom))) {
                            if (mappedClass != null) {
                                throw new TdarRecoverableRuntimeException(String.format(FIELDNAME_IS_NOT_VALID_FOR_TYPE, filename, name,
                                        resourceToProcess.getResourceType()));
                            }
                        }
                        requiredFields.remove(cellMetadata);
                        if (resourceAssignableFrom) {
                            reflectionService.validateAndSetProperty(resourceToProcess, cellMetadata.getPropertyName(), value);

                        } else {
                            if ((resourceCreatorAssignableFrom || creatorAssignableFrom)) {

                                logger.trace(String.format("%s - %s - %s", mappedClass, cellMetadata.getPropertyName(), value));
                                if (resourceCreatorAssignableFrom) {
                                    seenCreatorFields = true;
                                    reflectionService.validateAndSetProperty(creatorProxy, cellMetadata.getPropertyName(), value);

                                    // FIXME: This is a big assumption that role is the last field and then we repeat
                                    reconcileResourceCreator(resourceToProcess, creatorProxy);

                                    creatorProxy = new ResourceCreatorProxy();

                                    seenCreatorFields = false;
                                }
                                if (Person.class.isAssignableFrom(mappedClass)) {
                                    reflectionService.validateAndSetProperty(creatorProxy.getPerson(), cellMetadata.getPropertyName(), value);
                                }
                                if (Institution.class.isAssignableFrom(mappedClass)) {
                                    logger.trace("{} ", cellMetadata);
                                    Object bean = creatorProxy.getInstitution();
                                    if (cellMetadata.getName().contains("Person.Institution")) {
                                        if (creatorProxy.getPerson().getInstitution() == null) {
                                            creatorProxy.getPerson().setInstitution(new Institution());
                                        }
                                        bean = creatorProxy.getPerson().getInstitution();
                                    }
                                    reflectionService.validateAndSetProperty(bean, cellMetadata.getPropertyName(), value);
                                }
                            }
                        }
                    }
                    if (seenCreatorFields) {
                        reconcileResourceCreator(resourceToProcess, creatorProxy);
                    }
                    logger.debug("resourceCreators:{}", resourceToProcess.getResourceCreators());
                    if (requiredFields.size() > 0) {
                        String msg = filename + ": The following required fields have not been provided:";
                        for (CellMetadata meta : requiredFields) {
                            logger.trace("{}", meta);
                            msg += meta.getDisplayName() + ", ";
                        }
                        msg = msg.substring(0, msg.length() - 2);
                        throw new TdarRecoverableRuntimeException(msg);
                    }
                } catch (Throwable t) {
                    logger.debug("excel mapping error: {}", t);
                    resourceToProcess.setStatus(Status.DELETED);
                    receiver.addError(t);
                }
                genericDao.saveOrUpdate(resourceToProcess);
            }
            rowNum++;
        }
    }

    private void reconcileResourceCreator(Resource resource, ResourceCreatorProxy proxy) {
        ResourceCreator creator = proxy.getResourceCreator();
        logger.info("reconciling creator... {}", creator);
        if (creator.isValidForResource(resource)) {
            entityService.findOrSaveResourceCreator(creator);
            creator.setSequenceNumber(resource.getResourceCreators().size());
            logger.debug(creator + " (" + creator.getSequenceNumber() + ")");

            resource.getResourceCreators().add(creator);
            logger.debug("added " + creator + " successfully");
        } else {
            throw new TdarRecoverableRuntimeException(
                    String.format(RESOURCE_CREATOR_IS_NOT_VALID_FOR_TYPE, creator.getCreator().getName(), creator.getRole(), resource.getResourceType()));
        }
    }

    public AsyncUpdateReceiver checkAsyncStatus(Long ticketId) {
        return asyncStatusMap.get(ticketId);
    }

    public HSSFWorkbook createExcelTemplate() {
        BulkUploadTemplate template = new BulkUploadTemplate(excelService);
        return template.getTemplate(getAllValidFieldNames());
    }
}