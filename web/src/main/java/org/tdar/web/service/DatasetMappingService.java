package org.tdar.web.service;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.search.service.index.SearchIndexService;

@Service
public class DatasetMappingService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final DatasetDao datasetDao;
    private final SearchIndexService searchIndexService;

    @Autowired
    public DatasetMappingService(DatasetDao datasetDao, SearchIndexService searchIndexService) {
        this.datasetDao = datasetDao;
        this.searchIndexService = searchIndexService;
    }
    
    /*
     * convenience method, used for Asynchronous as opposed to the Synchronous version by the Controller
     */
    @Async
    @Transactional
    public void remapColumnsAsync(final List<DataTableColumn> columns, final Project project) {
        remapColumns(columns, project);
    }

    @Transactional
    public void remapColumns(List<DataTableColumn> columns, Project project) {
        datasetDao.remapColumns(columns, project);
        try {
            searchIndexService.indexProject(project);
        } catch (SolrServerException | IOException e) {
            logger.error("error in reindexing",e);
        }
        
    }

}
