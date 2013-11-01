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
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
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
import org.tdar.utils.MessageHelper;

/**
 * A centralized service used for the creation and management of PDF documents
 * 
 * @author abrin
 *
 */

@Service
public class PdfService implements Serializable {

    private static final long serialVersionUID = 2111947231803271925L;
    private static final String DOT_PDF = ".pdf";
    private static final String COVER_PAGE = "cover_page";
    private static final int LEFT_MARGIN = 73;

    @Transient
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    FileSystemResourceDao fileDao;

    @Autowired
    private UrlService urlService;

    /**
     * Provided a submitter and a file version, it creates a cover page from the resource, and then combines them
     * 
     * @param submitter
     * @param version
     * @return
     * @throws COSVisitorException
     * @throws IOException
     * @throws URISyntaxException
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
                template = createCoverPage(submitter, template, document);

                // merge the two PDFs

                return mergePDFs(template, TdarConfiguration.getInstance().getFilestore().retrieveFile(version));
            } else {
                throw new PdfCoverPageGenerationException(MessageHelper.getMessage("pdfService.file_type_invalid"));
            }
        } catch (Throwable e) {
            throw new PdfCoverPageGenerationException(MessageHelper.getMessage("pdfService.could_not_add_cover_page"), e);
        }
    }

    /**
     * Merges the list of files in that order. 
     * 
     * @param files
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws COSVisitorException
     */
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

    /**
     * Create the cover page from the template file and the @link resource provided
     * 
     * @param submitter
     * @param template
     * @param document
     * @return
     * @throws IOException
     * @throws COSVisitorException
     * @throws FileNotFoundException
     * @throws URISyntaxException
     */
    private File createCoverPage(Person submitter, File template, Document document) throws IOException, COSVisitorException, FileNotFoundException,
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

        cursorPositionFromBottom = writeOnPage(content, document.getTitle(), PdfFontHelper.HELVETICA_SIXTEEN_POINT, true, LEFT_MARGIN, cursorPositionFromBottom);
        cursorPositionFromBottom = writeOnPage(content, "", PdfFontHelper.HELVETICA_SIXTEEN_POINT, true, LEFT_MARGIN, cursorPositionFromBottom);
        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.authors"), document.getFormattedAuthorList(),
                PdfFontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);
        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.published"), document.getFormattedSourceInformation(), PdfFontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);
        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.document_type"), document.getDocumentType().getLabel(), PdfFontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);

        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.stable_url"), urlService.absoluteUrl(document), PdfFontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);
        String doi = document.getDoi();
        if (StringUtils.isBlank(doi)) {
            doi = document.getExternalId();
        }

        if (StringUtils.isNotBlank(doi)) {
            cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.doi"), doi, PdfFontHelper.HELVETICA_TWELVE_POINT, LEFT_MARGIN, cursorPositionFromBottom);

        }
        cursorPositionFromBottom = 200;
        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.downloaded"), MessageHelper.getMessage("pdfService.by_on",submitter.getProperName() , new Date()),
                PdfFontHelper.HELVETICA_EIGHT_POINT,
                LEFT_MARGIN, cursorPositionFromBottom);

        content.close();
        File tempFile = File.createTempFile(COVER_PAGE, DOT_PDF, TdarConfiguration.getInstance().getTempDirectory());
        doc.save(new FileOutputStream(tempFile));
        doc.close();
        return tempFile;
    }

    /**
     * adds a field and label in the format to the pdf. you pass the content, the fontHelper obejct, and where to put the page, it'll pass back the new
     * line position for text below that will take into account the single-spacing line height of the text. It will also take care of wrapping of long values
     * but may not take care of wrapping of long labels
     * 
     * Format:
     * <B>Field</B>: Label
     */
    public int writeLabelPairOnPage(PDPageContentStream content, String label, String utf8Text, PdfFontHelper fontHelper, int xFromLeft, int yFromBottom)
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
        if (text.length() > fontHelper.getCharsPerLine()) {
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

    /**
     * Strip out diacritics as we're not writing in UTF-8
     * 
     * @param utf8Text
     * @return
     */
    private String transliterate(String utf8Text) {
        AsciiTransliterator transliterator = new AsciiTransliterator();
        String text = transliterator.process(utf8Text).trim();
        return text;
    }

    /**
     * adds text to the pdf. you pass the content, the fontHelper obejct, and where to put the page, and whether it's bold or not it'll pass back the new
     * line position for text below that will take into account the single-spacing line height of the text. It will also take care of wrapping of long values
     * 
     * @param content
     * @param utf8Text
     * @param fontHelper
     * @param bold
     * @param xFromLeft
     * @param yFromBottom
     * @return
     * @throws IOException
     */
    public int writeOnPage(PDPageContentStream content, String utf8Text, PdfFontHelper fontHelper, boolean bold, int xFromLeft, int yFromBottom)
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

    /**
     * The actual "write" method for the text, wraps by word ... could be brittle there in that it
     * 
     * @param content
     * @param utf8Text
     * @param fontHelper
     * @param xFromLeft
     * @param yFromBottom
     * @return
     * @throws IOException
     */
    private int writeTextOnPage(PDPageContentStream content, String utf8Text, PdfFontHelper fontHelper, int xFromLeft, int yFromBottom) throws IOException {
        String text = transliterate(utf8Text);

        if (text.length() > fontHelper.getCharsPerLine()) {
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
