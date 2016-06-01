package org.tdar.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;

public class PDFJBIG2TestCase {

    private final transient Logger log = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unused")
    @Test
//    @Ignore("test for PDFBox issue, not tDAR issue")
    public void testJBIG2() throws IOException {
        File pdfFile = new File(TestConstants.TEST_ROOT_DIR + "/documents/pia-09-lame-1980-small.pdf");
        String imageFormat = "jpg";
        ImageIO.scanForPlugins();
        int resolution;
        try {
            resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        } catch (HeadlessException e) {
            resolution = 96;
        }

        String fn = pdfFile.getName();
        int pageNum = 1;

        Iterator<ImageWriter> ir = ImageIO.getImageWritersByFormatName("jpeg");
        while (ir.hasNext()) {
            ImageWriter w = ir.next();
            ImageWriteParam writerParams = w.getDefaultWriteParam();
            ImageTypeSpecifier type = writerParams.getDestinationType();

            log.debug("writer: {}", w);
            // if (w.getClass().getName().contains("CLibJPEGImageWriter")) {
            // ir.remove();
            // }
            // log.debug("getDefaultImageMetadata():  {}", w.getDefaultImageMetadata(type, writerParams));
        }

        String outputPrefix = fn.substring(0, fn.lastIndexOf('.'));
        outputPrefix = new File(System.getProperty("java.io.tmpdir"), outputPrefix).toString();
        PDDocument document = openPDF("", pdfFile);

        File outputFile = new File(outputPrefix + pageNum + "." + imageFormat);
        if (document != null) {

            ImageType color = ImageType.RGB;
            try {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNum, 300, color);
                boolean success = ImageIOUtil.writeImage(bim, outputFile.getAbsolutePath(), 300);
                if (!success) {
                    log.info("Error: no writer found for image format '" + imageFormat + "'");
                }
            } catch (NullPointerException npe) {
                log.error("encountered NPE", npe);
                fail("encountered NPE in proccessing JBIG2 file");
            } catch (Throwable e) {
                log.debug("PDF image extraction failed", e);
            }
        }
        log.debug("output file: {} size: {}", outputFile, outputFile.length());
        assertTrue(outputFile.length() > 2);
    }


    private PDDocument openPDF(String password, File pdfFile) throws IOException {
        PDDocument document = null;
        document = PDDocument.load(pdfFile);
        return document;
    }

}
