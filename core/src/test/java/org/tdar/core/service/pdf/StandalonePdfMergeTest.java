package org.tdar.core.service.pdf;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.junit.Ignore;
import org.junit.Test;


/**
 * This test attempts to recreate the missing page issue per https://issues.tdar.org/browse/TDAR-4906
 *
 * We think this may be a PDFBox-specific bug, so this test attempts to recreate the issue without using any
 * TDAR core libraries.
 *
 * Prerequisites:
 *  - test expects all files to be placed in /tmp
 *  - test expects /tmp/coverpage.pdf & /tmp/document.pdf  (where document.pdf is a pdf that causes tdar to exhibit the buggy
 *      behavior when merged with coverpage.pdf)
 *
 */
@Ignore
public class StandalonePdfMergeTest {

    private static String TMP_PATH = "/tmp";
    private static File TMP_FOLDER = new File(TMP_PATH);

    File coverpagePdf;
    File documentPdf;
    File destinationPdf;

    public StandalonePdfMergeTest() {
        coverpagePdf = new File(TMP_FOLDER, "coverpage.pdf");
        documentPdf = new File(TMP_FOLDER, "document.pdf");
    }


    @Test
    public void sanityCheck() {
        assertThat(TMP_FOLDER.exists(), is(true));
        assertThat(coverpagePdf.exists(), is(true));
        assertThat(documentPdf.exists(), is(true));
    }

    @Test
    public void testMerge() throws IOException, COSVisitorException {
        destinationPdf = new File(TMP_FOLDER, "result.pdf");
        PDFMergerUtility ut = new PDFMergerUtility();
        ut.addSource(coverpagePdf);
        ut.addSource(documentPdf);
        ut.setDestinationFileName(destinationPdf.getCanonicalPath());
        ut.mergeDocuments();

        //not a very robust assertion, but one telltale sign that the bug occurred is if the merger generated a file that is smaller than the original
        assertThat("destination pdf should be larger than the original pdf", destinationPdf.length(), is( greaterThan(documentPdf.length())));
    }

    @Test
    public void testMergeNonSeq() throws IOException, COSVisitorException {
        destinationPdf = new File(TMP_FOLDER, "result-nonseq.pdf");
        PDFMergerUtility ut = new PDFMergerUtility();
        RandomAccess ram = new RandomAccessFile(File.createTempFile("mergeram", ".bin"), "rw");
        ut.addSource(coverpagePdf);
        ut.addSource(documentPdf);
        ut.setDestinationFileName(destinationPdf.getCanonicalPath());
        ut.mergeDocumentsNonSeq(ram);

        //not a very robust assertion, but one telltale sign that the bug occurred is if the merger generated a file that is smaller than the original
        assertThat("destination pdf should be larger than the original pdf", destinationPdf.length(), is( greaterThan(documentPdf.length())));
    }

}
