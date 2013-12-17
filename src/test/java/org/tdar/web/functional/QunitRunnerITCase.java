package org.tdar.web.functional;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

@RunWith(Parameterized.class)
public class QunitRunnerITCase extends AbstractBasicSeleniumWebITCase {

//    Logger logger = LoggerFactory.getLogger(QunitRunnerITCase.class);
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

        assertEquals(String.format("qunit failures: %s", failures.getText()), 0, failures.size());
    };

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<>();
        File dir = new File("src/main/webapp/test/js");
        for (File file : FileUtils.listFiles(dir, null, true)) {
            String path = file.getPath();
            if (path.contains("tdar.commontest.js")) {
                continue;
            }
            path = path.replace("\\", "/");
            path = path.replace("src/main/webapp/test/js/", "");
            path = path.replace(".qunit.js", "?qunit");
            data.add(new Object[] { "/" + path });
        }
        return data;
    }

}
