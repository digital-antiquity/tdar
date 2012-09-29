/**
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddressList;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.LanguageEnum;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.AsyncUpdateReceiver.DefaultReceiver;
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

    @Autowired
    EntityService entityService;

    @Autowired
    FilestoreService filestoreService;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private InformationResourceService informationResourceService;

    @Autowired
    private FileAnalyzer analyzer;

    public static final String FILENAME = "filename";
    private Logger logger = Logger.getLogger(getClass());
    private DataFormatter formatter = new HSSFDataFormatter();

    private Map<Long, AsyncUpdateReceiver> asyncStatusMap = new WeakHashMap<Long, AsyncUpdateReceiver>();

    public LinkedHashSet<String> getImportFieldNamesForType(ResourceType type) {
        LinkedHashSet<String> toReturn = new LinkedHashSet<String>(Arrays.asList(FILENAME));
        toReturn.addAll(getImportFieldsForType(type).keySet());
        return toReturn;
    }

    @Async
    public void saveAsync(final InformationResource image, final Person submitter, final Long ticketId, final File excelManifest,
            Collection<FileProxy> fileProxies) {
        save(image, submitter, ticketId, excelManifest, fileProxies);
    }

    @Transactional
    public void save(final InformationResource image, final Person submitter, final Long ticketId, final File excelManifest,
            Collection<FileProxy> fileProxies) {
    	//in an async method the image's persistent associations will have become detached from the hibernate session that loaded them,
    	//and their lazy-init fields will be unavailable.  So we reload them to make them part of the current session and regain access
    	//to any lazy-init associations.
    	if(image.getProject() != null) {
    		Project p = genericDao.find(Project.class, image.getProject().getId());
    		image.setProject(p);
    	}
        try {
            logger.info("image:" + image);
            logger.info("submitter:" + submitter);
            logger.info("ticket:" + ticketId);
            logger.info("manifest:" + excelManifest);
            logger.info("proxies:" + fileProxies);
            AsyncUpdateReceiver receiver = new DefaultReceiver();
            asyncStatusMap.put(ticketId, receiver);
            Map<String, Resource> resourcesCreated = new HashMap<String, Resource>();
            float count = 0f;
            image.setDescription("");
            image.setDateCreated("");
            for (FileProxy fileProxy : fileProxies) {
                if (fileProxy == null || fileProxy.getAction() != FileAction.ADD
                        || (excelManifest != null && fileProxy.getFilename().equals(excelManifest.getName()))) {
                    continue;
                }

                String fileName = fileProxy.getFilename();
                logger.trace("inspecting ..." + fileName);
                receiver.setPercentComplete((count / Float.valueOf(fileProxies.size())) * 50);
                count++;
                receiver.setStatus(" processing " + fileName);
                ResourceType suggestTypeForFile = analyzer.suggestTypeForFileExtension(FilenameUtils.getExtension((fileName.toLowerCase())),
                        ResourceType.DOCUMENT, ResourceType.DATASET, ResourceType.IMAGE);
                image.setTitle(fileName);
                try {
                    if (InformationResource.class.isAssignableFrom(suggestTypeForFile.getResourceClass())) {
                        logger.info("saving " + fileName + "..." + suggestTypeForFile);
                        InformationResource informationResource = (InformationResource) informationResourceService.createResourceFrom(image,
                                suggestTypeForFile.getResourceClass());
                        informationResource.markUpdated(submitter);
                        informationResourceService.processFileProxy(informationResource, fileProxy);
                        genericDao.saveOrUpdate(informationResource);
                        receiver.getDetails().add(new Pair<Long, String>(informationResource.getId(), fileName));
                        resourcesCreated.put(fileName, informationResource);
                    }
                } catch (FileNotFoundException e) {
                    logger.error("Save failed.", e);
                    receiver.addError(e);
                } catch (IOException e) {
                    logger.error("Save failed.", e);
                    receiver.addError(e);
                }
            }
            logger.debug("mapping metadata with excelManifest " + excelManifest);
            if (excelManifest != null && excelManifest.exists()) {
                logger.debug("processing manifest " + excelManifest.getName());
                try {
                    readExcelFile(new FileInputStream(excelManifest), resourcesCreated, receiver);
                    receiver.setStatus(" applying metadata ");

                } catch (Exception e) {
                    logger.debug("there was an error parsing the excel template file", e);
                    receiver.addError(e);
                }
            }
            try {
                filestoreService.getPersonalFilestore(ticketId).purge(filestoreService.findPersonalFilestoreTicket(ticketId));
            } catch (Exception e) {
                receiver.addError(e);
            }

            receiver.setStatus("Complete");
            receiver.setPercentComplete(100f);
            logger.info("bulk upload complete");
            image.setStatus(Status.DELETED);

        } catch (Exception e) {
            throw new TdarRecoverableRuntimeException("something bad happend in bulk upload", e);
        }
    }

    public Map<String, Class<?>> getImportFieldsForType(ResourceType type) {
        Map<String, Class<?>> fields = new LinkedHashMap<String, Class<?>>();
        return getAnnotationsForClass(BulkImportField.class, type.getResourceClass(), fields);
    }

    public Map<String, Class<?>> getAnnotationsForClass(Class<? extends Annotation> annotationClass, Class<?> inspectionClass, Map<String, Class<?>> fields) {
        return getAnnotationsForClass(annotationClass, inspectionClass, fields, "");
    }

    public Map<String, Class<?>> getAnnotationsForClass(Class<? extends Annotation> annotationClass, Class<?> inspectionClass, Map<String, Class<?>> fields,
            String prefix) {
        if (!StringUtils.isEmpty(prefix)) {
            prefix += ".";
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
                        getAnnotationsForClass(BulkImportField.class, embedded, fields, prefix + embedded.getSimpleName());
                    } catch (Exception e) {
                    }
                    // check that we're dealing with a real class here
                } else if (implementedSubclasses != null && implementedSubclasses.length > 0) {

                    // if we have suggested subclasses, use that
                    for (Class<?> subclass : implementedSubclasses) {
                        getAnnotationsForClass(BulkImportField.class, subclass, fields, prefix + subclass.getSimpleName());

                    }
                } else if (Persistable.class.isAssignableFrom(embedded)) {
                    getAnnotationsForClass(BulkImportField.class, embedded, fields, prefix + embedded.getSimpleName());
                } else {
                    // otherwise, just use the field
                    fields.put(prefix + field.getName(), inspectionClass);
                }
            }
        }

        if (inspectionClass != Object.class) {
            return getAnnotationsForClass(annotationClass, inspectionClass.getSuperclass(), fields);
        }
        return fields;
    }

    public List<String> getAllValidFieldNames() {
        LinkedHashSet<String> nameSet = new LinkedHashSet<String>();
        for (ResourceType type : ResourceType.values()) {
            nameSet.addAll(getImportFieldNamesForType(type));
        }
        nameSet.remove("ResourceCreator.Institution.name");
        nameSet.remove("title");
        nameSet.remove("description");
        nameSet.remove("dateCreated");
        nameSet.remove("filename");

        // ADDING FIELDS BACK WITH EXPLICIT ORDER IN MIND
        List<String> nameList = new ArrayList<String>();
        nameList.add("filename");
        nameList.add("title");
        nameList.add("description");
        nameList.add("dateCreated");
        nameList.addAll(nameSet);
        nameList.add("ResourceCreator.Institution.name");
        nameList.add("ResourceCreator.role");

        return nameList;
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

    public <R extends Resource> void readExcelFile(InputStream stream, Map<String, Resource> filenameResourceMap, AsyncUpdateReceiver receiver)
            throws InvalidFormatException, IOException {
        Workbook workbook = WorkbookFactory.create(stream);
        Sheet sheet = workbook.getSheetAt(0);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        int rowNum = 0;

        List<String> validNames = getAllValidFieldNames();
        Row columnNamesRow = sheet.getRow(0);
        List<String> columnNames = new ArrayList<String>();
        List<String> errorColumns = new ArrayList<String>();

        // capture all of the column names and make sure they're valid in general
        for (int i = columnNamesRow.getFirstCellNum(); i <= columnNamesRow.getLastCellNum(); i++) {
            String name = getCellValue(evaluator, columnNamesRow, i);
            name = StringUtils.replace(name, "*", ""); // remove required char
            if (!validNames.contains(name) && !StringUtils.isBlank(name)) {
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

                // look in the hashmap for the filename
                Resource resourceToProcess = findResource(filename, filenameResourceMap);
                if (resourceToProcess == null) {
                    receiver.addError(new TdarRecoverableRuntimeException("a resource with the filename " + filename + " was not found in the import batch"));
                    continue;
                }
                logger.info("processing:" + filename);

                Map<String, Class<?>> importFields = new TreeMap<String, Class<?>>(String.CASE_INSENSITIVE_ORDER);
                importFields.putAll(getImportFieldsForType(resourceToProcess.getResourceType()));
                receiver.setPercentComplete(receiver.getPercentComplete() + 1f);
                receiver.setStatus("processing metadata for:" + filename);

                Person person = new Person();
                Institution institution = new Institution();
                ResourceCreator creator = new ResourceCreator();
                // there has to be a smarter way to do this generically... iterate through valid field names for class
                boolean seenCreatorFields = false;

                // iterate through the spreadsheet
                try {
                    for (int columnIndex = (startColumnIndex + 1); columnIndex < endColumnIndex; ++columnIndex) {
                        String value = getCellValue(evaluator, row, columnIndex);
                        String name = columnNames.get(columnNameIndex);
                        columnNameIndex++;
                        if (StringUtils.isBlank(name) || StringUtils.isBlank(value))
                            continue;

                        if (!importFields.containsKey(name)) {
                            throw new TdarRecoverableRuntimeException("the fieldname " + name + " is not valid for the resource type:"
                                    + resourceToProcess.getResourceType());
                        }
                        Class<?> cls = importFields.get(name);
                        if (Resource.class.isAssignableFrom(cls)) {// only pay attention to classes from a Resource
                            // if we're blank default to the standard value

                            logger.trace("setting property " + name + " on " + resourceToProcess + " value:" + value);
                            validateAndSetProperty(resourceToProcess, name, value);
                        } else if ((ResourceCreator.class.isAssignableFrom(cls) || Creator.class.isAssignableFrom(cls))) {

                            if (name.indexOf(".") != -1) {
                                name = name.substring(name.lastIndexOf(".") + 1);
                            }

                            logger.trace(cls + " - " + name + " - " + value);
                            if (ResourceCreator.class.isAssignableFrom(cls)) {
                                seenCreatorFields = true;
                                validateAndSetProperty(creator, name, value);

                                // FIXME: This is a big assumption that role is the last field and then we repeat
                                reconcileResourceCreator(resourceToProcess, creator, person, institution);
                                creator = new ResourceCreator();
                                person = new Person();
                                institution = new Institution();
                                seenCreatorFields = false;
                            }
                            if (Person.class.isAssignableFrom(cls)) {
                                validateAndSetProperty(person, name, value);
                            }
                            if (Institution.class.isAssignableFrom(cls)) {
                                validateAndSetProperty(institution, name, value);
                            }
                        }
                    }
                    if (seenCreatorFields) {
                        reconcileResourceCreator(resourceToProcess, creator, person, institution);
                    }
                    logger.debug(resourceToProcess.getResourceCreators());
                } catch (Throwable t) {
                    resourceToProcess.setStatus(Status.DELETED);
                    receiver.addError(t);
                }
                genericDao.saveOrUpdate(resourceToProcess);
            }
            rowNum++;
        }
    }

    private void reconcileResourceCreator(Resource resource, ResourceCreator creator, Person person, Institution institution) {
        logger.info("reconciling creator...");
        if (!StringUtils.isEmpty(person.getFirstName()) || !StringUtils.isEmpty(person.getLastName()) || !StringUtils.isEmpty(person.getEmail())) {
            creator.setCreator(person);
            if (!StringUtils.isEmpty(institution.getName())) {
                person.setInstitution(institution);
            }
        } else if (StringUtils.isEmpty(institution.getName())) {
            throw new TdarRecoverableRuntimeException("incomplete creator");
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
            throw new TdarRecoverableRuntimeException(String.format("resource creator is not valid %s, %s, %s (check appropriate role for type)", creator
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
                        Integer.parseInt(value);
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("the field " + name + " is expecting an integer value, but found: " + value);
                    }
                }
                if (Long.class.isAssignableFrom(propertyType)) {
                    try {
                        Long.parseLong(value);
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
            // FIXME: want to catch the error, but not catch the TdarRecoverableRuntimeException
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
            throw new TdarRecoverableRuntimeException("an error occured when setting " + name + " to " + value, e1);
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
            throw new TdarRecoverableRuntimeException("an error occured when setting " + name + " to " + value, e1);
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
            throw new TdarRecoverableRuntimeException("an error occured when setting " + name + " to " + value, e1);
        }

    }

    public String getCellValue(FormulaEvaluator evaluator, Row columnNamesRow, int columnIndex) {
        return formatter.formatCellValue(columnNamesRow.getCell(columnIndex), evaluator);
    }

    public AsyncUpdateReceiver checkAsyncStatus(Long ticketId) {
        return asyncStatusMap.get(ticketId);
    }

    // FIXME: refactor with DataIntegrationService to combine where possible with helper class?
    public HSSFWorkbook createExcelTemplate() {
        List<String> fieldnames = getAllValidFieldNames();
        // fieldnames.

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("template");
        HSSFRow row = sheet.createRow(0);

        HSSFCellStyle headerStyle = workbook.createCellStyle();
        HSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerStyle.setFont(headerFont);

        int i = 0;
        HSSFDataValidationHelper validationHelper = new HSSFDataValidationHelper(sheet);
        for (String name : fieldnames) {
            if (name.equals("metadataLanguage") || name.equals("resourceLanguage")) {
                validateColumn(sheet, i, validationHelper, LanguageEnum.values());
            }
            if (name.equals("ResourceCreator.role")) {
                validateColumn(sheet, i, validationHelper, ResourceCreatorRole.values());
            }
            if (name.equals("documentType")) {
                validateColumn(sheet, i, validationHelper, DocumentType.values());
            }
            if (name.equals("title") || name.equals("filename") || name.equals("description")) {
                name += "*";
            }
            row.createCell(i).setCellValue(name);
            row.getCell(i).setCellStyle(headerStyle);

            i++;
        }

        HSSFSheet referenceSheet = workbook.createSheet("REFERENCE");

        HSSFCellStyle summaryStyle = workbook.createCellStyle();
        HSSFFont summaryFont = workbook.createFont();
        summaryFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        summaryStyle.setFont(summaryFont);

        addReferenceColumn(referenceSheet, DocumentType.values(), "Document Type Values:", summaryStyle, 1);
        addReferenceColumn(referenceSheet, ResourceCreatorRole.values(), "Resource Creator Roles:", summaryStyle, 2);
        addReferenceColumn(referenceSheet, LanguageEnum.values(), "Language Values:", summaryStyle, 3);

        // autosize
        for (int c = 0; c < 4; c++) {
            referenceSheet.autoSizeColumn(i);
        }

        // autosize
        for (int c = 0; c < fieldnames.size(); c++) {
            sheet.autoSizeColumn(i);
        }
        return workbook;
    }

    public <T extends Enum<?>> void validateColumn(HSSFSheet sheet, int i, HSSFDataValidationHelper validationHelper, T[] enums) {
        CellRangeAddressList addressList = new CellRangeAddressList();
        addressList.addCellRangeAddress(1, i, 10000, i);
        DataValidationConstraint validationConstraint = validationHelper.createExplicitListConstraint(getEnumNames(enums).toArray(new String[] {}));
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, validationConstraint);
        dataValidation.setEmptyCellAllowed(true);
        dataValidation.setShowPromptBox(true);
        dataValidation.setSuppressDropDownArrow(false);
        sheet.addValidationData(dataValidation);
    }

    private <T extends Enum<?>> List<String> getEnumNames(T[] enums) {
        List<String> toReturn = new ArrayList<String>();
        for (T value : enums) {
            toReturn.add(value.name());
        }
        return toReturn;
    }

    // "Document Type Values:");
    public <T extends Enum<T>> void addReferenceColumn(HSSFSheet wb, T[] labels, String header, HSSFCellStyle summaryStyle, int col) {
        int rowNum = 0;
        HSSFRow row = createRow(wb, rowNum);
        row.createCell(col).setCellValue(header);
        row.getCell(col).setCellStyle(summaryStyle);
        for (T type : labels) {
            rowNum++;
            createRow(wb, rowNum).createCell(col).setCellValue(type.name());
        }
    }

    private HSSFRow createRow(HSSFSheet wb, int rowNum) {
        HSSFRow row = wb.getRow(rowNum);
        if (row == null) {
            row = wb.createRow(rowNum);
        }
        return row;
    }
}
