package org.tdar.core;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveEvaluator {

    static final Logger logger = LoggerFactory.getLogger(ArchiveEvaluator.class);

    public static void assertArchiveContents(Collection<File> expectedFiles, File archive) throws IOException {
        assertArchiveContents(expectedFiles, archive, true);
    }

    public static void assertArchiveContents(Collection<File> expectedFiles, File archive, boolean strict) throws IOException {

        Map<String, Long> nameSize = unzipArchive(archive);
        List<String> errs = new ArrayList<>();
        for (File expected : expectedFiles) {
            Long size = nameSize.get(expected.getName());
            if (size == null) {
                errs.add("expected file not in archive:" + expected.getName());
                continue;
            }
            // if doing a strict test, assert that file is exactly the same
            if (strict) {
                if (size.longValue() != expected.length()) {
                    errs.add(String.format("%s: item in archive %s does not have same content", size.longValue(), expected));
                }
                // otherwise, just make sure that the actual file is not empty
            } else {
                if (expected.length() > 0) {
                    assertThat(size, greaterThan(0L));
                }
            }
        }
        if (errs.size() > 0) {
            for (String err : errs) {
                logger.error(err);
            }
            fail("problems found in archive:" + archive);
        }
    }

    public static Map<String, Long> unzipArchive(File archive) {
        Map<String, Long> files = new HashMap<>();
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(archive);
            for (Enumeration<?> e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                files.put(entry.getName(), entry.getSize());
                logger.info("{} {}", entry.getName(), entry.getSize());
            }
        } catch (Exception e) {
            logger.error("Error while extracting file " + archive, e);
        } finally {
            if (zipfile != null) {
                IOUtils.closeQuietly(zipfile);
            }
        }
        return files;
    }

}
