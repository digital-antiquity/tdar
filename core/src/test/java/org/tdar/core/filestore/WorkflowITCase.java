/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.filestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.GenericService;
import org.tdar.exception.TdarRecoverableRuntimeException;
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
    public void test() throws InterruptedException, FileNotFoundException {
        // List<File>;
        final PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        final List<File> versions = new ArrayList<>();
        versions.add(TestConstants.getFile(TestConstants.TEST_IMAGE_DIR, "/sample_image_formats/grandcanyon.tif"));
        versions.add(TestConstants.getFile(TestConstants.TEST_IMAGE_DIR, "/sample_image_formats/grandcanyon_mac.tif"));
        versions.add(TestConstants.getFile(TestConstants.TEST_IMAGE_DIR, "/5127663428_42ef7f4463_b.jpg"));
        versions.add(TestConstants.getFile(TestConstants.TEST_IMAGE_DIR, "/handbook_of_archaeology.jpg"));

        AsynchTester[] testers = new AsynchTester[versions.size()];
        final FileAnalyzer analyzer = fileAnalyzer;
        final GenericService gs = genericService;
        for (int i = 0; i < versions.size(); i++) {
            final File version = versions.get(i);
            testers[i] = new AsynchTester(new Runnable() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run() {
                    runInNewTransactionWithoutResult(new TransactionCallback<Object>() {
                        @Override
                        public Object doInTransaction(TransactionStatus status) {

                            try {
                                Image doc = generateAndStoreVersion(Image.class, version.getName(), version, store);
                                // irversions.add(irfv);
                                InformationResourceFileVersion irfv = doc.getLatestUploadedVersion();
                                genericService.saveOrUpdate(irfv.getInformationResourceFile());

                                InformationResource ir = irfv.getInformationResourceFile().getInformationResource();
                                ir = gs.merge(ir);
                                analyzer.processFiles(ResourceType.IMAGE, Arrays.asList(irfv), false);
                            } catch (Exception e) {
                                logger.error("exception in workflowITCase", e);
                                throw new TdarRecoverableRuntimeException("something happened", e);
                            }
                            return null;
                        }
                    });

                }
            });
            testers[i].start();
        }

        for (AsynchTester tester : testers) {
            if (tester != null) {
                tester.test();
            }
        }
    }
}
