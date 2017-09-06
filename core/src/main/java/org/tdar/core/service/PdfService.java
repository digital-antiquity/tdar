package org.tdar.core.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.exception.PdfCoverPageGenerationException;
import org.tdar.core.service.pdf.PdfFontHelper;

import com.opensymphony.xwork2.TextProvider;

public interface PdfService {

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
    InputStream mergeCoverPage(TextProvider provider, Person submitter, InformationResourceFileVersion version, Document document, File coverPage)
            throws PdfCoverPageGenerationException;

    /**
     * Can the system generate a coverpage for the provided file?  This method is non-deterministic.
     * @param irfv
     * @return
     */
    boolean coverPageSupported(InformationResourceFileVersion irfv);

    int writeLabelPairOnPage(PDPageContentStream content, String label, String utf8Text, PdfFontHelper fontHelper, int xFromLeft, int yFromBottom)
            throws IOException;

    /**
     * adds a field and label in the format to the pdf. you pass the content, the fontHelper obejct, and where to put the page, it'll pass back the new
     * line position for text below that will take into account the single-spacing line height of the text. It will also take care of wrapping of long values
     * but may not take care of wrapping of long labels
     * 
     * Format:
     * <B>Field</B>: Label
     */
    int writeLabelPairOnPage(PDPageContentStream content, String label_, String utf8Text, PdfFontHelper fontHelper, int xFromLeft, int yFromBottom,
            boolean link, PDPage page)
            throws IOException;

    /**
     * Strip out diacritics as we're not writing in UTF-8
     * 
     * @param utf8Text
     * @return
     */
    String transliterate(String utf8Text);

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
    int writeOnPage(PDPageContentStream content, String utf8Text, PdfFontHelper fontHelper, boolean bold, int xFromLeft, int yFromBottom_)
            throws IOException;

}