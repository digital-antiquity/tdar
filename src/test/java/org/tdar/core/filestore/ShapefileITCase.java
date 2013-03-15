/**
 * 
 */
package org.tdar.core.filestore;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.tasks.ShapefileReaderTask;

/**
 * @author Adam Brin
 * 
 */
public class ShapefileITCase extends AbstractIntegrationTestCase {

    @Autowired
    private FileAnalyzer fileAnalyzer;

    protected Logger logger = Logger.getLogger(getClass());

    @Test
    @Rollback
    public void testShapefile() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ShapefileReaderTask task = new ShapefileReaderTask();
        task.run();
    }
}
