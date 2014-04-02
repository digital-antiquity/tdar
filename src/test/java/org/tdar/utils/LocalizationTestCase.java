package org.tdar.utils;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.tdar.core.exception.LocalizableException;
import org.tdar.core.service.ReflectionService;

public class LocalizationTestCase {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String exceptionRegex = "";
    private Map<String, List<String>> matchingMap = new HashMap<>();

    @Test
    public void testJavaLocaleEntriesHaveValues() throws IOException, ClassNotFoundException {
        Set<BeanDefinition> findClassesThatImplement = ReflectionService.findClassesThatImplement(LocalizableException.class);
        for (BeanDefinition bean : findClassesThatImplement) {
            Class<?> cls = Class.forName(bean.getBeanClassName());
            if (exceptionRegex.length() > 0) {
                exceptionRegex += "|";
            }
            exceptionRegex += cls.getSimpleName();
        }
        logger.debug(exceptionRegex);
        Pattern pattern = Pattern.compile(("^.+((get(Text|Message))|" + exceptionRegex + ")\\(\\s*\"([^\"]+)\".+"));
        Iterator<File> iterateFiles = FileUtils.iterateFiles(new File("src/main/java"), new String[] { "java" }, true);
        while (iterateFiles.hasNext()) {
            handleFile(pattern, iterateFiles.next());
        }

        List<String> results = new ArrayList<>();
        for (Entry<String, List<String>> key : matchingMap.entrySet()) {
            if (key.getKey().equals("advancedSearchController.")) {
                continue;
            }
            if (!MessageHelper.checkKey(key.getKey())) {
                String msg = String.format("Locale key is not available in localeFile: %s %s", key.getKey(), key.getValue());
                logger.error(msg);
                results.add(msg);
            }
        }
        if (results.size() > 0) {
            fail(StringUtils.join(results, "\n"));
        }
    }

    @Test
    public void testFreemarkerLocaleEntriesHaveValues() throws IOException, ClassNotFoundException {
        Pattern pattern = Pattern.compile(("^.+(\\.?localText|s\\.text)(\\s*(name=)?)\"([^\"]+)\".+"));
        Iterator<File> iterateFiles = FileUtils.iterateFiles(new File("src/main"), new String[] { "ftl", "dec" }, true);
        while (iterateFiles.hasNext()) {
            File file = iterateFiles.next();
            handleFile(pattern, file);
        }

        List<String> results = new ArrayList<>();
        MessageHelper freemarkerBundle = new MessageHelper(ResourceBundle.getBundle("Locales/tdar-freemarker-messages"));
        MessageHelper bundle = new MessageHelper(ResourceBundle.getBundle("Locales/tdar-messages"));
        for (Entry<String, List<String>> key : matchingMap.entrySet()) {
            if (key.getKey().startsWith("${")) {
                continue;
            }

            if (!bundle.containsKey(key.getKey()) && !freemarkerBundle.containsKey(key.getKey())) {
                String msg = String.format("Locale key is not available in localeFile: %s %s", key.getKey(), key.getValue());
                logger.error(msg);
                results.add(msg);
            }
        }
        if (results.size() > 0) {
            fail(StringUtils.join(results, "\n"));
        }
    }

    protected void handleFile(Pattern pattern, File file) throws IOException
    {
        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        try {
            int lineNum = 0;
            while (it.hasNext()) {
                lineNum++;
                String line = it.nextLine();
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    logger.trace(line);
                    String key = m.group(4);
                    logger.debug(key);
                    if (matchingMap.get(key) == null) {
                        matchingMap.put(key, new ArrayList<String>());
                    }
                    matchingMap.get(key).add(file.getAbsolutePath() + ":" + lineNum);
                }
            }
        } finally {
            LineIterator.closeQuietly(it);
        }
    }
}
