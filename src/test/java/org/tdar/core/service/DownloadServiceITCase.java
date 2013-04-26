package org.tdar.core.service;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

public class DownloadServiceITCase extends AbstractDataIntegrationTestCase {
    private static final File ROOT_DEST = new File("target/test/download-service-it-case");
    private static final File ROOT_SRC = new File(TestConstants.TEST_ROOT_DIR);
    
    //don't need injection (yet)
    DownloadService downloadService = new DownloadService(); 
    
    @Before
    public void prepareDir() throws IOException {
        FileUtils.forceMkdir(ROOT_DEST);
        FileUtils.cleanDirectory(ROOT_DEST);
    }
    
    @After
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(ROOT_DEST);
    }
    
    //get some files from the test dir and put them into an archive stream
    @Test
    public void testDownloadArchive() throws IOException {
        Collection<File> files = FileUtils.listFiles(ROOT_SRC, null, false);
        File dest = new File(ROOT_DEST, "everything.zip");
        downloadService.generateZipArchive(files, dest);
        assertTrue("file should have been created", dest.exists());
        assertTrue("file should be non-empty", dest.length() > 0);
    }
    
    
    
}
