package org.tdar;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.search.service.SearchIndexService;

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
