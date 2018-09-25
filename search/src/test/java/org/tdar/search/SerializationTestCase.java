package org.tdar.search;

import java.io.IOException;

import org.junit.Test;
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
        
        System.out.println(serializationService.convertToJson(sp));
        
    }
}
