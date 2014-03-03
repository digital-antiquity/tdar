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
import org.tdar.core.service.bulk.BulkFileProxy;
import org.tdar.core.service.bulk.BulkManifestProxy;
import org.tdar.core.service.bulk.BulkUploadTemplate;
import org.tdar.core.service.bulk.CellMetadata;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.workflow.ActionMessageErrorListener;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.ResourceCreatorProxy;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.Pair;
import org.tdar.utils.activity.Activity;

/**
 * The BulkUploadService support the bulk loading of resources into tDAR through the user interface
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class BulkUploadService {

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

    /**
     * The Save method needs to endpoints, one with the @Async annotation to allow Spring to run it asynchronously, and one without. Note, the @Async
     * annotation does not work in the Spring testing framework
     * 
     * @param image
     * @param submitterId
     * @param ticketId
     * @param excelManifest
     * @param fileProxies
     * @param accountId
     */
    @Async
    public void saveAsync(final InformationResource image,
            final Long submitterId, final Long ticketId,
            final File excelManifest, final Collection<FileProxy> fileProxies, Long accountId) {
        save(image, submitterId, ticketId, excelManifest, fileProxies, accountId);
    }
    
    /**
     * Load the Excel manifest if it exists
     * 
     * @param wrapper
     * @param receiver
     * @return
     */
    public BulkManifestProxy loadExcelManifest(BulkFileProxy wrapper, AsyncUpdateReceiver receiver) {
        BulkManifestProxy manifestProxy = null;
        File excelManifest = wrapper.getFile();
        if (excelManifest != null && excelManifest.exists()) {
            logger.debug("processing manifest:" + excelManifest.getName());
            try {
                wrapper.setStream(new FileInputStream(excelManifest));
                Workbook workbook = WorkbookFactory.create(wrapper.getStream());
                manifestProxy = validateManifestFile(workbook.getSheetAt(0));
            } catch (Exception e) {
                logger.debug("exception happened when reading excel file", e);
                receiver.addError(e);
            } finally {
                IOUtils.closeQuietly(wrapper.getStream());
            }
        }
        return manifestProxy;
    }

    /**
     * The Save method needs to endpoints, one with the @Async annotation to allow Spring to run it asynchronously.  This method:
     * (a) looks at the excel manifest proxy, tries to parse it, fails fast
     * (b) loads the proxy image record and clones it to each resource type as needed one per file being imported
     * (c) applies custom metadata from the manifest
     * (d) returns
     * 
     * @param image
     * @param submitterId
     * @param ticketId
     * @param excelManifest
     * @param fileProxies
     * @param accountId
     */
    @Transactional
    public void save(final InformationResource image, final Long submitterId, final Long ticketId, final File excelManifest_,
            final Collection<FileProxy> fileProxies, Long accountId) {
        Person submitter = genericDao.find(Person.class, submitterId);
        logger.debug("BEGIN ASYNC: " + image + fileProxies);

        
        // in an async method the image's persistent associations will have become detached from the hibernate session that loaded them, and their lazy-init
        // fields will be unavailable. So we reload them to make them part of the current session and regain access to any lazy-init associations.
         
         if (image.getProject() != null) {
            Project p = genericDao.find(Project.class, image.getProject().getId());
            image.setProject(p);
        }

        logger.debug("ticketID:" + ticketId);
        Activity activity = registerActivity(fileProxies, submitter);
        AsyncUpdateReceiver receiver = new DefaultReceiver();
        BulkFileProxy excelManifest = new BulkFileProxy(excelManifest_, activity);
        asyncStatusMap.put(ticketId, receiver);
        float count = 0f;
        image.setDescription("");
        image.setDate(-1);
        if (CollectionUtils.isEmpty(fileProxies)) {
            TdarRecoverableRuntimeException throwable = new TdarRecoverableRuntimeException("bulkUploadService.the_system_has_not_received_any_files");
            receiver.addError(throwable);
            throw throwable;
        }
        logger.debug("mapping metadata with excelManifest:" + excelManifest);
        BulkManifestProxy manifestProxy = loadExcelManifest(excelManifest, receiver);
        if (manifestProxy == null) {
            manifestProxy = new BulkManifestProxy(null, null, null);
        }
        manifestProxy.setFileProxies(fileProxies);
        manifestProxy.setSubmitter(submitter);
        // If there are errors, then stop...
        if (StringUtils.isNotBlank(receiver.getAsyncErrors())) {
            completeBulkUpload(image, accountId, receiver, excelManifest, ticketId);
            return;
        }

        logger.info("bulk: creating individual resources");
        count = processFileProxiesIntoResources(image,  manifestProxy, receiver, count);

        logger.info("bulk: applying manifest file data to resources");
        try {
            readExcelFile(manifestProxy, receiver);
        } catch (Exception e) {
            logger.warn(MessageHelper.getMessage("bulkUploadService.an_exception_occured_while_processing_the_manifest_file"), e);
        }

        updateAccountQuotas(accountId, manifestProxy.getResourcesCreated());
        logAndPersist(manifestProxy, receiver);
        completeBulkUpload(image, accountId, receiver, excelManifest, ticketId);

    }

    /**
     * Update the billing quotas and accounts as needed
     * 
     * @param accountId
     * @param resourcesCreated
     */
    private void updateAccountQuotas(Long accountId, Map<String, Resource> resourcesCreated) {
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            final Account account = genericDao.find(Account.class, accountId);
                accountService.updateQuota(account, resourcesCreated.values().toArray(new Resource[0]));
        }
    }

    /**
     * Log each record to XML and put in the filestore, and persist the record as needed, then let the @link AsyncUpdateReceiver know we're done
     * 
     * @param resourcesCreated
     * @param submitter
     * @param receiver
     */
    private void logAndPersist(BulkManifestProxy proxy, AsyncUpdateReceiver receiver) {
        logger.info("bulk: setting final statuses and logging");
        for (Resource resource : proxy.getResourcesCreated().values()) {
            receiver.update(receiver.getPercentComplete(), String.format("saving %s", resource.getTitle()));
            Person submitter = proxy.getSubmitter();
            String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]", resource.getResourceType(), submitter,
                    resource.getId(), StringUtils.left(resource.getTitle(), 100));

            try {
                resourceService.logRecordXmlToFilestore(resource);
                genericDao.refresh(submitter);
                resourceService.logResourceModification(resource, submitter, logMessage);
                // FIXME: saveRecordToFilestore doesn't distinguish 'recoverable' from 'disastrous' exceptions. Until it does we just have to assume the worst.
                resource.setReadyToIndex(true);
                genericDao.saveOrUpdate(resource);
            } catch (TdarRecoverableRuntimeException trex) {
                receiver.addError(trex);
                receiver.setCompleted();
                throw trex;
            }
        }
    }

    /**
     * Setup the @link Activity so we can track it
     * 
     * @param fileProxies
     * @param submitter
     * @return
     */
    private Activity registerActivity(final Collection<FileProxy> fileProxies, Person submitter) {
        Activity activity = new Activity();
        activity.setName(String.format("bulk upload for %s of %s resources", submitter.getName(), fileProxies.size()));
        activity.start();
        ActivityManager.getInstance().addActivityToQueue(activity);
        return activity;
    }

    /**
     * Close all of our @link InputStream entries, purge the @link PersonalFilestore , delete the @link Image entry, and return
     * 
     * @param image
     * @param accountId
     * @param receiver
     * @param wrapper
     * @param ticketId
     */
    private void completeBulkUpload(InformationResource image, Long accountId, AsyncUpdateReceiver receiver, BulkFileProxy wrapper, Long ticketId) {

        try {
            PersonalFilestoreTicket findPersonalFilestoreTicket = filestoreService.findPersonalFilestoreTicket(ticketId);
            filestoreService.getPersonalFilestore(findPersonalFilestoreTicket).purgeQuietly(findPersonalFilestoreTicket);
        } catch (Exception e) {
            receiver.addError(e);
        }

        receiver.setCompleted();
        logger.info("bulk upload complete");
        logger.info("remaining: " + image.getInternalResourceCollection());
        wrapper.getActivity().end();
        image.setStatus(Status.DELETED);
        try {
            IOUtils.closeQuietly(wrapper.getStream());
        } catch (Exception e) {
            logger.debug("an exception occurred when closing import stream", e);
        }

    }

    /**
     * Reads through the Excel Sheet and validates it. Looks for required fields that are missing, and also parses all files to figure out what files we have
     * and whether to evaluate the entire sheet as case-sensitive filenames or not.
     * 
     * @param sheet
     * @return
     */
    public BulkManifestProxy validateManifestFile(Sheet sheet) {

        Iterator<Row> rowIterator = sheet.rowIterator();

        LinkedHashSet<CellMetadata> allValidFields = getAllValidFieldNames();
        Map<String, CellMetadata> cellLookupMap = getCellLookupMapByName(allValidFields);
        BulkManifestProxy proxy = new BulkManifestProxy(sheet, allValidFields, cellLookupMap);
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        proxy.setColumnNamesRow(sheet.getRow(ExcelService.FIRST_ROW));

        // capture all of the column names and make sure they're valid in general
        initializeColumnMetadata(proxy, cellLookupMap, evaluator);

        List<String> requiredErrors = new ArrayList<String>();
        for (CellMetadata field : allValidFields) {
            if (field.isRequired() && !proxy.getRequired().contains(field)) {
                requiredErrors.add(field.getDisplayName());
            }
        }

        if (proxy.getColumnNames().isEmpty()) {
            logger.info("the manifest file uploaded appears to be empty");
            throw new TdarRecoverableRuntimeException("bulkUploadService.the_manifest_file_uploaded_appears_to_be_empty_no_columns_found");
        }

        if (!proxy.getColumnNames().get(ExcelService.FIRST_COLUMN).equals(BulkUploadTemplate.FILENAME)) {
            throw new TdarRecoverableRuntimeException("bulkUploadService.the_first_column_must_be_the_filename");
        }

        if (CollectionUtils.isNotEmpty(requiredErrors)) {
            throw new TdarRecoverableRuntimeException("bulkUploadService.the_following_columns_are_required_s", StringUtils.join(requiredErrors.toArray(), ", "));
        }

        testFilenameCaseAndAddFiles(rowIterator, proxy);

        return proxy;
    }

    /**
     * Evaluates all of the filenames in the ExcelSheet by iterating over the Row, determines whether we're dealing with a case-sensitive or inensitive system.
     * 
     * @param rowIterator
     * @param proxy
     */
    private void testFilenameCaseAndAddFiles(Iterator<Row> rowIterator, BulkManifestProxy proxy) {
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
                    throw new TdarRecoverableRuntimeException("bulkUploadService.duplicate_filename_s_was_found_in_manifest_file", filename);
                }
                if (testFile.equalsIgnoreCase(filename)) {
                    proxy.setCaseSensitive(true);
                }
            }
            list.add(filename);
        }
        
        if (proxy != null && !proxy.isCaseSensitive()) {
            proxy.setResourcesCreated(new TreeMap<String, Resource>(String.CASE_INSENSITIVE_ORDER));
         }

    }

    /**
     * Iterates over the column headers for the Excel Workbook and grabs all of the required and known fields
     * 
     * @param proxy
     * @param cellLookupMap
     * @param evaluator
     */
    private void initializeColumnMetadata(BulkManifestProxy proxy, Map<String, CellMetadata> cellLookupMap, FormulaEvaluator evaluator) {
        Set<CellMetadata> required = new HashSet<CellMetadata>();
        Row columnNamesRow = proxy.getColumnNamesRow();
        for (int i = columnNamesRow.getFirstCellNum(); i <= columnNamesRow.getLastCellNum(); i++) {
            String name = excelService.getCellValue(formatter, evaluator, columnNamesRow, i);
            name = StringUtils.replace(name, "*", "").trim(); // remove required char
            proxy.getColumnNames().add(name);

            CellMetadata cellMetadata = cellLookupMap.get(name);
            if (cellMetadata != null) {
                if (cellMetadata.isRequired()) {
                    required.add(cellMetadata);
                }
            }
//            } else if (!StringUtils.isBlank(name)) {
//                logger.debug("error name: {}", name);
////                errorColumns.add(name);
//            }
        }
        proxy.getRequired().addAll(required);
    }

    /**
     * Given the template @link Image (@link InformationResource) process each of the @link FileProxy entries (Excel template excluded) into it's own resource
     * 
     * @param image
     * @param submitter
     * @param manifestProxy
     * @param fileProxies
     * @param receiver
     * @param count
     * @return
     */
    private float processFileProxiesIntoResources(final InformationResource image, final BulkManifestProxy manifestProxy, AsyncUpdateReceiver receiver,
            float count) {

        if (StringUtils.isBlank(image.getTitle())) {
            image.setTitle(BulkUploadTemplate.BULK_TEMPLATE_TITLE);
        }

        if (manifestProxy == null) {
            return count;
        }
        
        for (FileProxy fileProxy : manifestProxy.getFileProxies()) {
            logger.trace("processing: {}", fileProxy);
            try {
                if (fileProxy == null || fileProxy.getAction() != FileAction.ADD) {
                    continue;
                }
                logger.debug("processing: {} | {}" , fileProxy , fileProxy.getAction());
                String fileName = fileProxy.getFilename();
                // if there is not an exact match in the manifest file then, skip it. If there is no manifest file, then go merrily along
                if (manifestProxy != null && !manifestProxy.containsFilename(fileName)) {
                    logger.info("skipping {} filenames: {} ", fileName, manifestProxy.listFilenames());
                    continue;
                }

                logger.info("inspecting ... {}", fileName);
                count++;
                float percent = (count / Float.valueOf(manifestProxy.getFileProxies().size())) * 50;
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
                    informationResource.markUpdated(manifestProxy.getSubmitter());
                    informationResource.setDescription(" ");
                    genericDao.saveOrUpdate(informationResource);
                    informationResourceService.importFileProxiesAndProcessThroughWorkflow(informationResource, manifestProxy.getSubmitter(), null, listener,
                            Arrays.asList(fileProxy));

                    receiver.getDetails().add(new Pair<Long, String>(informationResource.getId(), fileName));
                    manifestProxy.getResourcesCreated().put(fileName, informationResource);
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

    /**
     * get the set of @link ResourceType enums that support BulkUpload
     * @return
     */
    public ResourceType[] getResourceTypesSupportingBulkUpload() {
        List<ResourceType> types = new ArrayList<ResourceType>();
        for (ResourceType type : ResourceType.values()) {
            if (type.supportBulkUpload())
                types.add(type);
        }
        return types.toArray(new ResourceType[0]);

    }

    /**
     * For the set of @link ResourceType entries, get all of the valid @link BulkImportField fields that should be used. 
     * @param resourceTypes
     * @return
     */
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
    
    /**
     * Get only the required fields for the @link ResourceType entries that are specified (title, date, filename, description)
     * 
     * @param resourceTypes
     * @return
     */
    public Set<CellMetadata> getRequiredFields(ResourceType... resourceTypes) {
        Set<CellMetadata> toReturn = new HashSet<CellMetadata>();
        for (CellMetadata cell : getAllValidFieldNames(resourceTypes)) {
            if (cell.isRequired()) {
                toReturn.add(cell);
            }
        }
        return toReturn;
    }

    /**
     * Special Case lookup: (a) look for exact match (b) look for case where person forgot file extension
     * 
     * @param filename
     * @param filenameResourceMap
     * @return
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
                    throw new TdarRecoverableRuntimeException("bulkUploadService.please_include_the_file_extension_in_the_filename");
                }
                toReturn = filenameResourceMap.get(name);
            }
        }

        return toReturn;
    }

    /**
     * Create a Map<String,CellMetadata> of the set of @link CellMetadata entries using the name as a key
     * 
     * @param set
     * @return
     */
    public Map<String, CellMetadata> getCellLookupMapByName(Set<CellMetadata> set) {
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

    /**
     * Filter the Set of @link CellMetadata entries by whether they're required
     * 
     * @param set
     * @return
     */
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

    /**
     * Read the entire excel file in row-by-row. Process the row by looking at the fields and reflecting the values into their appropriate beans.  
     * 
     * FIXME: This method needs refactoring and is overly complex
     * 
     * @param manifestProxy
     * @param receiver
     * @throws InvalidFormatException
     * @throws IOException
     */
    @Transactional
    public <R extends Resource> void readExcelFile(BulkManifestProxy manifestProxy, AsyncUpdateReceiver receiver)
            throws InvalidFormatException, IOException {

        if (manifestProxy == null) {
            return;
        }
        FormulaEvaluator evaluator = manifestProxy.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        int rowNum = 0;
        Map<String, CellMetadata> cellLookupMap = manifestProxy.getCellLookupMap();

        // if we use a manifest file, then keep track of all resources that have errors
        for (Row row : manifestProxy.getSheet()) {
            if (row == null) {
                logger.warn("null row.");
                continue;
            }
            rowNum++;
            if (ExcelService.FIRST_ROW == rowNum - 1) {
                continue;
            }
            // find the resource for the identifier (based on current title

            int startColumnIndex = manifestProxy.getFirstCellNum();
            int endColumnIndex = manifestProxy.getLastCellNum();

            String filename = excelService.getCellValue(formatter, evaluator, row, startColumnIndex);

            // look in the hashmap for the filename, skip the examples
            Resource resourceToProcess = findResource(filename, manifestProxy.getResourcesCreated());
            logger.debug("fn: {} resource to Process: {}", filename, resourceToProcess);
            if (StringUtils.isBlank(filename) || filename.equalsIgnoreCase(BulkUploadTemplate.EXAMPLE_PDF)
                    || filename.equalsIgnoreCase(BulkUploadTemplate.EXAMPLE_TIFF)) {
                continue;
            }
            if (resourceToProcess == null) {
                receiver.addError(new TdarRecoverableRuntimeException("bulkUploadService.skipping_line_filename_not_found", filename));
                continue;
            }
            logger.info("processing:" + filename);

            receiver.setPercentComplete(receiver.getPercentComplete() + 1f);
            receiver.setStatus(MessageHelper.getMessage("bulkUploadService.processing_file", filename));

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
                            throw new TdarRecoverableRuntimeException("bulkUploadService.fieldname_is_not_valid_for_type", filename, name,
                                    resourceToProcess.getResourceType());
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
                    String msg = MessageHelper.getMessage("bulkUploadService.required_fields_missing",filename);
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
    }

    /**
     * Confirm that a @link ResourceCreator is valid, and then set it properly on the @link Resource
     * 
     * @param resource
     * @param proxy
     */
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
            throw new TdarRecoverableRuntimeException("bulkUploadService.resource_creator_is_not_valid_for_type", creator.getCreator().getName(), creator.getRole(), resource.getResourceType());
        }
    }

    /**
     * Expose the AsyncStatus Reciever
     * 
     * @param ticketId
     * @return
     */
    public AsyncUpdateReceiver checkAsyncStatus(Long ticketId) {
        return asyncStatusMap.get(ticketId);
    }

    /**
     * Create the Excel BulkUploadTemplate
     * 
     * @return
     */
    public HSSFWorkbook createExcelTemplate() {
        BulkUploadTemplate template = new BulkUploadTemplate(excelService);
        return template.getTemplate(getAllValidFieldNames());
    }
}