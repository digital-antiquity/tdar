package org.tdar.core.service;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.core.exception.PdfCoverPageGenerationException;
import org.tdar.core.service.pdf.PDFMergeTask;
import org.tdar.core.service.pdf.PDFMergeWrapper;
import org.tdar.core.service.pdf.PdfFontHelper;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.ResourceCitationFormatter;

import com.ibm.icu.text.Transliterator;
import com.opensymphony.xwork2.TextProvider;

/**
 * A centralized service used for the creation and management of PDF documents
 * 
 * @author abrin
 * 
 */

@Service
public class PdfService {

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    private static final int MAX_FILE_DESCRIPTION_LENGTH = 640;
    private static final int MAX_DESCRIPTION_LENGTH = 512;
    private static final String DOT_PDF = ".pdf";
    private static final String COVER_PAGE = "cover_page";
    private static final int LEFT_MARGIN = 73;
    private static final String PDF = "PDF";

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private FileSystemResourceDao fileDao;

    /**
     * Provided a submitter and a file version, it creates a cover page from the resource, and then combines them
     * 
     * @param submitter
     * @param version
     * @param document
     * @return
     * @throws COSVisitorException
     * @throws IOException
     * @throws URISyntaxException
     */
    public InputStream mergeCoverPage(TextProvider provider, Person submitter, InformationResourceFileVersion version, Document document, File coverPage)
            throws PdfCoverPageGenerationException {
        try {
            logger.debug("IR: {}, {} {}", document, version, version.getExtension());
            if (version.getExtension().equalsIgnoreCase("PDF")) {
                // get the tDAR document and get the path to the template
                String path = String.format("%s/%s%s", CONFIG.getThemeDir(), COVER_PAGE, DOT_PDF);

                // get the template
                File template = fileDao.loadTemplate(path);
                if (coverPage != null && coverPage.exists()) {
                    template = coverPage;
                }
                // create the cover page
                String description = version.getInformationResourceFile().getDescription();
                description = truncateDescription(description);
                template = createCoverPage(provider, submitter, template, document, description);

                // merge the two PDFs
                logger.debug("calling merge on: {}", version);
                return mergePDFs(template, CONFIG.getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version), coverPage);
            } else {
                logger.debug("IR: invalid type");
                throw new PdfCoverPageGenerationException("pdfService.file_type_invalid");
            }
        } catch (Throwable e) {
            logger.debug("IR: merge issue", e);
            throw new PdfCoverPageGenerationException("pdfService.could_not_add_cover_page", e);
        }
    }



    private String truncateDescription(String description) {
        if (StringUtils.isNotBlank(description) && description.length() > MAX_FILE_DESCRIPTION_LENGTH) {
            BreakIterator instance = BreakIterator.getWordInstance();
            instance.setText(description);
            int after = instance.following(MAX_DESCRIPTION_LENGTH);
            description = description.substring(0, after) + "...";
        }
        return description;
    }

    

    /**
     * Can the system generate a coverpage for the provided file?  This method is non-deterministic.
     * @param irfv
     * @return
     */
    public boolean coverPageSupported(InformationResourceFileVersion irfv) {
        if (!StringUtils.equalsIgnoreCase(PDF, irfv.getExtension())) {
            return false;
        }
        
        //pdfbox 1.8.x uses at least irfv.fileLength bytes of RAM for merges (even when using scratchdisk)
        long freemem = Runtime.getRuntime().freeMemory();
        boolean enoughRam = freemem >  irfv.getFileLength();
        if(!enoughRam) {
            logger.error("Not enough RAM for coverpage (free:{} needed:{}) ", freemem, irfv.getFileLength());
        }

        return enoughRam;
    }

    /**
     * Merges the list of files in that order.
     * 
     * @param files
     * @return
     * @throws IOException
     * @throws COSVisitorException
     * @throws InterruptedException
     */
    private PipedInputStream mergePDFs(File coverPage, File document, File coverPageImage) throws IOException, COSVisitorException, InterruptedException {
        final PDFMergeWrapper wrapper = new PDFMergeWrapper();

        int downloadBufferSize = CONFIG.getDownloadBufferSize();
        PipedInputStream inputStream = new PipedInputStream(downloadBufferSize);
        final PipedOutputStream pipedOutputStream = new PipedOutputStream(inputStream);
        wrapper.getMerger().setDestinationStream(new BufferedOutputStream(pipedOutputStream,downloadBufferSize));
        wrapper.getMerger().addSource(coverPage);
        wrapper.getMerger().addSource(document);
        wrapper.setDocument(document);

        @SuppressWarnings("unused")
        Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                System.out.println("Uncaught exception: " + ex);
            }
        };
        // Separate thread needed here to call merge
        // FIXME: handle exceptions better
        Thread thread = new Thread(new PDFMergeTask(wrapper, pipedOutputStream));
        thread.start();
        logger.trace("done with PDF Merge"); // fixme: technically the method is done, but really you've just started the merge operation.
        return inputStream;
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
    private File createCoverPage(TextProvider provider, Person submitter, File template, Document document, String description)
            throws IOException,
            COSVisitorException, FileNotFoundException,
            URISyntaxException {
        PDDocument doc = PDDocument.load(template);
        PDPage page = null;
        for (Object kid : doc.getDocumentCatalog().getPages().getKids()) {
            if (kid instanceof PDPage) {
                page = (PDPage) kid;
                break;
            }
        }

        PDPageContentStream content = new PDPageContentStream(doc, page, true, false, true);
        appendCoverPageLogo(doc, content);
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
        ResourceCitationFormatter formatter = new ResourceCitationFormatter(document);
        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.authors"), formatter.getFormattedAuthorList(),
                PdfFontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);
        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.published"), formatter.getFormattedSourceInformation(),
                PdfFontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);
        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.document_type"), document.getDocumentType().getLabel(),
                PdfFontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom);

        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.stable_url"), UrlService.absoluteUrl(document),
                PdfFontHelper.HELVETICA_TWELVE_POINT,
                LEFT_MARGIN,
                cursorPositionFromBottom, true, page);

        String doi = document.getDoi();
        if (StringUtils.isBlank(doi)) {
            doi = document.getExternalId();
        }

        if (StringUtils.isNotBlank(doi)) {
            cursorPositionFromBottom = writeLabelPairOnPage(content, provider.getText("pdfService.doi"), doi, PdfFontHelper.HELVETICA_TWELVE_POINT,
                    LEFT_MARGIN, cursorPositionFromBottom);

        }
        if (StringUtils.isNotBlank(description)) {
            cursorPositionFromBottom = writeOnPage(content, "", PdfFontHelper.HELVETICA_SIXTEEN_POINT, true, LEFT_MARGIN, cursorPositionFromBottom);
            cursorPositionFromBottom = writeLabelPairOnPage(content, "Note: ", description, PdfFontHelper.HELVETICA_TEN_POINT,
                    LEFT_MARGIN,
                    cursorPositionFromBottom);
        }

        cursorPositionFromBottom = 200;
        List<Object> byOn = new ArrayList<>();
        String msg = "pdfService.by_on";
        if (submitter != null) {
            byOn.add(submitter.getProperName());
        } else {
            msg = "pdfService.on";
        }
        byOn.add(new Date());
        cursorPositionFromBottom = writeLabelPairOnPage(content, provider.getText("pdfService.downloaded"), provider.getText(msg, byOn),
                PdfFontHelper.HELVETICA_EIGHT_POINT,
                LEFT_MARGIN, cursorPositionFromBottom);
        content.close();
        File tempFile = File.createTempFile(COVER_PAGE, DOT_PDF, CONFIG.getTempDirectory());
        doc.save(new FileOutputStream(tempFile));
        doc.close();
        return tempFile;
    }



    /**
     *  This is not used, but is here for reference.
     *  NOTE: THIS HAS ISSUES WITH SCREEN vs. Image Resolution and cannot produce the quality acceptable for us
     * @param doc
     * @param content
     * @throws FileNotFoundException
     * @throws IOException
     */
    
    private void appendCoverPageLogo(PDDocument doc, PDPageContentStream content) throws FileNotFoundException, IOException {
        File coverPageLogo = null;
        if (coverPageLogo != null && coverPageLogo.exists()) {
            InputStream in = new FileInputStream(coverPageLogo);

            BufferedImage awtImage = ImageIO.read(in);
            PDXObjectImage img = new PDPixelMap(doc, awtImage);

            int TOP = 646;
            int LEFT = 541 - img.getWidth();
            content.drawXObject(img, LEFT, TOP,awtImage.getWidth(), awtImage.getHeight());
        }
    }

    public int writeLabelPairOnPage(PDPageContentStream content, String label, String utf8Text, PdfFontHelper fontHelper, int xFromLeft, int yFromBottom)
            throws IOException {
        return writeLabelPairOnPage(content, label, utf8Text, fontHelper, xFromLeft, yFromBottom, false, null);
    }

    /**
     * adds a field and label in the format to the pdf. you pass the content, the fontHelper obejct, and where to put the page, it'll pass back the new
     * line position for text below that will take into account the single-spacing line height of the text. It will also take care of wrapping of long values
     * but may not take care of wrapping of long labels
     * 
     * Format:
     * <B>Field</B>: Label
     */
    public int writeLabelPairOnPage(PDPageContentStream content, String label, String utf8Text, PdfFontHelper fontHelper, int xFromLeft, int yFromBottom,
            boolean link, PDPage page)
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

        String line = StringUtils.repeat(" ", label.length()) + text; // take into account the label when wrapping
        int writeTextOnPage = writeTextOnPage(content, line, fontHelper, xFromLeft, yFromBottom);
        if (link) {
            writeLink(fontHelper, xFromLeft + fontHelper.estimateWidth(label), yFromBottom, page, text, writeTextOnPage);
        }
        return writeTextOnPage;
    }

    private void writeLink(PdfFontHelper fontHelper, int xFromLeft, int yFromBottom, PDPage page, String linkText, int writeTextOnPage)
            throws IOException {
        PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
        borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
        borderULine.setWidth(0f);
        PDAnnotationLink txtLink = new PDAnnotationLink();
        txtLink.setBorderStyle(borderULine);
        // add an action
        PDActionURI action = new PDActionURI();
        action.setURI(linkText);
        txtLink.setAction(action);
        // txtLink.setBorderStyle(borderULine);
        PDRectangle position = new PDRectangle();
        position.setLowerLeftX(xFromLeft);
        position.setUpperRightY(yFromBottom + fontHelper.getLineHeight());
        txtLink.setRectangle(position);
        position.setUpperRightX(fontHelper.estimateWidth(linkText) * 2);
        position.setLowerLeftY(yFromBottom);
        page.getAnnotations().add(txtLink);
    }

    /**
     * Strip out diacritics as we're not writing in UTF-8
     * 
     * @param utf8Text
     * @return
     */
    public String transliterate(String utf8Text) {
//        Enumeration<String> availableIDs = Transliterator.getAvailableIDs();
//        while (availableIDs.hasMoreElements()) {
//            String str  = availableIDs.nextElement();
//            logger.debug("{}", str);
//        }
        Transliterator instance = Transliterator.getInstance("Latin-ASCII");
        String text = instance.transliterate(utf8Text);
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
