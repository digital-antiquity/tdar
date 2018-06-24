package org.tdar.fileprocessing;

import java.io.File;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.tdar.configuration.TdarConfiguration;

public class PdfConfig {

    public static MemoryUsageSetting getPDFMemoryReadSetting() {
        return MemoryUsageSetting.setupTempFileOnly();
    }

    public static MemoryUsageSetting getPDFMemoryWriteSetting(File file) {
        if (TdarConfiguration.getInstance().shouldUseLowMemoryPDFMerger()) {
            return MemoryUsageSetting.setupMixed(Runtime.getRuntime().freeMemory() / 5L);
        } else {
            return MemoryUsageSetting.setupMainMemoryOnly();
        }
    }

}
