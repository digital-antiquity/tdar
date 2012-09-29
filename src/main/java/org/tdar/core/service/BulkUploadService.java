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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Drawing;
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
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.AsyncUpdateReceiver.DefaultReceiver;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.LanguageEnum;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.CellMetadata;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;

/**
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class BulkUploadService {

    private static final String EXAMPLE_TIFF = "TDAR_EXAMPLE.TIFF";
    private static final String EXAMPLE_PDF = "TDAR_EXAMPLE.PDF";

    @Autowired
    EntityService entityService;

    @Autowired
    PersonalFilestoreService filestoreService;

    @Autowired
    private GenericDao genericDao;

    private InformationResourceService informationResourceService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private FileAnalyzer analyzer;

    @Autowired
    ExcelService excelService;

    public static final String FILENAME = "filename";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private DataFormatter formatter = new HSSFDataFormatter();

    private Map<Long, AsyncUpdateReceiver> asyncStatusMap = new WeakHashMap<Long, AsyncUpdateReceiver>();

    public LinkedHashSet<CellMetadata> getImportFieldNamesForType(ResourceType type) {
        LinkedHashSet<CellMetadata> toReturn = new LinkedHashSet<CellMetadata>();
        CellMetadata filename = new CellMetadata(FILENAME);
        filename.setRequired(true);
        filename.setComment(BulkImportField.FILENAME_DESCRIPTION);
        filename.setOrder(-1000);
        toReturn.add(filename);
        toReturn.addAll(getImportFieldsForType(type));
        return toReturn;
    }

    @Async
    public void saveAsync(final InformationResource image, final Person submitter, final Long ticketId, final File excelManifest,
            final Collection<FileProxy> fileProxies) {
        save(image, submitter, ticketId, excelManifest, fileProxies);
    }

    public void save(final InformationResource image, final Person submitter, final Long ticketId,
            final File excelManifest, final Collection<FileProxy> fileProxies) {

        logger.debug("BEGIN ASYNC: " + image + fileProxies);

        // in an async method the image's persistent associations will have become detached from the hibernate session that loaded them,
        // and their lazy-init fields will be unavailable. So we reload them to make them part of the current session and regain access
        // to any lazy-init associations.
        if (image.getProject() != null) {
            Project p = genericDao.find(Project.class, image.getProject().getId());
            image.setProject(p);
        }
        logger.debug("ticketID:" + ticketId);
        AsyncUpdateReceiver receiver = new DefaultReceiver();
        asyncStatusMap.put(ticketId, receiver);
        Map<String, Resource> resourcesCreated = new HashMap<String, Resource>();
        float count = 0f;
        image.setDescription("");
        image.setDate(-1);
        if (CollectionUtils.isEmpty(fileProxies)) {
            TdarRecoverableRuntimeException throwable = new TdarRecoverableRuntimeException("The system has not received any files.");
            receiver.addError(throwable);
            throw throwable;
        }

        for (FileProxy fileProxy : fileProxies) {
            logger.debug("processing:" + fileProxy + " |" + fileProxy.getAction());
            try {
                if (fileProxy == null || fileProxy.getAction() != FileAction.ADD
                        || (excelManifest != null && fileProxy.getFilename().equals(excelManifest.getName()))) {

                    continue;
                }
                String fileName = fileProxy.getFilename();
                logger.info("inspecting ..." + fileName);
                count++;
                receiver.update((count / Float.valueOf(fileProxies.size())) * 50, " processing " + fileName);
                ResourceType suggestTypeForFile = analyzer.suggestTypeForFileExtension(FilenameUtils.getExtension((fileName.toLowerCase())),
                        ResourceType.DOCUMENT, ResourceType.DATASET, ResourceType.IMAGE);
                image.setTitle(fileName);
                if (InformationResource.class.isAssignableFrom(suggestTypeForFile.getResourceClass())) {
                    logger.info("saving " + fileName + "..." + suggestTypeForFile);
                    InformationResource informationResource = (InformationResource) getInformationResourceService().createResourceFrom(image,
                            suggestTypeForFile.getResourceClass());
                    informationResource.markUpdated(submitter);
                    getInformationResourceService().processFileProxy(informationResource, fileProxy);
                    genericDao.saveOrUpdate(informationResource);
                    receiver.getDetails().add(new Pair<Long, String>(informationResource.getId(), fileName));
                    resourcesCreated.put(fileName, informationResource);
                }
            } catch (Exception e) {
                logger.error("something happend", e);
                receiver.addError(e);
            }
        }
        logger.debug("mapping metadata with excelManifest " + excelManifest);
        if (excelManifest != null && excelManifest.exists()) {
            logger.debug("processing manifest " + excelManifest.getName());
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(excelManifest);
                readExcelFile(stream, resourcesCreated, receiver);
                receiver.setStatus(" applying metadata ");

            } catch (Exception e) {
                logger.debug("there was an error parsing the excel template file", e);
                receiver.addError(e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        for (Resource resource : resourcesCreated.values()) {
            resourceService.saveRecordToFilestore(resource);
            String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]", resource.getResourceType().getLabel(),
                    submitter, resource.getId(), StringUtils.left(resource.getTitle(), 100));
            resourceService.logResourceModification(resource, submitter, logMessage);
        }

        try {
            filestoreService.getPersonalFilestore(ticketId).purgeQuietly(filestoreService.findPersonalFilestoreTicket(ticketId));
        } catch (Exception e) {
            receiver.addError(e);
        }
        receiver.setCompleted();
        logger.info("bulk upload complete");
        logger.info("remaining: " + image.getInternalResourceCollection());
        image.setStatus(Status.DELETED);
    }

    public LinkedHashSet<CellMetadata> getImportFieldsForType(ResourceType type) {
        LinkedHashSet<CellMetadata> fields = new LinkedHashSet<CellMetadata>();
        return getAnnotationsForClass(BulkImportField.class, type.getResourceClass(), fields);
    }

    public LinkedHashSet<CellMetadata> getAnnotationsForClass(Class<? extends Annotation> annotationClass, Class<?> inspectionClass,
            LinkedHashSet<CellMetadata> fields) {
        return getAnnotationsForClass(annotationClass, inspectionClass, fields, "", "", 0);
    }

    public LinkedHashSet<CellMetadata> getAnnotationsForClass(Class<? extends Annotation> annotationClass, Class<?> inspectionClass,
            LinkedHashSet<CellMetadata> fields,
            String namePrefix, String labelPrefix, int add) {
        if (!StringUtils.isEmpty(namePrefix)) {
            namePrefix += ".";
        }
        // iterate through all of the fields
        logger.trace("inspecting " + inspectionClass.getName());
        for (Field field : inspectionClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                // get the annotation, if the implementedSubclasses is defined, then evaluate and use
                Annotation annotation = field.getAnnotation(annotationClass);
                BulkImportField bulkAnnotation = (BulkImportField) annotation;
                Class<?>[] implementedSubclasses = bulkAnnotation.implementedSubclasses();

                Class<?> embedded = field.getType();
                logger.trace("field is a : " + embedded);

                // if we're looking at a collection, use the return type to implement
                if (Collection.class.isAssignableFrom(field.getType())) {
                    try {
                        if (field.getGenericType() instanceof ParameterizedType) {
                            for (Object tt : ((ParameterizedType) field.getGenericType()).getActualTypeArguments()) {
                                if (tt instanceof Class)
                                    embedded = ((Class<?>) tt);
                            }
                        }
                        logger.trace("adding : " + embedded);
                        getAnnotationsForClass(BulkImportField.class, embedded, fields, namePrefix + embedded.getSimpleName(), bulkAnnotation.label(),
                                bulkAnnotation.order() + add);
                    } catch (Exception e) {
                    }
                    // check that we're dealing with a real class here
                } else if (implementedSubclasses != null && implementedSubclasses.length > 0) {
                    // if we have suggested subclasses, use that
                    for (Class<?> subclass : implementedSubclasses) {
                        getAnnotationsForClass(BulkImportField.class, subclass, fields, namePrefix + subclass.getSimpleName(), bulkAnnotation.label(), 0);
                    }
                } else if (Persistable.class.isAssignableFrom(embedded)) {
                    getAnnotationsForClass(BulkImportField.class, embedded, fields, namePrefix + embedded.getSimpleName(), bulkAnnotation.label(), 0);
                } else {
                    // otherwise, just use the field

                    fields.add(new CellMetadata(namePrefix + field.getName(), bulkAnnotation, inspectionClass, labelPrefix));
                }
            }
        }

        if (inspectionClass != Object.class) {
            return getAnnotationsForClass(annotationClass, inspectionClass.getSuperclass(), fields);
        }
        return fields;
    }

    public LinkedHashSet<CellMetadata> getAllValidFieldNames() {
        LinkedHashSet<CellMetadata> nameSet = new LinkedHashSet<CellMetadata>();
        for (ResourceType type : ResourceType.values()) {
            nameSet.addAll(getImportFieldNamesForType(type));
        }

        return nameSet;
    }

    /*
     * Special Case lookup:
     * (a) look for exact match
     * (b) look for case where person forgot file extension
     */
    public Resource findResource(String filename, Map<String, Resource> filenameResourceMap) {
        Resource toReturn = filenameResourceMap.get(filename);
        if (toReturn != null) {
            return toReturn;
        }

        for (String name : filenameResourceMap.keySet()) {
            if (FilenameUtils.getBaseName(name).equals(filename)) {
                if (toReturn != null) {
                    throw new TdarRecoverableRuntimeException("please include the file extension in the filename");
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
        return map;
    }

    public <R extends Resource> void readExcelFile(InputStream stream, Map<String, Resource> filenameResourceMap, AsyncUpdateReceiver receiver)
            throws InvalidFormatException, IOException {
        Workbook workbook = WorkbookFactory.create(stream);
        Sheet sheet = workbook.getSheetAt(0);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        int rowNum = 0;

        LinkedHashSet<CellMetadata> allValidFields = getAllValidFieldNames();
        Map<String, CellMetadata> cellLookupMap = getCellLookupMap(allValidFields);
        Row columnNamesRow = sheet.getRow(0);
        List<String> columnNames = new ArrayList<String>();
        List<String> errorColumns = new ArrayList<String>();

        // capture all of the column names and make sure they're valid in general
        for (int i = columnNamesRow.getFirstCellNum(); i <= columnNamesRow.getLastCellNum(); i++) {
            String name = getCellValue(evaluator, columnNamesRow, i);
            name = StringUtils.replace(name, "*", ""); // remove required char
            if (!cellLookupMap.containsKey(name) && !StringUtils.isBlank(name)) {
                logger.debug("error name: {}", name);
                errorColumns.add(name);
            } else {
                columnNames.add(name);
            }
        }

        // FIXME: is this needed? or do we just ignore?
        if (!errorColumns.isEmpty()) {
            logger.info("error columns" + errorColumns);
            throw new TdarRecoverableRuntimeException("the following column names are not 'valid' tDar field names:" + StringUtils.join(errorColumns, ", "));
        }

        if (!columnNames.get(0).equals(FILENAME)) {
            throw new TdarRecoverableRuntimeException("the first column must be the filename");
        }

        // if we use a manifest file, then keep track of all resources that have errors
        for (Row row : sheet) {
            if (row == null) {
                logger.warn("null row.");
                continue;
            }
            if (rowNum != 0) {
                // find the resource for the identifier (based on current title

                int startColumnIndex = columnNamesRow.getFirstCellNum();
                int endColumnIndex = columnNamesRow.getLastCellNum();
                int columnNameIndex = 1;

                String filename = getCellValue(evaluator, row, startColumnIndex);
                filename = StringUtils.replace(filename, "*", ""); // remove required char

                // look in the hashmap for the filename, skip the examples
                Resource resourceToProcess = findResource(filename, filenameResourceMap);
                if (StringUtils.isBlank(filename) || filename.equalsIgnoreCase(EXAMPLE_PDF) || filename.equalsIgnoreCase(EXAMPLE_TIFF)) {
                    continue;
                }
                if (resourceToProcess == null) {
                    receiver.addError(new TdarRecoverableRuntimeException("skipping line in excel file as resource with the filename \"" + filename
                            + "\" was not found in the import batch"));
                    continue;
                }
                logger.info("processing:" + filename);

                receiver.setPercentComplete(receiver.getPercentComplete() + 1f);
                receiver.setStatus("processing metadata for:" + filename);

                Person person = new Person();
                Institution institution = new Institution();
                ResourceCreator creator = new ResourceCreator();
                // there has to be a smarter way to do this generically... iterate through valid field names for class
                boolean seenCreatorFields = false;

                Set<CellMetadata> requiredFields = getRequiredFields(allValidFields);
                requiredFields.remove(cellLookupMap.get(FILENAME));
                // iterate through the spreadsheet
                try {
                    for (int columnIndex = (startColumnIndex + 1); columnIndex < endColumnIndex; ++columnIndex) {
                        String value = getCellValue(evaluator, row, columnIndex);
                        String name = columnNames.get(columnNameIndex);
                        columnNameIndex++;
                        if (StringUtils.isBlank(name) || StringUtils.isBlank(value))
                            continue;
                        CellMetadata cellMetadata = cellLookupMap.get(name);
                        logger.trace("cell metadata: {}", cellMetadata);
                        if (cellMetadata == null || !(cellMetadata.getMappedClass() != null &&
                                (cellMetadata.getMappedClass().isAssignableFrom(resourceToProcess.getClass()) ||
                                        cellMetadata.getMappedClass().isAssignableFrom(ResourceCreator.class) ||
                                Creator.class.isAssignableFrom(cellMetadata.getMappedClass())))) {
                            if (cellMetadata.getMappedClass() != null) {
                                throw new TdarRecoverableRuntimeException(filename + ": the fieldname " + name + " is not valid for the resource type:"
                                        + resourceToProcess.getResourceType());
                            }
                        }
                        requiredFields.remove(cellMetadata);
                        if (Resource.class.isAssignableFrom(cellMetadata.getMappedClass())) {// only pay attention to classes from a Resource
                            // if we're blank default to the standard value

                            logger.trace("setting property " + cellMetadata.getPropertyName() + " on " + resourceToProcess + " value:" + value);
                            validateAndSetProperty(resourceToProcess, cellMetadata.getPropertyName(), value);
                        } else if ((ResourceCreator.class.isAssignableFrom(cellMetadata.getMappedClass()) || Creator.class.isAssignableFrom(cellMetadata
                                .getMappedClass()))) {

                            logger.trace(cellMetadata.getMappedClass() + " - " + cellMetadata.getPropertyName() + " - " + value);
                            if (ResourceCreator.class.isAssignableFrom(cellMetadata.getMappedClass())) {
                                seenCreatorFields = true;
                                validateAndSetProperty(creator, cellMetadata.getPropertyName(), value);

                                // FIXME: This is a big assumption that role is the last field and then we repeat
                                reconcileResourceCreator(resourceToProcess, creator, person, institution, filename);
                                creator = new ResourceCreator();
                                person = new Person();
                                institution = new Institution();
                                seenCreatorFields = false;
                            }
                            if (Person.class.isAssignableFrom(cellMetadata.getMappedClass())) {
                                validateAndSetProperty(person, cellMetadata.getPropertyName(), value);
                            }
                            if (Institution.class.isAssignableFrom(cellMetadata.getMappedClass())) {
                                validateAndSetProperty(institution, cellMetadata.getPropertyName(), value);
                            }
                        }
                    }
                    if (seenCreatorFields) {
                        reconcileResourceCreator(resourceToProcess, creator, person, institution, filename);
                    }
                    logger.debug("resourceCreators:{}", resourceToProcess.getResourceCreators());
                    if (requiredFields.size() > 0) {
                        String msg = filename + ": The following required fields have not been provided:";
                        for (CellMetadata meta : requiredFields) {
                            logger.debug("{}", meta);
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

    private void reconcileResourceCreator(Resource resource, ResourceCreator creator, Person person, Institution institution,String filename) {
        logger.info("reconciling creator...");
        if (!StringUtils.isEmpty(person.getFirstName()) || !StringUtils.isEmpty(person.getLastName()) || !StringUtils.isEmpty(person.getEmail())) {
            creator.setCreator(person);
            if (!StringUtils.isEmpty(institution.getName())) {
                person.setInstitution(institution);
            }
        } else if (StringUtils.isEmpty(institution.getName())) {
            throw new TdarRecoverableRuntimeException(filename + ": incomplete creator");
        } else {
            creator.setCreator(institution);
        }
        creator.setResource(resource);
        if (creator.isValid()) {
            entityService.findOrSaveResourceCreator(creator);
            creator.setSequenceNumber(resource.getResourceCreators().size());
            logger.debug(creator + " (" + creator.getSequenceNumber() + ")");
            genericDao.saveOrUpdate(creator);
            resource.getResourceCreators().add(creator);
            logger.debug("added " + creator + " successfully");
        } else {
            throw new TdarRecoverableRuntimeException(String.format("%s: resource creator is not valid %s, %s, %s (check appropriate role for type)", filename, creator
                    .getCreator().getName(), creator.getRole(), resource.getResourceType()));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void validateAndSetProperty(Object beanToProcess, String name, String value) {
        try {
            logger.trace("processing: " + beanToProcess + " - " + name + " --> " + value);
            Class propertyType = PropertyUtils.getPropertyType(beanToProcess, name);
            // handle types
            // should we be testing column length?
            if (propertyType.isEnum()) {
                try {
                    BeanUtils.setProperty(beanToProcess, name, Enum.valueOf(propertyType, value));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    throw new TdarRecoverableRuntimeException(value + " is not a valid value for the " + name + " field", e);
                }
            } else {
                if (Integer.class.isAssignableFrom(propertyType)) {
                    try {
                        Double dbl = Double.valueOf(value);
                        if (dbl == Math.floor(dbl)) {
                            value = new Integer((int) Math.floor(dbl)).toString();
                        }
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("the field " + name + " is expecting an integer value, but found: " + value);
                    }
                }
                if (Long.class.isAssignableFrom(propertyType)) {
                    try {
                        Double dbl = Double.valueOf(value);
                        if (dbl == Math.floor(dbl)) {
                            value = new Long((long) Math.floor(dbl)).toString();
                        }
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("the field " + name + " is expecting a big integer value, but found: " + value);
                    }
                }
                if (Float.class.isAssignableFrom(propertyType)) {
                    try {
                        Float.parseFloat(value);
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("the field " + name + " is expecting a floating point value, but found: " + value);
                    }
                }
                BeanUtils.setProperty(beanToProcess, name, value);
            }
        } catch (Exception e1) {
            if (e1 instanceof TdarRecoverableRuntimeException) {
                throw (TdarRecoverableRuntimeException) e1;
            }
            logger.debug("error processing bulk upload: {}", e1);
            throw new TdarRecoverableRuntimeException("an error occured when setting " + name + " to " + value, e1);
        }
    }

    public String getCellValue(FormulaEvaluator evaluator, Row columnNamesRow, int columnIndex) {
        return formatter.formatCellValue(columnNamesRow.getCell(columnIndex), evaluator);
    }

    public AsyncUpdateReceiver checkAsyncStatus(Long ticketId) {
        return asyncStatusMap.get(ticketId);
    }

    public HSSFWorkbook createExcelTemplate() {
        LinkedHashSet<CellMetadata> fieldnameSet = getAllValidFieldNames();
        // fieldnames.
        List<CellMetadata> fieldnames = new ArrayList<CellMetadata>(fieldnameSet);
        Collections.sort(fieldnames);
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("template");
        CreationHelper factory = workbook.getCreationHelper();

        HSSFRow row = sheet.createRow(0);
        // When the comment box is visible, have it show in a 1x3 space

        HSSFCellStyle headerStyle = excelService.createSummaryStyle(workbook);
        HSSFCellStyle headerStyle2 = excelService.createSummaryStyleGray(workbook);
        HSSFCellStyle headerStyle3 = excelService.createSummaryStyleGreen(workbook);

        HashMap<String, String> exampleDoc = new HashMap<String, String>();
        HashMap<String, String> exampleImage = new HashMap<String, String>();

        exampleDoc.put(FILENAME, EXAMPLE_PDF);
        exampleImage.put(FILENAME, EXAMPLE_TIFF);
        exampleDoc.put("title", "EXAMPLE TITLE");
        exampleImage.put("title", "EXAMPLE TITLE");

        exampleDoc.put("title", "EXAMPLE TITLE");
        exampleDoc.put("bookTitle", "Book Title");
        exampleDoc.put("startPage", "20");
        exampleDoc.put("endPage", "40");
        exampleDoc.put("issn", "1111-1111");
        exampleDoc.put("documentType", "BOOK_SECTION");
        exampleImage.put("title", "EXAMPLE TITLE");

        exampleDoc.put("ResourceCreator.role", "AUTHOR");
        exampleImage.put("ResourceCreator.role", "CREATOR");
        exampleImage.put("ResourceCreator.Person.email", "test@test.com");
        exampleDoc.put("ResourceCreator.Person.firstName", "First Name");
        exampleDoc.put("ResourceCreator.Institution.name", "Institutional Author");
        exampleImage.put("ResourceCreator.Person.firstName", "First Name");

        exampleDoc.put("ResourceCreator.Person.lastName", "Last Name");
        exampleImage.put("ResourceCreator.Person.lastName", "Last Name");

        CellMetadata creatorInstitutionRole = null;
        int pos = -1;
        // cleanup for roles and creators to separate individual from institution to
        // help users
        for (int i = 0; i < fieldnames.size(); i++) {
            if (fieldnames.get(i).getName().equals("ResourceCreator.role")) {
                creatorInstitutionRole = fieldnames.get(i);
            }
            if (fieldnames.get(i).getName().equals("ResourceCreator.Institution.name")) {
                pos = i;
            }
        }

        CellMetadata institutionalCreator = fieldnames.remove(pos);
        fieldnames.add(4, institutionalCreator);
        fieldnames.add(5, creatorInstitutionRole);
        // end cleanup

        pos = 4;
        Drawing drawing = sheet.createDrawingPatriarch();

        int i = 0;
        HSSFDataValidationHelper validationHelper = new HSSFDataValidationHelper(sheet);
        for (CellMetadata field : fieldnames) {

            // FIXME: CELL METADATA SHOULD MAINTAIN THE TYPE OF THE FIELD AND THEN CONSTRAIN VALUES BY
            // THE FIELD TYPE -- ENUM, STRING, INTEGER, FLOAT
            if (field.equals("metadataLanguage") || field.equals("resourceLanguage")) {
                excelService.addColumnValidation(sheet, i, validationHelper, LanguageEnum.values());
            }
            if (field.equals("ResourceCreator.role")) {
                excelService.addColumnValidation(sheet, i, validationHelper, ResourceCreatorRole.values());
            }
            if (field.equals("documentType")) {
                excelService.addColumnValidation(sheet, i, validationHelper, DocumentType.values());
            }

            row.createCell(i).setCellValue(field.getOutputName());
            if (field.getMappedClass() != null && field.getMappedClass().equals(Document.class)) {
                row.getCell(i).setCellStyle(headerStyle2);
            } else if (i == pos || i == pos + 1) {
                row.getCell(i).setCellStyle(headerStyle3);
            } else {
                row.getCell(i).setCellStyle(headerStyle);
            }

            excelService.addComment(factory, drawing, row.getCell(i), field.getComment());

            i++;
        }

        HSSFRow rowDoc = sheet.createRow(1);
        HSSFRow rowImg = sheet.createRow(2);
        i = 0;
        String imgRole = "";
        String docRole = "";
        for (CellMetadata field : fieldnames) {
            String imgFld = exampleImage.remove(field.getName());
            String docFld = exampleDoc.remove(field.getName());
            if (imgFld == null) {
                imgFld = "";
            }
            if (docFld == null) {
                docFld = "";
            }
            if (field.getName().equals("ResourceCreator.role")) {
                if (imgRole == "") {
                    imgRole = imgFld;
                    docRole = docFld;
                    imgFld = "";
                } else {
                    imgFld = imgRole;
                    docFld = docRole;
                }
            }
            rowImg.createCell(i).setCellValue(imgFld);
            rowDoc.createCell(i).setCellValue(docFld);
            i++;
        }

        HSSFSheet referenceSheet = workbook.createSheet("REFERENCE");

        HSSFCellStyle summaryStyle = excelService.createSummaryStyle(workbook);

        addReferenceColumn(referenceSheet, DocumentType.values(), "Document Type Values:", summaryStyle, 1);
        addReferenceColumn(referenceSheet, ResourceCreatorRole.values(), "Resource Creator Roles:", summaryStyle, 2);
        addReferenceColumn(referenceSheet, LanguageEnum.values(), "Language Values:", summaryStyle, 3);

        // autosize
        for (int c = 0; c < 4; c++) {
            referenceSheet.autoSizeColumn(i);
        }

        for (int c = 0; c < fieldnames.size(); c++) {
            sheet.autoSizeColumn(i);
        }
        return workbook;
    }

    public <T extends Enum<T>> void addReferenceColumn(HSSFSheet wb, T[] labels, String header, HSSFCellStyle summaryStyle, int col) {
        int rowNum = 0;
        HSSFRow row = excelService.createRow(wb, rowNum);
        row.createCell(col).setCellValue(header);
        row.getCell(col).setCellStyle(summaryStyle);
        for (T type : labels) {
            rowNum++;
            excelService.createRow(wb, rowNum).createCell(col).setCellValue(type.name());
        }
    }

    /**
     * @param informationResourceService
     *            the informationResourceService to set
     */
    @Autowired
    @Qualifier("informationResourceService")
    public void setInformationResourceService(InformationResourceService informationResourceService) {
        this.informationResourceService = informationResourceService;
    }

    /**
     * @return the informationResourceService
     */
    public InformationResourceService getInformationResourceService() {
        return informationResourceService;
    }

}