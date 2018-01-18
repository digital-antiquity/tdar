package org.tdar.core.service.resource;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.integration.IntegrationDataTableSearchResult;
import org.tdar.core.dao.integration.search.DatasetSearchFilter;
import org.tdar.core.dao.resource.DataTableColumnDao;
import org.tdar.core.dao.resource.DataTableDao;
import org.tdar.core.service.ServiceInterface;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.utils.PersistableUtils;

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
public class DataTableServiceImpl extends ServiceInterface.TypedDaoBase<DataTable, DataTableDao> implements DataTableService {

    @Autowired
    private DataTableColumnDao dataTableColumnDao;

    @Autowired
    @Qualifier("target")
    private TargetDatabase tdarDataImportDatabase;

    /*
     * Find a @link DataTable by name
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DataTableService#findByName(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public DataTable findByName(final String name) {
        return getDao().findByName(name);
    }

    /*
     * Find a @link DataTableColumn by Id
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DataTableService#findDataTableColumn(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public DataTableColumn findDataTableColumn(Long dataTableColumnId) {
        return dataTableColumnDao.find(dataTableColumnId);
    }

    /*
     * Find all distinct values for a @link DataTableColumn within the tdardata database
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DataTableService#findAllDistinctValues(org.tdar.core.bean.resource.datatable.DataTableColumn)
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findAllDistinctValues(DataTableColumn column) {
        return tdarDataImportDatabase.selectNonNullDistinctValues(column, false);
    }

    /*
     * Find all distinct values for a @link DataTableColumn, but also return count() occurrences for each value.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DataTableService#findAllDistinctValuesWithCounts(org.tdar.core.bean.resource.datatable.DataTableColumn)
     */
    @Override
    public Map<String, Long> findAllDistinctValuesWithCounts(DataTableColumn dataTableColumn) {
        return tdarDataImportDatabase.selectDistinctValuesWithCounts(dataTableColumn);
    }

    /*
     * Find all @link DataTableColumn entries that are mapped to an @link Ontology
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DataTableService#findOntologyMappedColumns(org.tdar.core.bean.resource.Dataset)
     */
    @Override
    @Transactional(readOnly = true)
    public List<DataTableColumn> findOntologyMappedColumns(Dataset dataset) {
        return dataTableColumnDao.findOntologyMappedColumns(dataset);
    }

    /*
     * Find @link DataTable entries that have @link DataTableColumn objects that map to either a @link CodingSheet or @link Ontology
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DataTableService#findDataTablesUsingResource(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public List<DataTable> findDataTablesUsingResource(Resource resource) {
        return getDao().findDataTablesUsingResource(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DataTableService#findDataTables(org.tdar.core.dao.integration.search.DatasetSearchFilter)
     */
    @Override
    @Transactional(readOnly = true)
    public IntegrationDataTableSearchResult findDataTables(DatasetSearchFilter searchFilter) throws IOException {
        return getDao().findDataTables(searchFilter);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DataTableService#getMissingCodingKeys(org.tdar.core.bean.resource.CodingSheet, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public Set<String> getMissingCodingKeys(CodingSheet sheet, final List<DataTable> tables_) {
        List<DataTable> tables = tables_;
        if (tables == null) {
            tables = findDataTablesUsingResource(sheet);
        }
        logger.trace("tables to check: {}", tables);
        Set<String> uniqueValues = new HashSet<>();
        for (DataTable table : tables) {
            for (DataTableColumn col : table.getDataTableColumns()) {
                if (PersistableUtils.isEqual(col.getDefaultCodingSheet(), sheet)) {
                    try {
                        List<String> selectNonNullDistinctValues = tdarDataImportDatabase.selectNonNullDistinctValues(col, true);
                        logger.trace("unique values for {}: {}", table.getName(), selectNonNullDistinctValues);
                        uniqueValues.addAll(selectNonNullDistinctValues);
                    } catch (Exception e) {
                        logger.error("table doesn't exist: {}", table.getName());
                        // temporarily avoiding blocking to make sure that deploy is safer
                        // if (TdarConfiguration.getInstance().isProductionEnvironment()) {
                        // throw e;
                        // } else {
                        // logger.warn("table doesn't exist: {}", table.getName());
                        // }
                    }
                }
            }
        }
        Set<String> keySet = sheet.getCodeToRuleMap().keySet();
        uniqueValues.removeAll(keySet);
        logger.trace("unique missing: {}", uniqueValues);
        return uniqueValues;
    }

}
