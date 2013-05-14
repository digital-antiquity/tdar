package org.tdar.core.service.excel;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

public enum CellFormat {
    NORMAL(HSSFFont.BOLDWEIGHT_NORMAL),
    BOLD(HSSFFont.BOLDWEIGHT_BOLD);

    private CellFormat(short weight) {
        boldweight = weight;
    }

    private short boldweight;
    private transient HSSFColor color;
    private transient HSSFColor backgroundColor;
    private transient short fontSize = 11;
    private transient short borderBottom;
    private transient short borderTop;
    private transient short borderLeft;
    private transient short borderRight;
    private transient boolean wrapping;

    public short getBorderBottom() {
        return borderBottom;
    }

    public CellFormat setBorderBottom(short borderBottom) {
        this.borderBottom = borderBottom;
        return this;
    }

    public short getBorderTop() {
        return borderTop;
    }

    public CellFormat setBorderTop(short borderTop) {
        this.borderTop = borderTop;
        return this;
    }

    public short getBorderLeft() {
        return borderLeft;
    }

    public CellFormat setBorderLeft(short borderLeft) {
        this.borderLeft = borderLeft;
        return this;
    }

    public short getBorderRight() {
        return borderRight;
    }

    public CellFormat setBorderRight(short borderRight) {
        this.borderRight = borderRight;
        return this;
    }

    public short getBoldweight() {
        return boldweight;
    }

    public CellFormat setBoldweight(short boldweight) {
        this.boldweight = boldweight;
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

    /*
     * Create a style that's basic
     */
    public CellStyle createStyle(Workbook workbook) {
        CellStyle summaryStyle = workbook.createCellStyle();
        Font summaryFont = workbook.createFont();
        summaryFont.setBoldweight(this.getBoldweight());
        summaryStyle.setFont(summaryFont);
        if (this.getColor() != null) {
            summaryStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            summaryStyle.setFillForegroundColor(this.getColor().getIndex());
        }

        if (this.getBackgroundColor() != null) {
            summaryStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            summaryStyle.setFillBackgroundColor(this.getBackgroundColor().getIndex());
        }

        if (this.getBorderBottom() != -1) {
            summaryStyle.setBorderBottom(this.getBorderBottom());
        }

        if (this.getBorderTop() != -1) {
            summaryStyle.setBorderTop(this.getBorderTop());
        }

        if (this.getBorderLeft() != -1) {
            summaryStyle.setBorderLeft(this.getBorderLeft());
        }

        if (this.getBorderLeft() != -1) {
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
}
