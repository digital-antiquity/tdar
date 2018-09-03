package org.tdar.db.conversion.converters;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

/*
 * An enum "helper" to simplify dealing with formatting Excel/POI Objects
 */
public class CellFormat {
    public enum Style {
        NORMAL(false),
        BOLD(true);

        private boolean bold;

        private Style(boolean bold) {
            this.setBold(bold);
        }

        public boolean isBold() {
            return bold;
        }

        private void setBold(boolean bold) {
            this.bold = bold;
        }
    }

    private Style style;
    private HSSFColor color;
    private HSSFColor backgroundColor;
    private short fontSize = 11;
    private BorderStyle borderBottom = BorderStyle.NONE;
    private BorderStyle borderTop = BorderStyle.NONE;
    private BorderStyle borderLeft = BorderStyle.NONE;
    private BorderStyle borderRight = BorderStyle.NONE;
    private boolean wrapping;

    public CellFormat(Style style) {
        this.style = style;
    }

    public BorderStyle getBorderBottom() {
        return borderBottom;
    }

    public CellFormat setBorderBottom(BorderStyle borderBottom) {
        this.borderBottom = borderBottom;
        return this;
    }

    public BorderStyle getBorderTop() {
        return borderTop;
    }

    public CellFormat setBorderTop(BorderStyle borderTop) {
        this.borderTop = borderTop;
        return this;
    }

    public BorderStyle getBorderLeft() {
        return borderLeft;
    }

    public CellFormat setBorderLeft(BorderStyle borderLeft) {
        this.borderLeft = borderLeft;
        return this;
    }

    public BorderStyle getBorderRight() {
        return borderRight;
    }

    public CellFormat setBorderRight(BorderStyle borderRight) {
        this.borderRight = borderRight;
        return this;
    }

    public HSSFColor getColor() {
        return color;
    }

    public CellFormat setColor(HSSFColor colorIndex) {
        this.color = colorIndex;
        return this;
    }

    public short getFontSize() {
        return fontSize;
    }

    public CellFormat setFontSize(short fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public boolean isWrapping() {
        return wrapping;
    }

    public CellFormat setWrapping(boolean wraps) {
        this.wrapping = wraps;
        return this;
    }

    public HSSFColor getBackgroundColor() {
        return backgroundColor;
    }

    public CellFormat setBackgroundColor(HSSFColor backgroundColorIndex) {
        this.backgroundColor = backgroundColorIndex;
        return this;
    }

    public static CellFormat build(Style style) {
        CellFormat format = new CellFormat(style);
        return format;
    }

    /*
     * Create a style that's basic
     */
    public CellStyle createStyle(Workbook workbook) {
        CellStyle summaryStyle = workbook.createCellStyle();
        Font summaryFont = workbook.createFont();
        summaryFont.setBold(style.isBold());
        summaryStyle.setFont(summaryFont);
        if (this.getColor() != null) {
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryStyle.setFillForegroundColor(this.getColor().getIndex());
        }

        if (this.getBackgroundColor() != null) {
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryStyle.setFillBackgroundColor(this.getBackgroundColor().getIndex());
        }

        if (this.getBorderBottom() != BorderStyle.NONE) {
            summaryStyle.setBorderBottom(this.getBorderBottom());
        }

        if (this.getBorderTop() != BorderStyle.NONE) {
            summaryStyle.setBorderTop(this.getBorderTop());
        }

        if (this.getBorderLeft() != BorderStyle.NONE) {
            summaryStyle.setBorderLeft(this.getBorderLeft());
        }

        if (this.getBorderLeft() != BorderStyle.NONE) {
            summaryStyle.setBorderBottom(this.getBorderLeft());
        }

        if (this.isWrapping()) {
            summaryStyle.setWrapText(this.isWrapping());
        }

        if (this.getFontSize() != -1) {
            summaryFont.setFontHeightInPoints(this.getFontSize());
        }

        return summaryStyle;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }
}
