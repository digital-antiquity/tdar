package org.tdar.utils;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationTestCase {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, List<String>> matchingMap = new HashMap<>();

    @Test
    public void testUniqueSerialVersionIds() throws IOException, ClassNotFoundException {
        Pattern pattern = Pattern.compile(("^.+serialVersionUID\\s*=\\s*(.+)\\s*$"));
        Iterator<File> iterateFiles = FileUtils.iterateFiles(new File("src/main/java"), new String[] { "java" }, true);
        while (iterateFiles.hasNext()) {
            handleFile(pattern, iterateFiles.next());
        }

        String message = null;
        boolean seen = false;
        if (matchingMap.size() > 0) {
            message = "the following classes share the same serialVersionId:\\n";
            for (String key : matchingMap.keySet()) {
                if (matchingMap.get(key).size() > 1) {
                    seen = true;
                    message += String.format(" - %s -- %s\n", key, StringUtils.join(matchingMap.get(key), ", "));
                }
            }
        }
        if (seen) {
            fail(message);
        }
    }

    protected void handleFile(Pattern pattern, File file) throws IOException
    {
        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        try {
            while (it.hasNext()) {
                String line = it.nextLine();
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    logger.trace(line);
                    String key = m.group(1);
                    logger.trace(key + " (" + file.getName() + ")");
                    if (matchingMap.get(key) == null) {
                        matchingMap.put(key, new ArrayList<String>());
                    }
                    matchingMap.get(key).add(file.getAbsolutePath());
                }
            }
        } finally {
            LineIterator.closeQuietly(it);
        }
    }

    protected Collection<? extends String> handleInvalidUse(Pattern pattern, File file) throws IOException
    {
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
