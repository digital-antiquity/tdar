package org.tdar;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.search.config.TdarSearchAppConfiguration;
import org.tdar.search.service.index.SearchIndexService;

@ContextConfiguration(classes = TdarSearchAppConfiguration.class)
public abstract class AbstractWithIndexIntegrationTestCase extends AbstractIntegrationTestCase {

    @Autowired
    protected SearchIndexService searchIndexService;

    public SearchIndexService getSearchIndexService() {
        return searchIndexService;
    }

    @Before
    public void reindex() {
        getSearchIndexService().indexAll(getAdminUser());
    }

}
