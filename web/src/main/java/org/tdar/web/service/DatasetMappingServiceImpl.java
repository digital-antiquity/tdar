package org.tdar.web.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.service.index.SearchIndexService;

@Service
public class DatasetMappingServiceImpl implements DatasetMappingService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final DatasetDao datasetDao;
    private final SearchIndexService searchIndexService;

    @Autowired
    public DatasetMappingServiceImpl(DatasetDao datasetDao, SearchIndexService searchIndexService) {
        this.datasetDao = datasetDao;
        this.searchIndexService = searchIndexService;
    }

    /*
     * convenience method, used for Asynchronous as opposed to the Synchronous version by the Controller
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.DatasetMappingService#remapColumnsAsync(java.util.List, org.tdar.core.bean.resource.Project)
     */
    @Override
    @Async
    @Transactional
    public void remapColumnsAsync(final Dataset dataset, final List<DataTableColumn> columns, final Project project) {
        remapColumns(dataset, columns, project);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.DatasetMappingService#remapColumns(java.util.List, org.tdar.core.bean.resource.Project)
     */
    @Override
    @Transactional
    public void remapColumns(Dataset dataset, List<DataTableColumn> columns, Project project) {
        datasetDao.remapColumns(columns , dataset, project);
        try {
            searchIndexService.indexProject(project);
        } catch (SearchIndexException | IOException e) {
            logger.error("error in reindexing", e);
        }

    }

}
