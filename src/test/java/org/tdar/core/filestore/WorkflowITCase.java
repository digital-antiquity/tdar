/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.filestore;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
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
        versions.add(new File(TestConstants.TEST_DOCUMENT_DIR, "pia-09-lame-1980.pdf"));
        versions.add(new File(TestConstants.TEST_DOCUMENT_DIR, "schoenwetter1964a.pdf"));
        versions.add(new File(TestConstants.TEST_DOCUMENT_DIR, "pia-09-lame-1980.pdf"));

        try {
            AsynchTester[] testers = new AsynchTester[versions.size()];
            for (int i = 0; i < versions.size(); i++) {
                testers[i] = new AsynchTester(new Runnable() {

                    @Override
                    public void run() {
                        File version = versions.remove(0);
                        InformationResourceFileVersion irversion;
                        try {
                            irversion = generateAndStoreVersion(Document.class, version.getName(), version, store);
                            InformationResource informationResource = irversion.getInformationResourceFile().getInformationResource();
                            boolean result = fileAnalyzer.processFile(irversion);
                            if (!result) {
                                throw new TdarRecoverableRuntimeException("some error happend in processing");
                            }
                            informationResource.markUpdated(getAdminUser());
                            genericService.saveOrUpdate(informationResource);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.err.println("SOMETHING REALLY BAD HPND:" + e);
                            throw new TdarRecoverableRuntimeException("something happened", e);
                        }
                    }
                });
                testers[i].start();
            }

            for (AsynchTester tester : testers) {
                if (tester != null) {
                    tester.test();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }
}
