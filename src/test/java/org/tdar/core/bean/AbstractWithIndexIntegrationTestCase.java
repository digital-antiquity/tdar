package org.tdar.core.bean;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.SearchIndexService;
import org.tdar.struts.action.AbstractControllerITCase;

public abstract class AbstractWithIndexIntegrationTestCase extends AbstractControllerITCase {

    @Autowired private SearchIndexService searchIndexService;

    public SearchIndexService getSearchIndexService() {
    	return searchIndexService;
    }

    @Before
    public void setupIndex() {
    	getSearchIndexService().indexAll();
    }

}
