package org.tdar.web.functional;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

@RunWith(Parameterized.class)
public class QunitRunnerITCase extends AbstractBasicSeleniumWebITCase {

    Logger logger = LoggerFactory.getLogger(QunitRunnerITCase.class);
    String path;

    public QunitRunnerITCase(String path) {
        this.path = path;
    }

    // for now we just start off simple. look for an error conditions and fail() if we detect an error
    @Test
    public void runQunitPage() {
        logger.debug("about to test:{}", path);
        gotoPage(path);

        if (find("#qunit-fixture").isEmpty()) {
            throw new TdarRecoverableRuntimeException("could not find QUnit Fixture on page (#qunit-fixture)");
        }

        // FIXME: this is insufficient because we don't know that this is the very last element it adds to the dom.
        waitFor("#qunit-testresult");
        WebElementSelection result = find("#qunit-testresult");
        logger.debug(result.getText());
        WebElementSelection failures = find(".fail");

        assertEquals("qunit test has failures", 0, failures.size());
    };

//    @Test
//    public void testFileUploadTests() {
//        List<String> failedTests = new ArrayList<>();
//        File dir = new File("src/main/webapp/test/js");
//        for (File file : FileUtils.listFiles(dir, null, true)) {
//            String path = file.getPath();
//            try {
//                path = path.replace("\\", "/");
//                path = path.replace("src/main/webapp/test/js/", "");
//                path = path.replace(".qunit.js", "?qunit");
//                String result =  runQunitPage("/" + path);
//                if (result != null) {
//                    failedTests.add(path + " : " + result);
//                }
//            } catch (Exception e) {
//                logger.error("{}", e);
//                failedTests.add(path + ":" + ExceptionUtils.getFullStackTrace(e));
//            }
//        }
//        if (CollectionUtils.isNotEmpty(failedTests)){
//            fail(StringUtils.join(failedTests.toArray(new String[0])));
//        }
//    }

    @Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<>();
        File dir = new File("src/main/webapp/test/js");
        for (File file : FileUtils.listFiles(dir, null, true)) {
            String path = file.getPath();
            path = path.replace("\\", "/");
            path = path.replace("src/main/webapp/test/js/", "");
            path = path.replace(".qunit.js", "?qunit");
            data.add(new Object[] {path});
        }
        return data;
    }

}
