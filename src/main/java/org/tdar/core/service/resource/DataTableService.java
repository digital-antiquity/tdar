package org.tdar.core.service.resource;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.resource.DataTableColumnDao;
import org.tdar.core.dao.resource.DataTableDao;
import org.tdar.core.service.ServiceInterface;
import org.tdar.db.model.abstracts.TargetDatabase;

/**
 * $Id$
 * <p>
 * Transactional service layer for data tables in tDAR. Also provides access to the underlying 
 * data import database storing the actual data from datasets imported into tDAR.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Service
public class DataTableService extends ServiceInterface.TypedDaoBase<DataTable, DataTableDao> {

    @Autowired
    private DataTableColumnDao dataTableColumnDao;

    @Autowired
    private TargetDatabase tdarDataImportDatabase;

    /*
     * Find a @link DataTable by name
     */
    @Transactional(readOnly = true)
    public DataTable findByName(final String name) {
        return getDao().findByName(name);
    }

    /*
     * Find a @link DataTableColumn by Id
     */
    @Transactional(readOnly = true)
    public DataTableColumn findDataTableColumn(Long dataTableColumnId) {
        return dataTableColumnDao.find(dataTableColumnId);
    }

    /*
     * Find all distinct values for a @link DataTableColumn within the tdardata database
     */
    @Transactional(readOnly = true)
    public List<String> findAllDistinctValues(DataTableColumn column) {
        return tdarDataImportDatabase.selectNonNullDistinctValues(column);
    }

    /*
     * Find all distinct values for a @link DataTableColumn, but also return count() occurrences for each value.
     */
    public Map<String, Long> findAllDistinctValuesWithCounts(DataTableColumn dataTableColumn) {
        return tdarDataImportDatabase.selectDistinctValuesWithCounts(dataTableColumn);
    }

    /*
     * Find all @link DataTableColumn entries that are mapped to an @link Ontology
     */
    @Transactional(readOnly = true)
    public List<DataTableColumn> findOntologyMappedColumns(Dataset dataset) {
        return dataTableColumnDao.findOntologyMappedColumns(dataset);
    }

    /*
     * Find @link DataTable entries that have @link DataTableColumn objects that map to either a @link CodingSheet or @link Ontology
     */
    @Transactional(readOnly = true)
    public List<DataTable> findDataTablesUsingResource(Resource resource) {
        return getDao().findDataTablesUsingResource(resource);
    }

//    public void setDataTableColumnDao(DataTableColumnDao dataTableColumnDao) {
//        this.dataTableColumnDao = dataTableColumnDao;
//    }

//    public void setTdarDataImportDatabase(PostgresDatabase tdarDataImportDatabase) {
//        this.tdarDataImportDatabase = tdarDataImportDatabase;
//    }


}
