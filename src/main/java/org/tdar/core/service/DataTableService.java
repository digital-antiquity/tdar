package org.tdar.core.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.dao.resource.DataTableColumnDao;
import org.tdar.core.dao.resource.DataTableDao;
import org.tdar.db.model.PostgresDatabase;

/**
 * $Id$
 * <p>
 * Transactional service layer for data tables in tDAR. Also provides access to the underlying data import database storing the actual data from datasets
 * imported into tDAR.
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
    private PostgresDatabase tdarDataImportDatabase;

    @Autowired
    public void setDao(DataTableDao dao) {
        super.setDao(dao);
    }

    @Transactional(readOnly = true)
    public DataTable findByName(final String name) {
        return getDao().findByName(name);
    }

    @Transactional(readOnly = true)
    public List<DataTable> findAllFromIdList(List<Long> ids) {
        return getDao().findAllFromIdList(ids);
    }

    @Transactional(readOnly = true)
    public DataTableColumn findDataTableColumn(Long dataTableColumnId) {
        return dataTableColumnDao.find(dataTableColumnId);
    }

    @Transactional(readOnly = true)
    public List<String> findAllDistinctValues(DataTableColumn column) {
        return tdarDataImportDatabase.selectNonNullDistinctValues(column);
    }

    @Transactional(readOnly = true)
    public <T> T query(PreparedStatementCreator psc, PreparedStatementSetter pss, ResultSetExtractor<T> rse) {
        return tdarDataImportDatabase.query(psc, pss, rse);
    }

    @Transactional(readOnly = true)
    public List<DataTableColumn> findOntologyMappedColumns(Dataset dataset) {
        return dataTableColumnDao.findOntologyMappedColumns(dataset);
    }

    @Transactional(readOnly = true)
    public List<DataTable> findDataTablesUsingResource(Resource resource) {
        return getDao().findDataTablesUsingResource(resource);
    }

    public void setDataTableColumnDao(DataTableColumnDao dataTableColumnDao) {
        this.dataTableColumnDao = dataTableColumnDao;
    }

    public void setTdarDataImportDatabase(PostgresDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }

}
