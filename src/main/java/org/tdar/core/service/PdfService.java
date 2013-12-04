package org.tdar.core.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Date;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.PdfDocEncoding;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.core.exception.PdfCoverPageGenerationException;
import org.tdar.utils.AsciiTransliterator;

/*
 * A central service to help handle all PDF creation functions
 */

@Service
public class PdfService implements Serializable {

    private static final long serialVersionUID = 2111947231803271925L;
    private static final String DOT_PDF = ".pdf";
    private static final String COVER_PAGE = "cover_page";
    private static final int LEFT_MARGIN = 73;

    // A simple ENUM to help with the management of Fonts.
    enum FontHelper {
        // confirm correct encoding http://stackoverflow.com/questions/1713751/using-java-pdfbox-library-to-write-russian-pdf
        HELVETICA_EIGHT_POINT(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, new PdfDocEncoding(), 8, 100),
        HELVETICA_TEN_POINT(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, new PdfDocEncoding(), 10, 90),
        HELVETICA_TWELVE_POINT(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, new PdfDocEncoding(), 12, 75),
        HELVETICA_SIXTEEN_POINT(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, new PdfDocEncoding(), 16, 55);

        private int fontSize;
        private int lineHeight;
        private int charsPerLine;
        private PDType1Font font;
        private PDType1Font bold;
        private PDType1Font italic;
        private Encoding encoding;

        private FontHelper(PDType1Font font, PDType1Font boldVariant, PDType1Font italicVariant, Encoding encoding, int size, int charsPerLine) {
            setFont(font);
            setBold(boldVariant);
            setItalic(italicVariant);
            setFontSize(size);
            Float lineHeight_ = (float) size * 1.25f;
            setLineHeight(Math.round(lineHeight_));
            setCharsPerLine(charsPerLine);
            setEncoding(encoding);
        }

        public int getCharsPerLine() {
            return charsPerLine;
        }

        public void setCharsPerLine(int charsPerLine) {
            this.charsPerLine = charsPerLine;
        }

        public int getLineHeight() {
            return lineHeight;
        }

        public void setLineHeight(int lineHeight) {
            this.lineHeight = lineHeight;
        }

        public PDType1Font getFont() {
            return font;
        }

        public void setFont(PDType1Font font) {
            this.font = font;
        }

        public PDType1Font getBold() {
            return bold;
        }

        public void setBold(PDType1Font bold) {
            this.bold = bold;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }

        public Encoding getEncoding() {
            return encoding;
        }

        public void setEncoding(Encoding encoding) {
            this.encoding = encoding;
        }

        public PDType1Font getItalic() {
            return italic;
        }

        public void setItalic(PDType1Font italic) {
            this.italic = italic;
        }
    }

    @Transient
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    FileSystemResourceDao fileDao;

    @Autowired
    private UrlService urlService;

    /*
     * Provided a submitter and a file version, it creates a cover page from the resource, and then combines them
     */
    public File mergeCoverPage(Person submitter, InformationResourceFileVersion version) throws COSVisitorException, IOException, URISyntaxException {
        try {
            InformationResource informationResource = version.getInformationResourceFile().getInformationResource();
            if (version.getExtension().equalsIgnoreCase("PDF") && informationResource instanceof Document) {
                // get the tDAR document and get the path to the template
                Document document = (Document) version.getInformationResourceFile().getInformationResource();
                String path = String.format("%s%s%s", TdarConfiguration.getInstance().getThemeDir(), COVER_PAGE, DOT_PDF);

                // get the template
                File template = fileDao.loadTemplate(path);

                // create the cover page
                template = createCoverPage(submitter, template, document, version.getInformationResourceFile().getDescription());

                // merge the two PDFs

                return mergePDFs(template, TdarConfiguration.getInstance().getFilestore().retrieveFile(version));
            } else {
                throw new PdfCoverPageGenerationException("file type was not valid or file was null");
            }
        } catch (Throwable e) {
            throw new PdfCoverPageGenerationException("could not add Cover Page", e);
        }
    }

    // simply merges the list of files in that order
    private File mergePDFs(File... files) throws IOException, FileNotFoundException, COSVisitorException {
        PDFMergerUtility merger = new PDFMergerUtility();
        File outputFile = File.createTempFile(files[0].getName(), DOT_PDF, TdarConfiguration.getInstance().getTempDirectory());
        merger.setDestinationStream(new FileOutputStream(outputFile));
        for (File file : files) {
            merger.addSource(file);
        }
        merger.mergeDocuments();
        return outputFile;
    }

    // Create the cover pge from the template file and the resource
    private File createCoverPage(Person submitter, File template, Document document, String description) throws IOException, COSVisitorException, FileNotFoundException,
            URISyntaxException {
        PDDocument doc = PDDocument.load(template);
        PDPage page = null;
        for (Object kid : doc.getDocumentCatalog().getPages().getKids()) {
            if (kid instanceof PDPage) {
                page = (PDPage) kid;
                break;
            }
        }

        PDPageContentStream content = new PDPageContentStream(doc, page, true, false);
        int cursorPositionFromBottom = 580;
        /*
         * Title: An Interaction Model for Resource Implement Complexity Based on Risk and Number of Annual Moves
         * Document type: book/report
         * Author(s): Dwight Read
         * Source: (book title, journal, year?) American Antiquity, Vol. 73, No. 4 (Oct., 2008), pp. 599-625
         * Published by: (if applicable)
         * Stable URL: http://core.tdar.org/document/123456 .
         */

        cursorPositionFromBottom = writeOnPage(content, document.getTitle(), FontHelper.HELVETICA_SIXTEEN_POINT, true, LEFT_MARGIN, cursorPositionFromBottom);
        cursorPositionFromBottom = writeOnPage(content, "", FontHelper.HELVETICA_SIXTEEN_POINT, true, LEFT_MARGIN, cursorPositionFromBottom);
        cursorPositionFromBottom = writeLabelPairOnPage(content, "Author(s) / Editor(s): ", document.getFormattedAuthorList(),
                FontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);
        cursorPositionFromBottom = writeLabelPairOnPage(content, "Published: ", document.getFormattedSourceInformation(), FontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);
        cursorPositionFromBottom = writeLabelPairOnPage(content, "Document Type: ", document.getDocumentType().getLabel(), FontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);

        cursorPositionFromBottom = writeLabelPairOnPage(content, "Stable URL: ", urlService.absoluteUrl(document), FontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);

        if (StringUtils.isNotBlank(description)) {
            cursorPositionFromBottom = writeOnPage(content, "", FontHelper.HELVETICA_SIXTEEN_POINT, true, LEFT_MARGIN, cursorPositionFromBottom);
            cursorPositionFromBottom = writeLabelPairOnPage(content, "Note: ", description, FontHelper.HELVETICA_TEN_POINT,
                    LEFT_MARGIN,
                    cursorPositionFromBottom);
        }
        
        String doi = document.getDoi();
        if (StringUtils.isBlank(doi)) {
            doi = document.getExternalId();
        }

        if (StringUtils.isNotBlank(doi)) {
            cursorPositionFromBottom = writeLabelPairOnPage(content, "DOI: ", doi, FontHelper.HELVETICA_TWELVE_POINT, LEFT_MARGIN, cursorPositionFromBottom);

        }
        cursorPositionFromBottom = 200;
        cursorPositionFromBottom = writeLabelPairOnPage(content, "Downloaded: ", "by " + submitter.getProperName() + " on " + new Date(),
                FontHelper.HELVETICA_EIGHT_POINT,
                LEFT_MARGIN, cursorPositionFromBottom);

        content.close();
        File tempFile = File.createTempFile(COVER_PAGE, DOT_PDF, TdarConfiguration.getInstance().getTempDirectory());
        doc.save(new FileOutputStream(tempFile));
        doc.close();
        return tempFile;
    }

    /*
     * adds a field and label in the format to the pdf. you pass the content, the fontHelper obejct, and where to put the page, it'll pass back the new
     * line position for text below that will take into account the single-spacing line height of the text. It will also take care of wrapping of long values
     * but may not take care of wrapping of long labels
     * 
     * Format:
     * <B>Field</B>: Label
     */
    public int writeLabelPairOnPage(PDPageContentStream content, String label, String utf8Text, FontHelper fontHelper, int xFromLeft, int yFromBottom)
            throws IOException {
        if (StringUtils.isBlank(label)) {
            label = "";
        }
        String text = transliterate(utf8Text);
        content.beginText();
        content.setFont(fontHelper.getBold(), fontHelper.getFontSize());
        content.moveTextPositionByAmount(xFromLeft, yFromBottom);// INITIAL POSITION
        content.drawString(label);
        content.setFont(fontHelper.getFont(), fontHelper.getFontSize());

        text = StringUtils.repeat(" ", label.length()) + text; // take into account the label when wrapping
        if (text.length() > fontHelper.charsPerLine) {
            text = WordUtils.wrap(text, fontHelper.getCharsPerLine(), "\r\n", true);
        }
        text = text.trim();

        for (String line : text.split("([\r|\n]+)")) {
            logger.trace(line);
            content.drawString(line);
            yFromBottom -= fontHelper.getLineHeight();
            content.endText();
            content.beginText();
            content.moveTextPositionByAmount(xFromLeft, yFromBottom);// INITIAL POSITION
        }
        content.endText();
        return yFromBottom;
    }

    private String transliterate(String utf8Text) {
        AsciiTransliterator transliterator = new AsciiTransliterator();
        String text = transliterator.process(utf8Text).trim();
        return text;
    }

    /*
     * adds text to the pdf. you pass the content, the fontHelper obejct, and where to put the page, and whether it's bold or not it'll pass back the new
     * line position for text below that will take into account the single-spacing line height of the text. It will also take care of wrapping of long values
     */
    public int writeOnPage(PDPageContentStream content, String utf8Text, FontHelper fontHelper, boolean bold, int xFromLeft, int yFromBottom)
            throws IOException {
        String text = transliterate(utf8Text);
        content.beginText();
        content.moveTextPositionByAmount(xFromLeft, yFromBottom);// INITIAL POSITION

        if (bold) {
            content.setFont(fontHelper.getBold(), fontHelper.getFontSize());
        } else {
            content.setFont(fontHelper.getFont(), fontHelper.getFontSize());
        }

        yFromBottom = writeTextOnPage(content, text, fontHelper, xFromLeft, yFromBottom);
        return yFromBottom;
    }

    /*
     * The actual "write" method for the text, wraps by word ... could be brittle there in that it
     */
    private int writeTextOnPage(PDPageContentStream content, String utf8Text, FontHelper fontHelper, int xFromLeft, int yFromBottom) throws IOException {
        String text = transliterate(utf8Text);

        if (text.length() > fontHelper.charsPerLine) {
            text = WordUtils.wrap(text, fontHelper.getCharsPerLine(), "\r\n", true);
        }
        text = text.trim();

        for (String line : text.split("([\r|\n]+)")) {
            logger.trace(line);
            content.drawString(line);
            yFromBottom -= fontHelper.getLineHeight();
            content.endText();
            content.beginText();
            content.moveTextPositionByAmount(xFromLeft, yFromBottom);// INITIAL POSITION
        }
        content.endText();
        return yFromBottom;
    }
}
