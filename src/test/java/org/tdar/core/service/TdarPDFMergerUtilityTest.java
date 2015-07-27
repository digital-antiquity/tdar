package org.tdar.core.service;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessFile;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import  org.tdar.TestConstants;
import org.tdar.core.service.pdf.TdarPDFMergerUtility;

/**
 * Created by jimdevos on 6/30/15.
 */
public class TdarPDFMergerUtilityTest {



    public static File DOCUMENT_DIR = new File(TestConstants.TEST_DOCUMENT_DIR);
    public static File OUTPUT_DIR = new File("target/tmp/pdfmerge-output");

    private Logger logger = LoggerFactory.getLogger(getClass());
    private TdarPDFMergerUtility merger = new TdarPDFMergerUtility();
    private File scratchFile = new File(OUTPUT_DIR, "scratchfile.bin");


    @BeforeClass
    public static void prepwork() {
        OUTPUT_DIR.mkdirs();
    }

    @Before
    public void setup() throws IOException {
        FileUtils.deleteQuietly(scratchFile);
        scratchFile.createNewFile();
    }

    @After
    public void cleanup() {
    }

    @Test
    public void testMergeDocumentsNonSeq() throws IOException, COSVisitorException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(scratchFile, "rw");

        File outputFile = new File(OUTPUT_DIR, "manypdfs.pdf");
        if(outputFile.exists()) {
            outputFile.createNewFile();
        }
        FileOutputStream fop = new FileOutputStream(outputFile);
        File pdf = new File(DOCUMENT_DIR, "a2-15.pdf");
        merger.setDestinationStream(fop);
        merger.addSource(pdf);
        merger.addSource(pdf);
        merger.addSource(pdf);

        logger.debug("done with appending");
        merger.mergeDocumentsNonSeq(randomAccessFile);

        assertThat("output file exists",  outputFile.exists(), is( true));
        assertThat("output file is reasonable size", outputFile.length(), is( greaterThan( pdf.length())));
        assertThat("scratch file is reasonable size", scratchFile.length(), is( greaterThan( pdf.length())));
    }

}
