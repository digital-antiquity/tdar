package org.tdar.core.service;

import java.io.BufferedOutputStream;
import java.io.File;
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

import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
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
public class PdfServiceImpl implements PdfService {

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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PdfService#mergeCoverPage(com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.entity.Person,
     * org.tdar.core.bean.resource.file.InformationResourceFileVersion, org.tdar.core.bean.resource.Document, java.io.File)
     */
    @Override
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

    private String truncateDescription(String description_) {
        String description = description_;
        if (StringUtils.isNotBlank(description) && description.length() > MAX_FILE_DESCRIPTION_LENGTH) {
            BreakIterator instance = BreakIterator.getWordInstance();
            instance.setText(description);
            int after = instance.following(MAX_DESCRIPTION_LENGTH);
            description = description.substring(0, after) + "...";
        }
        return description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PdfService#coverPageSupported(org.tdar.core.bean.resource.file.InformationResourceFileVersion)
     */
    @Override
    public boolean coverPageSupported(InformationResourceFileVersion irfv) {
        if (!StringUtils.equalsIgnoreCase(PDF, irfv.getExtension())) {
            return false;
        }

        long freemem = Runtime.getRuntime().freeMemory();
        boolean enoughRam = freemem > irfv.getFileLength() / 2;
        if (!enoughRam) {
            logger.error("Not enough RAM for coverpage (free:{} needed:{}) ", freemem, irfv.getFileLength());
            return false;
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
    private PipedInputStream mergePDFs(File coverPage, File document, File coverPageImage) throws IOException, InterruptedException {
        final PDFMergeWrapper wrapper = new PDFMergeWrapper();

        int downloadBufferSize = CONFIG.getDownloadBufferSize();
        PipedInputStream inputStream = new PipedInputStream(downloadBufferSize);
        final PipedOutputStream pipedOutputStream = new PipedOutputStream(inputStream);
        wrapper.getMerger().setDestinationStream(new BufferedOutputStream(pipedOutputStream, downloadBufferSize));
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
        thread.setName(Thread.currentThread().getName() + "-pdf-merge");
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
            throws IOException, FileNotFoundException, URISyntaxException {
        PDDocument doc = PDDocument.load(template);
        PDPage page = null;
        for (Object kid : doc.getDocumentCatalog().getPages()) {
            if (kid instanceof PDPage) {
                page = (PDPage) kid;
                break;
            }
        }

        PDPageContentStream content = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
        // PDPageContentStream(doc, page, true, false, true);
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

        cursorPositionFromBottom = writeOnPage(content, document.getTitle(), PdfFontHelper.HELVETICA_SIXTEEN_POINT, true, LEFT_MARGIN,
                cursorPositionFromBottom);
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
            cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.doi"), doi, PdfFontHelper.HELVETICA_TWELVE_POINT,
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
        cursorPositionFromBottom = writeLabelPairOnPage(content, MessageHelper.getMessage("pdfService.downloaded"), MessageHelper.getMessage(msg, byOn),
                PdfFontHelper.HELVETICA_EIGHT_POINT,
                LEFT_MARGIN, cursorPositionFromBottom);
        content.close();
        File tempFile = File.createTempFile(COVER_PAGE, DOT_PDF, CONFIG.getTempDirectory());
        doc.save(new FileOutputStream(tempFile));
        doc.close();
        return tempFile;
    }

    /**
     * This is not used, but is here for reference.
     * NOTE: THIS HAS ISSUES WITH SCREEN vs. Image Resolution and cannot produce the quality acceptable for us
     * 
     * @param doc
     * @param content
     * @throws FileNotFoundException
     * @throws IOException
     */

    @SuppressWarnings("unused")
    private void appendCoverPageLogo(PDDocument doc, PDPageContentStream content) throws FileNotFoundException, IOException {
        File coverPageLogo = null;
        if (coverPageLogo != null && coverPageLogo.exists()) {
            // InputStream in = new FileInputStream(coverPageLogo);
            // BufferedImage awtImage = ImageIO.read(in);
            // IOUtils.closeQuietly(in);
            PDImageXObject img = PDImageXObject.createFromFile(coverPageLogo.getAbsolutePath(), doc);

            int TOP = 646;
            int LEFT = 541 - img.getWidth();
            content.drawImage(img, LEFT, TOP, img.getWidth(), img.getHeight());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PdfService#writeLabelPairOnPage(org.apache.pdfbox.pdmodel.PDPageContentStream, java.lang.String, java.lang.String,
     * org.tdar.core.service.pdf.PdfFontHelper, int, int)
     */
    @Override
    public int writeLabelPairOnPage(PDPageContentStream content, String label, String utf8Text, PdfFontHelper fontHelper, int xFromLeft, int yFromBottom)
            throws IOException {
        return writeLabelPairOnPage(content, label, utf8Text, fontHelper, xFromLeft, yFromBottom, false, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PdfService#writeLabelPairOnPage(org.apache.pdfbox.pdmodel.PDPageContentStream, java.lang.String, java.lang.String,
     * org.tdar.core.service.pdf.PdfFontHelper, int, int, boolean, org.apache.pdfbox.pdmodel.PDPage)
     */
    @Override
    public int writeLabelPairOnPage(PDPageContentStream content, String label_, String utf8Text, PdfFontHelper fontHelper, int xFromLeft, int yFromBottom,
            boolean link, PDPage page)
            throws IOException {
        String label = label_;
        if (StringUtils.isBlank(label)) {
            label = "";
        }
        String text = transliterate(utf8Text);

        content.beginText();
        content.setFont(fontHelper.getBold(), fontHelper.getFontSize());
        content.newLineAtOffset(xFromLeft, yFromBottom);// INITIAL POSITION
        content.showText(label);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PdfService#transliterate(java.lang.String)
     */
    @Override
    public String transliterate(String utf8Text) {
        // Enumeration<String> availableIDs = Transliterator.getAvailableIDs();
        // while (availableIDs.hasMoreElements()) {
        // String str = availableIDs.nextElement();
        // logger.debug("{}", str);
        // }
        Transliterator instance = Transliterator.getInstance("Latin-ASCII");
        String text = instance.transliterate(utf8Text);
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PdfService#writeOnPage(org.apache.pdfbox.pdmodel.PDPageContentStream, java.lang.String,
     * org.tdar.core.service.pdf.PdfFontHelper, boolean, int, int)
     */
    @Override
    public int writeOnPage(PDPageContentStream content, String utf8Text, PdfFontHelper fontHelper, boolean bold, int xFromLeft, int yFromBottom_)
            throws IOException {
        int yFromBottom = yFromBottom_;
        String text = transliterate(utf8Text);
        content.beginText();
        content.newLineAtOffset(xFromLeft, yFromBottom);// INITIAL POSITION

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
    private int writeTextOnPage(PDPageContentStream content, String utf8Text, PdfFontHelper fontHelper, int xFromLeft, int yFromBottom_) throws IOException {
        String text = transliterate(utf8Text);

        if (text.length() > fontHelper.getCharsPerLine()) {
            text = WordUtils.wrap(text, fontHelper.getCharsPerLine(), "\r\n", true);
        }
        text = text.trim();
        int yFromBottom = yFromBottom_;

        for (String line : text.split("([\r|\n]+)")) {
            logger.trace(line);
            content.showText(line);
            yFromBottom -= fontHelper.getLineHeight();
            content.endText();
            content.beginText();
            content.newLineAtOffset(xFromLeft, yFromBottom);// INITIAL POSITION
        }
        content.endText();
        return yFromBottom;
    }
}
