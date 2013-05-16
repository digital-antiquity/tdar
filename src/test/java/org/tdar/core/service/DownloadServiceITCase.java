package org.tdar.core.service;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;


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
    
    //hashmap representing single level of a directory
    private class DirectoryMap extends HashMap<String, File> {
        public DirectoryMap(File dir) {
            for(File file : dir.listFiles()) {
                put(file.getName(), file);
            }
        }
    }
    
    public void assertArchiveContents(Collection<File> expectedFiles, File archive) throws IOException {
        TConfig config = TConfig.push();
        TConfig.get().setArchiveDetector(TArchiveDetector.ALL);
        TFile arc = new TFile(archive);
        if(!arc.exists()) fail("file does not exist:" + archive);
        if(!arc.isArchive()) fail("file is not an archive:" + archive);
        DirectoryMap dmap = new DirectoryMap(arc);
        Set<String> expectedNames = new HashSet<String>();
        List<String> errs  = new ArrayList<String>(); 
        for(File expected : expectedFiles) {
            File actual = dmap.get(expected.getName());
            if(actual == null) {
                errs.add("expected file not in archive:" + actual.getName());
                continue;
            }
            
            if(!FileUtils.contentEquals(expected, actual)) {
                errs.add(String.format("%s: item in archive %s does not have same content", actual, expected));
            }
        }
        if(errs.size() > 0) {
            for(String err: errs) {
                logger.error(err);
            }
            fail("problems found in archive:" + archive);
        }
    }
    
    //get some files from the test dir and put them into an archive stream
    @Test
    public void testDownloadArchive() throws IOException {
        Collection<File> files = FileUtils.listFiles(ROOT_SRC, null, false);
        File dest = new File(ROOT_DEST, "everything.zip");
        downloadService.generateZipArchive(files, dest);
        assertTrue("file should have been created", dest.exists());
        assertTrue("file should be non-empty", dest.length() > 0);
        //assertArchiveContents(files, dest);
    }
    
    
    
}
