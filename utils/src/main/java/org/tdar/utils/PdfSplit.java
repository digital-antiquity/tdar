package org.tdar.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfSplit {
    private static final String BAR = "  ----------------  ";

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private File file;

    public PdfSplit(File file) {
        this.file = file;
    }

    public static void main(String[] args) throws IOException {
        PdfSplit split = new PdfSplit(new File(args[0]));
        split.exec();
    }

    Integer num = 0;
    PDDocument samplePdf = new PDDocument();

    private void exec() throws IOException {
        PDDocument document = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();

        document.getPages().forEach(page -> {
            try {
                logger.debug(BAR + num + BAR);
                stripper.setStartPage(num + 1);
                stripper.setEndPage(num + 1);
                String text = stripper.getText(document);
                 logger.debug(text);
                if (StringUtils.startsWith(StringUtils.trim(text), "FHR-") || StringUtils.contains(text,"OAHP INVENTORY")) {
                    logger.debug("SPLIT!!!!");
                    close();

                    samplePdf = new PDDocument();
                } 
                samplePdf.importPage(page);
                
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            num = num + 1;
        });
        close();
    }

    private void close() throws FileNotFoundException, IOException {
        File output = new File(num  + ".pdf");
        FileOutputStream out = new FileOutputStream(output);
        samplePdf.save(out);

        samplePdf.close();
    }

}
