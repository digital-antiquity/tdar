package org.tdar.core.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.web.WebFileSystemResourceService;

/**
 * Created by jimdevos on 2/26/15.
 */
public class FileSystemResourceServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private WebFileSystemResourceService service;

    @Test
    public void sanityTest() {
        assertThat("test properly configured", service, notNullValue());
    }

    @Test
    public void testGroupContentDefault() throws URISyntaxException {
        List<String> list = service.fetchGroupUrls("default");
        assertThat(list, is(not(empty())));
        List<String> integrationList = service.fetchGroupUrls("ng-integrate");
        assertTrue(integrationList.size() > list.size());
    }

    @Test
    @Ignore
    public void testFetchGroupNames() throws URISyntaxException {
        List<String> groups = service.fetchGroupNames();
        assertThat(groups, is(not(empty())));
    }
}
