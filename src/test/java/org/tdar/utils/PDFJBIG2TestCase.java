package org.tdar.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDFJBIG2TestCase {

    private final transient Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testJBIG2() throws IOException {
        File pdfFile = new File("src/test/resources/documents/pia-09-lame-1980-small.pdf");
        String imageFormat = "jpg";
        String color = "rgb";
        ImageIO.scanForPlugins();
        int resolution;
        try {
            resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        } catch (HeadlessException e) {
            resolution = 96;
        }

        String fn = pdfFile.getName();
        int pageNum = 1;
        String outputPrefix = fn.substring(0, fn.lastIndexOf('.'));
        outputPrefix = new File(System.getProperty("java.io.tmpdir"), outputPrefix).toString();
        PDDocument document = openPDF("", pdfFile);

        if (document != null) {
            int imageType = determineImageType(color);

            try {
                PDFImageWriter imageWriter = new PDFImageWriter();
                // The following library call will write "Writing: " + the file name to the System.out stream. Naughty!
                // This is fixed in a later version of pdfbox, but we have a transitive dependency via Tika...
                boolean success = imageWriter.writeImage(document, imageFormat, "", pageNum, pageNum, outputPrefix, imageType, resolution);
                if (!success) {
                    log.info("Error: no writer found for image format '" + imageFormat + "'");
                }
            } catch (NullPointerException npe) {
                fail("encountered NPE in proccessing JBIG2 file");
            } catch (Throwable e) {
               log.debug("PDF image extraction failed", e);
            }
        }
        File outputFile = new File(outputPrefix + pageNum + "." + imageFormat);
        log.debug("output file: {} size: {}", outputFile, outputFile.length());
        assertTrue(outputFile.length() > 2);
    }

    private int determineImageType(String color) {
        int imageType = 24;
        if ("bilevel".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_BYTE_BINARY;
        } else if ("indexed".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_BYTE_INDEXED;
        } else if ("gray".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_BYTE_GRAY;
        } else if ("rgb".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_INT_RGB;
        } else if ("rgba".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        } else {
            log.debug("Error: the number of bits per pixel must be 1, 8 or 24.");
        }
        return imageType;
    }
    

    private PDDocument openPDF(String password, File pdfFile) throws IOException  {
        PDDocument document = null;
            document = PDDocument.load(pdfFile);
        return document;
    }

}
