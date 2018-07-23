package org.tdar.core.service.resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.integration.IntegrationDataTableSearchResult;
import org.tdar.core.dao.integration.search.DatasetSearchFilter;
import org.tdar.db.datatable.ImportColumn;
import org.tdar.db.datatable.ImportTable;

public interface DataTableService {

    /*
     * Find a @link DataTable by name
     */
    DataTable findByName(String name);

    /*
     * Find a @link DataTableColumn by Id
     */
    DataTableColumn findDataTableColumn(Long dataTableColumnId);

    /*
     * Find all distinct values for a @link DataTableColumn within the tdardata database
     */
    List<String> findAllDistinctValues(ImportTable table, ImportColumn columnByName);

    /*
     * Find all distinct values for a @link DataTableColumn, but also return count() occurrences for each value.
     */
    Map<String, Long> findAllDistinctValuesWithCounts(ImportTable table, ImportColumn column);

    /*
     * Find all @link DataTableColumn entries that are mapped to an @link Ontology
     */
    List<DataTableColumn> findOntologyMappedColumns(Dataset dataset);

    /*
     * Find @link DataTable entries that have @link DataTableColumn objects that map to either a @link CodingSheet or @link Ontology
     */
    List<DataTable> findDataTablesUsingResource(Resource resource);

    IntegrationDataTableSearchResult findDataTables(DatasetSearchFilter searchFilter) throws IOException;

    /**
     * finds all of the datatables that are mapped to a coding sheet, and gets the unique column entries on each. For those, remove the duplicates and
     * remove the overlap, return anything missing
     * 
     * @param sheet
     * @param tables
     * @return
     */
    Set<String> getMissingCodingKeys(CodingSheet sheet, List<DataTable> tables_);

    DataTable find(Long dataTableId);
    
    public Dataset findDatasetForTable(DataTable dt);


}