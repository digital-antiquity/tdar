package org.tdar.core.service.bulk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.data.FileProxy;

/**
 * The BulkManifestProxy helps keep track of state throughout the @link BulkUploadService's run process. It tracks filenames, fields, the resources created and
 * other information
 * 
 * @author abrin
 * 
 */
public class BulkManifestProxy implements Serializable {

    private static final long serialVersionUID = -3716153931002809635L;

    private List<String> filenames = new ArrayList<>();
    private List<String> filenamesInsensitive = new ArrayList<>();
    private boolean caseSensitive = false;
    private Map<Row, List<String>> rowFilenameMap = new HashMap<>();
    private Map<String, CellMetadata> cellLookupMap = new HashMap<>();
    private List<String> columnNames = new ArrayList<>();
    private LinkedHashSet<CellMetadata> allValidFields = new LinkedHashSet<>();
    private Collection<FileProxy> fileProxies;
    private Sheet sheet;
    private Person submitter;

    private Map<String, Resource> resourcesCreated = new HashMap<>();

    private Row columnNamesRow;

    private Set<CellMetadata> required = new HashSet<>();

    public BulkManifestProxy(Sheet sheet2, LinkedHashSet<CellMetadata> allValidFields2, Map<String, CellMetadata> cellLookupMap2) {
        this.sheet = sheet2;
        this.allValidFields = allValidFields2;
        this.cellLookupMap = cellLookupMap2;
    }

    public void addFilename(String filename) {
        filenames.add(filename);
        filenamesInsensitive.add(filename.toLowerCase());
    }

    // don't want to expose the list directly for manipulation because we're managing two variants (case sensitive and insensitive)
    public String listFilenames() {
        return String.format("[%s]", StringUtils.join(filenames, ","));
    }

    public boolean containsFilename(String filename) {
        if (!caseSensitive) {
            return filenamesInsensitive.contains(filename.toLowerCase());
        }
        return filenames.contains(filename);
    }

    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

    public Map<Row, List<String>> getRowFilenameMap() {
        return rowFilenameMap;
    }

    public void setRowFilenameMap(Map<Row, List<String>> rowFilenameMap) {
        this.rowFilenameMap = rowFilenameMap;
    }

    public Map<String, CellMetadata> getCellLookupMap() {
        return cellLookupMap;
    }

    public void setCellLookupMap(Map<String, CellMetadata> cellLookupMap) {
        this.cellLookupMap = cellLookupMap;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public LinkedHashSet<CellMetadata> getAllValidFields() {
        return allValidFields;
    }

    public void setAllValidFields(LinkedHashSet<CellMetadata> allValidFields) {
        this.allValidFields = allValidFields;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public int getLastCellNum() {
        return columnNamesRow.getLastCellNum();
    }

    public int getFirstCellNum() {
        return columnNamesRow.getFirstCellNum();
    }

    public void setColumnNamesRow(Row columnNamesRow) {
        this.columnNamesRow = columnNamesRow;

    }

    public Row getColumnNamesRow() {
        return columnNamesRow;

    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public Map<String, Resource> getResourcesCreated() {
        return resourcesCreated;
    }

    public void setResourcesCreated(Map<String, Resource> resourcesCreated) {
        this.resourcesCreated = resourcesCreated;
    }

    public void setRequired(Set<CellMetadata> required) {
        this.required = required;
    }
    
    public Set<CellMetadata> getRequired() {
        return required;
    }

    public Collection<FileProxy> getFileProxies() {
        return fileProxies;
    }

    public void setFileProxies(Collection<FileProxy> fileProxies) {
        this.fileProxies = fileProxies;
    }

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

}
