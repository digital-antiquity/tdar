package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.bulk.BulkUploadTemplate;
import org.tdar.core.service.bulk.CellMetadata;
import org.tdar.utils.MessageHelper;

/**
 * The BulkUploadService support the bulk loading of resources into tDAR through
 * the user interface
 * 
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class BulkUploadTemplateService {

    @Autowired
    private ReflectionService reflectionService;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Get only the required fields for the @link ResourceType entries that are
     * specified (title, date, filename, description)
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
     * For the set of @link ResourceType entries, get all of the valid @link
     * BulkImportField fields that should be used.
     * 
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
            String displayName = field.getDisplayName();
            String key = field.getKey();
            logger.trace(field.getKey() + "-" + field.getName() + " " + displayName);
            if (!TdarConfiguration.getInstance().getLicenseEnabled()) {
                if ((key.equals(InformationResource.LICENSE_TEXT) || key.equals(InformationResource.LICENSE_TYPE))) {
                    fields.remove();
                }
            }
            if (!TdarConfiguration.getInstance().getCopyrightMandatory()) {
                if ((key.equals(InformationResource.COPYRIGHT_HOLDER))
                        || displayName.contains(
                                CellMetadata.getDisplayLabel(MessageHelper.getInstance(), InformationResource.COPYRIGHT_HOLDER))) {
                    fields.remove();
                }
            }
        }

        return nameSet;
    }

    /**
     * Create a Map<String,CellMetadata> of the set of @link CellMetadata
     * entries using the name as a key
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
     * Create the Excel BulkUploadTemplate
     * 
     * @return
     */
    public HSSFWorkbook createExcelTemplate() {
        BulkUploadTemplate template = new BulkUploadTemplate();
        return template.getTemplate(getAllValidFieldNames());
    }

    /**
     * get the set of @link ResourceType enums that support BulkUpload
     * 
     * @return
     */
    public ResourceType[] getResourceTypesSupportingBulkUpload() {
        return ResourceType.getTypesSupportingBulkUpload();

    }

}