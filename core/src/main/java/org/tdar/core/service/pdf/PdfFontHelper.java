package org.tdar.core.service.pdf;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.font.encoding.StandardEncoding;

/**
 * An enum for managing PDF Fonts; each entry tracks a different font style for us.
 * 
 * @author abrin
 * 
 */
public enum PdfFontHelper {
    // confirm correct encoding http://stackoverflow.com/questions/1713751/using-java-pdfbox-library-to-write-russian-pdf
    HELVETICA_EIGHT_POINT(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, StandardEncoding.INSTANCE, 8, 100),
    HELVETICA_TEN_POINT(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, StandardEncoding.INSTANCE, 10, 90),
    HELVETICA_TWELVE_POINT(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, StandardEncoding.INSTANCE, 12, 75),
    HELVETICA_SIXTEEN_POINT(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, StandardEncoding.INSTANCE, 16, 55);

    private int fontSize;
    private int lineHeight;
    private int charsPerLine;
    private PDType1Font font;
    private PDType1Font bold;
    private PDType1Font italic;
    private Encoding encoding;

    private PdfFontHelper(PDType1Font font, PDType1Font boldVariant, PDType1Font italicVariant, Encoding encoding, int size, int charsPerLine) {
        setFont(font);

        setBold(boldVariant);
        setItalic(italicVariant);
        setFontSize(size);
        Float lineHeight_ = size * 1.25f;
        setLineHeight(Math.round(lineHeight_));
        setCharsPerLine(charsPerLine);
        setEncoding(encoding);
    }

    public int getCharsPerLine() {
        return charsPerLine;
    }

    private void setCharsPerLine(int charsPerLine) {
        this.charsPerLine = charsPerLine;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    private void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public PDType1Font getFont() {
        return font;
    }

    private void setFont(PDType1Font font) {
        this.font = font;
    }

    public PDType1Font getBold() {
        return bold;
    }

    private void setBold(PDType1Font bold) {
        this.bold = bold;
    }

    public int getFontSize() {
        return fontSize;
    }

    private void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    private void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public PDType1Font getItalic() {
        return italic;
    }

    private void setItalic(PDType1Font italic) {
        this.italic = italic;
    }

    public int estimateWidth(String text) {
        return Math.round((this.fontSize / 2) * text.length());
    }
}
