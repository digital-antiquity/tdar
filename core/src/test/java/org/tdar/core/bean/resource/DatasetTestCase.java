package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.tdar.core.dao.resource.DatasetDao;

public class DatasetTestCase {

    private static final String TEST = "test";

    @Test
    public void testrename() {
        DatasetDao datasetDao = new DatasetDao();
        Set<String> names = new HashSet<>();
        String name = datasetDao.getUniqueTableName(names, TEST);
        assertEquals(TEST, name);
        names.add(name);
        name = datasetDao.getUniqueTableName(names, TEST);
        assertEquals(TEST + " (1)", name);
        names.add(name);
        name = datasetDao.getUniqueTableName(names, TEST);
        assertEquals(TEST + " (2)", name);
    }
}
