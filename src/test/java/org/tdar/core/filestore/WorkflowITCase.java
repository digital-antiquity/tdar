/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.filestore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.utils.AsynchTester;

/**
 * @author Adam Brin
 *         borrowed from http://eyalsch.wordpress.com/2010/07/13/multithreaded-tests/
 */
public class WorkflowITCase extends AbstractIntegrationTestCase {

    @Autowired
    FileAnalyzer fileAnalyzer;

    @Test
    @Rollback(true)
    public void test() throws InterruptedException, InstantiationException, IllegalAccessException, IOException {
        // List<File>;
        final PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        final List<File> versions = new ArrayList<File>();
        versions.add(new File(TestConstants.TEST_IMAGE_DIR, "/sample_image_formats/grandcanyon.tif"));
        versions.add(new File(TestConstants.TEST_IMAGE_DIR, "/sample_image_formats/grandcanyon_mac.tif"));
        versions.add(new File(TestConstants.TEST_IMAGE_DIR, "/sample_image_formats/grandcanyon.tif"));
        versions.add(new File(TestConstants.TEST_IMAGE_DIR, "/sample_image_formats/grandcanyon_mac.tif"));

        AsynchTester[] testers = new AsynchTester[versions.size()];
        for (int i = 0; i < versions.size(); i++) {
            testers[i] = new AsynchTester(new Runnable() {

                @Override
                public void run() {
                    File version = versions.remove(0);
                    InformationResourceFileVersion irversion;
                    try {
                        irversion = generateAndStoreVersion(Image.class, version.getName(), version, store);
                        boolean result = fileAnalyzer.processFile(irversion);
                        if (!result) {
                            throw new TdarRecoverableRuntimeException("should not see this, file processing error");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new TdarRecoverableRuntimeException("something happened", e);
                    }
                }
            });
            testers[i].start();
        }

        for (AsynchTester tester : testers)
            if (tester != null) {
                tester.test();
            }
    }
}
