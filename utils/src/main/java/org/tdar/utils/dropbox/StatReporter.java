package org.tdar.utils.dropbox;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatReporter implements MetadataListener {

    private static final String SEP = "\t";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    Map<String, List<DropboxItemWrapper>> map = new TreeMap<>();

    @Override
    public void consume(DropboxItemWrapper fileWrapper) throws Exception {
        String key = fileWrapper.getFullPath().toLowerCase();
        key = StringUtils.replace(key, "/input/", "/");
        key = StringUtils.replace(key, "/output/", "/");
        key = StringUtils.remove(key, DropboxConstants.CLIENT_DATA.toLowerCase());
        key = StringUtils.substringAfter(key, "/");
        logger.debug(key);
        key = StringUtils.replace(key, "_ocr_pdfa.pdf", ".pdf");
        map.putIfAbsent(key, new ArrayList<>());
        map.get(key).add(fileWrapper);
    }

    public void report() {
        String path1 = "/Client Data/Create PDFA/input/";
        String path2 = "/Client Data/Create PDFA/output/";
        String path3 = "/Client Data/Upload to tDAR/";
        System.out.println("File\tExtension\tDate PDF/a\tWhom\tSize\tDone PDF/a\tWhom\tSize\tUploaded to tDAR\tWhom\tSize");
        for (Entry<String, List<DropboxItemWrapper>> entry : map.entrySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append(entry.getKey()).append(SEP);
            File f = new File(entry.getKey());
            if (f.isFile()) {
            String extension = FilenameUtils.getExtension(f.getName());
                sb.append(extension);
            }
            sb.append(SEP);
            appendIf(entry, path1, sb);
            appendIf(entry, path2, sb);
            appendIf(entry, path3, sb);
            System.out.println(sb.toString());
        }
    }

    private void appendIf(Entry<String, List<DropboxItemWrapper>> entry, final String path1, final StringBuilder sb) {
        boolean seen = false;

        for (DropboxItemWrapper iw : entry.getValue()) {
            if (StringUtils.containsIgnoreCase(iw.getFullPath(), path1)) {
                seen = true;
                append(sb, iw.getModified(), iw.getModifiedBy(), iw.getSize());
            }
        }
        if (!seen) {
            append(sb, null, null, null);
        }
    }

    private void append(StringBuilder sb, DateTime modified, String modifiedBy, Integer size) {
        SimpleDateFormat dateParser = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        sb.append(dateParser.format(modified.toDate())).append(SEP).append(modifiedBy).append(SEP).append(size).append(SEP);

    }
}
