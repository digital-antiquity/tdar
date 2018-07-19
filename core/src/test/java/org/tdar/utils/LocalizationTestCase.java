package org.tdar.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Localizable;
import org.tdar.core.exception.LocalizableException;
import org.tdar.core.service.ReflectionHelper;

public class LocalizationTestCase {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String exceptionRegex = "";
    private Map<String, List<String>> matchingMap = new HashMap<>();

    @Test
    public void testTDarId() {
        String msg = MessageHelper.getMessage("dataIntegrationService.generated_coding_sheet_description",
                Arrays.asList("TDAR", "id", "my fancy dataset", 12340005, new Date()));
        assertThat(msg, containsString("(12340005)"));
    }

    @Test
    public void testLocalization() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Set<Class<? extends Localizable>> findClassesThatImplement = ReflectionHelper.findClassesThatImplement(Localizable.class);
        Set<String> badKeys = new HashSet<>();
        for (Class<? extends Localizable> cls : findClassesThatImplement) {
            logger.debug("{} {}", cls, cls.getEnumConstants());
            for (Object obj : cls.getEnumConstants()) {
                String key = String.format("%s.%s", cls.getSimpleName(), obj);
                logger.trace(key);
                if (!MessageHelper.checkKey(key)) {
                    badKeys.add(key);
                }
            }
        }
        if (!CollectionUtils.isEmpty(badKeys)) {
            for (String key : badKeys) {
                logger.error("no localization: " + key);
            }
            fail("missing localization keys: " + badKeys);
        }
    }

    @Test
    public void testJavaLocaleEntriesHaveValues() throws IOException, ClassNotFoundException {
        Set<Class<? extends LocalizableException>> findClassesThatImplement = ReflectionHelper.findClassesThatImplement(LocalizableException.class);
        for (Class<? extends LocalizableException> cls : findClassesThatImplement) {
            if (exceptionRegex.length() > 0) {
                exceptionRegex += "|";
            }
            exceptionRegex += cls.getSimpleName();
        }
        logger.debug(exceptionRegex);
        Pattern pattern = Pattern.compile(("^.+((get(Text|Message))|" + exceptionRegex + ")\\(\\s*\"([^\"]+)\".+"));
        Iterator<File> iterateFiles = FileUtils.iterateFiles(new File("src/main/java"), new String[] { "java" }, true);
        Iterator<File> iterateFiles2 = FileUtils.iterateFiles(new File("src/main/java"), new String[] { "java" }, true);
        Pattern pattern2 = Pattern.compile("^.+(add(ActionError|ActionMessage)(\\w*))\\(\\s*\\\"([^\"]+)\\\"\\.*\\).+");
        // FIXME: add support for addFieldError
        List<String> results = new ArrayList<>();
        while (iterateFiles.hasNext()) {
            handleFile(pattern, iterateFiles.next());
            results.addAll(handleInvalidUse(pattern2, iterateFiles2.next()));
        }

        for (Entry<String, List<String>> key : matchingMap.entrySet()) {
            if (key.getKey().equals("advancedSearchController.") || key.getKey().equals("searchParameters.")) {
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
    public void testStringFormatErrors() throws IOException, ClassNotFoundException {
        Pattern pattern = Pattern.compile((".*\\%(\\w|\\$).*"));
        searchPropsFileForPattern(pattern);

        List<String> results = new ArrayList<>();
        for (Entry<String, List<String>> key : matchingMap.entrySet()) {
            String msg = String.format("Locale key using wrong format: %s %s", key.getKey(), key.getValue());
            logger.error(msg);
            results.add(msg);
        }
        if (results.size() > 0) {
            fail(StringUtils.join(results, "\n"));
        }
    }

    @Test
    public void testNonEscapedApostrophes() throws IOException, ClassNotFoundException {

        Iterator<File> iterateFiles = FileUtils.iterateFiles(new File("../locales/src/main/resources/Locales"), new String[] { "properties" }, true);
        while (iterateFiles.hasNext()) {
            File file = iterateFiles.next();
            LineIterator it = FileUtils.lineIterator(file, "UTF-8");
            try {
                int lineNum = 0;
                while (it.hasNext()) {
                    lineNum++;
                    String line = it.nextLine();
                    if (line.contains("'") && !line.contains("''")) {
                        matchingMap.put(line, Arrays.asList(Integer.toString(lineNum)));
                    }
                }
            } catch (Throwable t) {
                LineIterator.closeQuietly(it);
            }
        }
        List<String> results = new ArrayList<>();
        for (Entry<String, List<String>> key : matchingMap.entrySet()) {
            String msg = String.format("Locale key using wrong format: %s line: %s", key.getKey(), key.getValue());
            logger.error(msg);
            results.add(msg);
        }

        if (results.size() > 0) {
            fail("Use two '' instead of ' to escape\n" + StringUtils.join(results, "\n"));
        }
    }

    private void searchPropsFileForPattern(Pattern pattern) throws IOException {
        Iterator<File> iterateFiles = FileUtils.iterateFiles(new File("../locales/src/main/resources/Locales"), new String[] { "properties" }, true);
        while (iterateFiles.hasNext()) {
            File file = iterateFiles.next();
            LineIterator it = FileUtils.lineIterator(file, "UTF-8");
            try {
                int lineNum = 0;
                while (it.hasNext()) {
                    lineNum++;
                    String line = it.nextLine();
                    Matcher m = pattern.matcher(line);
                    if (m.matches()) {
                        logger.trace(line);
                        String key = m.group(0);
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

    @Test
    public void testAvoidDuplicateKeys() {
        MessageHelper freemarkerBundle = new MessageHelper("Locales/tdar-freemarker-messages");
        MessageHelper bundle = new MessageHelper("Locales/tdar-messages");
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (freemarkerBundle.containsKey(key)) {
                fail("duplicate key in bundles:" + key);
            }

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
        MessageHelper freemarkerBundle = new MessageHelper("Locales/tdar-freemarker-messages");
        MessageHelper bundle = new MessageHelper("Locales/tdar-messages");
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
            fail(StringUtils.join(results, "\n"));
        }
    }

    protected void handleFile(Pattern pattern, File file) throws IOException {
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
                    logger.trace(key);
                    if (line.trim().startsWith("//")) {
                        logger.debug("ignoring comments: {}", line);
                        continue;
                    }
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

    protected Collection<? extends String> handleInvalidUse(Pattern pattern, File file) throws IOException {
        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        List<String> results = new ArrayList<>();
        try {
            int lineNum = 0;
            while (it.hasNext()) {
                lineNum++;
                String line = it.nextLine();
                Matcher m = pattern.matcher(line);
                String messageResponse = file.getAbsolutePath() + ":" + lineNum;
                if (line.trim().startsWith("//")) {
                    continue;
                }
                if (m.matches()) {
                    String key = m.group(4);
                    if (key.contains(" ")) {
                        String txt = String.format("%s(\"%s\") needs transalation prior to call %s", m.group(1), m.group(4), messageResponse);
                        results.add(txt);
                        logger.error(txt);
                    }
                    logger.trace(line);
                    logger.trace(key);
                    if (matchingMap.get(key) == null) {
                        matchingMap.put(key, new ArrayList<String>());
                    }
                    matchingMap.get(key).add(messageResponse);
                }
            }
        } finally {
            LineIterator.closeQuietly(it);
        }
        return results;
    }
}
