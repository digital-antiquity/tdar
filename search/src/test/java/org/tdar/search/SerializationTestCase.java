package org.tdar.search;

import java.io.IOException;

import org.junit.Test;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.service.SerializationServiceImpl;
import org.tdar.search.bean.SearchParameters;
import org.tdar.utils.StringPair;

public class SerializationTestCase {

    
    @Test
    public void test() throws ClassNotFoundException, IOException {
        SerializationServiceImpl serializationService = new SerializationServiceImpl();
        SearchParameters sp = new SearchParameters();
        sp.getAllFields().add("test");
        sp.getAnnotations().add(new StringPair("a", "1"));
        sp.getShares().add(new ResourceCollection(1L, "test", null, null, false));
        sp.getCoverageDates().add(new CoverageDate(CoverageType.RADIOCARBON_DATE, 200, 1000));
        
        System.out.println(serializationService.convertToJson(sp));
        
    }
}
