package org.tdar.core;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.tdar.utils.DeleteOnCloseFileInputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;

public class FileDeletionTestCase {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testDeletion() throws IOException {
        String parent = TdarConfiguration.getInstance().getFileStoreLocation();
        File file = new File(parent, "test.txt");
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.write("this isa  test", fos);
        IOUtils.closeQuietly(fos);
        boolean exceptions = false;
        try {
            DeleteOnCloseFileInputStream is = new DeleteOnCloseFileInputStream(file);
            IOUtils.toString(is);
            IOUtils.closeQuietly(is);
        } catch (Exception e) {
            exceptions = true;
            logger.debug("good", e);
        }
        assertTrue(exceptions);
    }

    @Test
    public void testDeletionAllowed() throws IOException {
        String parent = System.getProperty("java.io.tmpdir");
        File file = new File(parent, "test.txt");
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.write("this isa  test", fos);
        IOUtils.closeQuietly(fos);
        boolean exceptions = false;
        try {
            DeleteOnCloseFileInputStream is = new DeleteOnCloseFileInputStream(file);
            IOUtils.toString(is);
            IOUtils.closeQuietly(is);
        } catch (Exception e) {
            exceptions = true;
            logger.debug("good", e);
        }
        assertFalse(exceptions);
    }

}
