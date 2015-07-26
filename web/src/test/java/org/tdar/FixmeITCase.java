package org.tdar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.utils.MessageHelper;

@Ignore
public class FixmeITCase extends AbstractAdminControllerITCase {

    @Autowired
    private SerializationService serializationService;

    @Autowired
    private ImportService importService;

    @Test
    @Rollback
    public void testDataset() throws Exception {
        Dataset dataset = setupAndLoadResource(AbstractDataIntegrationTestCase.SPITAL_DB_NAME, Dataset.class);
        Long id = dataset.getId();
        genericService.synchronize();
        Dataset newDoc = importService.cloneResource(dataset, getAdminUser());
        genericService.synchronize();
        logger.debug(serializationService.convertToXML(newDoc));

    }

    @Test
    public void testAvoidDuplicateKeys() {
        MessageHelper freemarkerBundle = new MessageHelper(ResourceBundle.getBundle("Locales/tdar-freemarker-messages"));
        MessageHelper bundle = new MessageHelper(ResourceBundle.getBundle("Locales/tdar-messages"));
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (freemarkerBundle.containsKey(key)) {
                Assert.fail("duplicate key in bundles:" + key);
            }

        }

    }
    
    Map<String, List<String>> matchingMap = new HashMap<>();
    // was LocalizationTestCase
    @Test
    public void testFreemarkerLocaleEntriesHaveValues() throws IOException, ClassNotFoundException {
        Pattern pattern = Pattern.compile(("^.+(\\.?localText|s\\.text)(\\s*(name=)?)\"([^\"]+)\".+"));
        Iterator<File> iterateFiles = FileUtils.iterateFiles(new File("src/main"), new String[] { "ftl", "dec" }, true);
        while (iterateFiles.hasNext()) {
            File file = iterateFiles.next();
//            handleFile(pattern, file);
        }

        List<String> results = new ArrayList<>();
        MessageHelper freemarkerBundle = new MessageHelper(ResourceBundle.getBundle("Locales/tdar-freemarker-messages"));
        MessageHelper bundle = new MessageHelper(ResourceBundle.getBundle("Locales/tdar-messages"));
        for (Entry<String, List<String>> key : matchingMap.entrySet()) {
            if (key.getKey().startsWith("${") || key.getKey().contains("$")) {
                continue;
            }

            if (!bundle.containsKey(key.getKey()) && !freemarkerBundle.containsKey(key.getKey())) {
                String msg = String.format("Locale key is not available in localeFile: %s %s", key.getKey(), key.getValue());
                logger.error(msg);
                results.add(msg);
            }
        }
        if (results.size() > 0) {
            Assert.fail(StringUtils.join(results, "\n"));
        }
    }

}
