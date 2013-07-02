package org.tdar.web.functional;

import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QunitRunnerITCase extends AbstractBasicSeleniumWebITCase {

    Logger logger = LoggerFactory.getLogger(QunitRunnerITCase.class);

    public void assertSelector(String selector) {
        if (find(selector).isEmpty()) {
            fail("could not find content on page with selector:" + selector);
        }
    }

    // for now we just start off simple. look for an error conditions and fail() if we detect an error
    public void runQunitPage(String path) {
        gotoPage(path);

        assertSelector("#qunit-fixture");

        // FIXME: this is insufficient because we don't know that this is the very last element it adds to the dom.
        waitFor("#qunit-testresults");

        if (!find(".fail").isEmpty()) {
            fail("your qunit tests failed. sorry");
        }
    };

    @Test
    public void testFileUploadTests() {
        File dir = new File("src/main/webapp/test/js");
        for (File file : FileUtils.listFiles(dir, null, true)) {
            String path = file.getPath();
            path = path.replace("\\", "/");
            path = path.replace("src/main/webapp/test/js/", "");
            path = path.replace(".qunit.js", "?qunit");
            try {
                runQunitPage("/" + path);
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }
        
        runQunitPage("/includes/test/fileupload.test.html");
    }
}
