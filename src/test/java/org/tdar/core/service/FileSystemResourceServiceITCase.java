package org.tdar.core.service;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by jimdevos on 2/26/15.
 */
public class FileSystemResourceServiceITCase  {

    private FileSystemResourceService service = new FileSystemResourceService();


    @Test
    public void sanityTest() {
        assertThat( "test properly configured", service, notNullValue());
    }


    @Test 
    public void testGroupContentDefault() {
        List<String> list = service.fetchGroupUrls("default");
        assertThat(list, is( not (empty())));
    }


    @Test @Ignore
    public void testFetchGroupNames() {
        List<String> groups = service.fetchGroupNames();
        assertThat(groups, is (not (empty())));
    }


}
