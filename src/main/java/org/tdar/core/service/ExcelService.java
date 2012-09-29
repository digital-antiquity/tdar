package org.tdar.core.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.stereotype.Service;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

/*
 * This is a service specific to trying to centralize all of the specific issues with writing 
 * excel files. It could handle helper functions for reading in the future too.
 */
@Service
public class ExcelService {

    public static final int MAX_ROWS_PER_SHEET = SpreadsheetVersion.EXCEL97.getMaxRows();
    public static final int FIRST_ROW = 0;
    public static final int FIRST_COLUMN = 0;
    
    /*
     * Add validation to a given column
     */
    public <T extends Enum<?>> void addColumnValidation(HSSFSheet sheet, int i, HSSFDataValidationHelper validationHelper, T[] enums) {
        CellRangeAddressList addressList = new CellRangeAddressList();
        addressList.addCellRangeAddress(1, i, 10000, i);
        DataValidationConstraint validationConstraint = validationHelper.createExplicitListConstraint(getEnumNames(enums).toArray(new String[] {}));
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, validationConstraint);
        dataValidation.setEmptyCellAllowed(true);
        dataValidation.setShowPromptBox(true);
        dataValidation.setSuppressDropDownArrow(false);
        sheet.addValidationData(dataValidation);
    }

    /*
     * get all of the names of enums passed in a list
     */
    private <T extends Enum<?>> List<String> getEnumNames(T[] enums) {
        List<String> toReturn = new ArrayList<String>();
        for (T value : enums) {
            toReturn.add(value.name());
        }
        return toReturn;
    }

    /*
     * simple helper to create a row (needed before writing to that row)
     */
    public HSSFRow createRow(HSSFSheet wb, int rowNum) {
        HSSFRow row = wb.getRow(rowNum);
        if (row == null) {
            row = wb.createRow(rowNum);
        }
        return row;
    }

    /*
     * Create a style that's bold and wraps
     */
    public HSSFCellStyle createSummaryStyle(HSSFWorkbook workbook) {
        HSSFCellStyle summaryStyle = workbook.createCellStyle();
        HSSFFont summaryFont = workbook.createFont();
        summaryFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        summaryStyle.setFont(summaryFont);
        summaryStyle.setWrapText(true);
        return summaryStyle;
    }

    /*
     * Create a style that's bold and wraps
     */
    public HSSFCellStyle createBasicSummaryStyle(HSSFWorkbook workbook) {
        HSSFCellStyle summaryStyle = workbook.createCellStyle();
        HSSFFont summaryFont = workbook.createFont();
        summaryFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        summaryStyle.setFont(summaryFont);
        return summaryStyle;
    }

    
    /*
     * Create a style that's basic
     */
    public HSSFCellStyle createBasicStyle(HSSFWorkbook workbook) {
        HSSFCellStyle summaryStyle = workbook.createCellStyle();
        return summaryStyle;
    }

    /*
     * create a style that's bold and wraps, and is grey in the background and uses bold text
     */
    public HSSFCellStyle createSummaryStyleGray(HSSFWorkbook workbook) {
        HSSFCellStyle summaryStyle = createBasicSummaryStyle(workbook);
        summaryStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        summaryStyle.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());
        return summaryStyle;
    }

    /*
     * create a style that's bold and wraps, and is green in the background
     */
    public HSSFCellStyle createSummaryStyleGreen(HSSFWorkbook workbook) {
        HSSFCellStyle summaryStyle = createSummaryStyle(workbook);
        summaryStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        summaryStyle.setFillForegroundColor(new HSSFColor.LIGHT_GREEN().getIndex());
        return summaryStyle;
    }

    /*
     * create a style that's grey in the background and uses bold text
     */
    public HSSFCellStyle createTableNameStyle(HSSFWorkbook workbook) {
        HSSFCellStyle dataTableNameStyle = workbook.createCellStyle(); // DATA_TABLE_NAME_STYLE
        dataTableNameStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        dataTableNameStyle.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());
        return dataTableNameStyle;
    }

    /*
     * create a style that's bold and wraps, and is light blue in the background and uses bold text? and has a line at the bottom
     */
    public HSSFCellStyle createHeaderStyle(HSSFWorkbook workbook, HSSFFont font) {
        HSSFCellStyle headerStyle = workbook.createCellStyle(); // HEADER_STYLE
        headerStyle.setFont(font);
        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerStyle.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return headerStyle;
    }

    // helper for creating a workbook
    public HSSFWorkbook createWorkbook() {
        return new HSSFWorkbook();
    }

    // create a workbook, but with a name
    public HSSFSheet createWorkbook(String name) {
        HSSFWorkbook createWorkbook = createWorkbook();
        return createWorkbook.createSheet(name);
    }

    /*
     * Create a cell and set the value
     */
    public HSSFCell createCell(HSSFRow row, int position, String value) {
        return createCell(row, position, value, null);
    }

    /*
     * specify the style for a cell
     */
    public void setCellStyle(HSSFSheet sheet, int rowNum, int colNum, HSSFCellStyle style) {
        sheet.getRow(rowNum).getCell(colNum).setCellStyle(style);
    }

    /*
     * Create a cell and be smart about it. If there's a link, make it a link, if numeric, set the type
     * to say, numeric.
     */
    public HSSFCell createCell(HSSFRow row, int position, String value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(position);

        if (!StringUtils.isEmpty(value)) {
            if (value.startsWith("http")) {
                HSSFHyperlink hyperlink = new HSSFHyperlink(HSSFHyperlink.LINK_URL);
                hyperlink.setAddress(value);
                hyperlink.setLabel(value);
                cell.setHyperlink(hyperlink);
            }
            if (StringUtils.isNumeric(value)) {
                cell.setCellValue(Double.valueOf(value));
            } else {
                cell.setCellValue(value);
            }
        }
        if (style != null) {
            cell.setCellStyle(style);
        }

        return cell;
    }

    /*
     * Create a header cell with text
     */
    public void createHeaderCell(HSSFCellStyle summaryStyle, HSSFRow row, int position, String text) {
        HSSFCell headerCell = row.createCell(position);
        headerCell.setCellValue(text);
        headerCell.setCellStyle(summaryStyle);
    }

    /*
     * add a comment
     */
    public void addComment(CreationHelper factory, Drawing drawing, HSSFCell cell, String commentText) {
        if (StringUtils.isEmpty(commentText))
            return;
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 3);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);
        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(commentText);
        comment.setString(str);
        comment.setRow(cell.getRowIndex());
        comment.setColumn(cell.getColumnIndex());
        comment.setAuthor("tDAR");
    }

    /**
     * add a header row with the following fields
     * 
     * @param rowNum
     * @param columnNumber
     * @param fieldNames
     */
    public HSSFCellStyle addHeaderRow(HSSFSheet sheet, int rowNum, int columnNumber, List<String> fieldNames) {
        HSSFCellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        addRow(sheet, rowNum, columnNumber, fieldNames, headerStyle);
        return headerStyle;
    }

    /**
     * add a header row with the following fields
     * 
     * @param rowNum
     * @param columnNumber
     * @param fieldNames
     */
    public HSSFCellStyle addDocumentHeaderRow(HSSFSheet sheet, int rowNum, int columnNumber, List<String> fieldNames) {
        HSSFCellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        HSSFFont font = sheet.getWorkbook().createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerStyle.setFont(font);
        headerStyle.setFillBackgroundColor(HSSFColor.WHITE.index);
        addRow(sheet, rowNum, columnNumber, fieldNames, headerStyle);
        return headerStyle;
    }

    /*
     * add a row with the following fields
     */
    public HSSFCellStyle addRow(HSSFSheet sheet, int rowNum, int startCol, List<? extends Object> fields, HSSFCellStyle headerStyle) {
        if (rowNum > MAX_ROWS_PER_SHEET) {
            throw new TdarRecoverableRuntimeException("could not write row -- too many rows for excel");
        }
        HSSFRow row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        for (Object datum : fields) {
            String value = GenericService.extractStringValue(datum);
            createCell(row, startCol, value, headerStyle);
            startCol++;
        }
        return headerStyle;
    }

    /**
     * @param rowNum
     * @param i
     */
    public void addDataRow(HSSFSheet sheet, int rowNum, int startCol, List<? extends Object> data) {
        addRow(sheet, rowNum, startCol, data, null);
    }

    /**
     * @param workbook
     * @return
     */
    public HSSFCellStyle createHeaderStyle(HSSFWorkbook workbook) {
        HSSFFont font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        return createHeaderStyle(workbook, font);
    }

    /*
     * helper method to try and simplify the process of just getting all of the data out of a result-set and
     * directly into excel
     */
    public void addDataRow(HSSFSheet sheet, int rowIndex, int startCol, ResultSet resultSet) {
        List<Object> row = new ArrayList<Object>();
        try {
            for (int columnIndex = 0; columnIndex < resultSet.getMetaData().getColumnCount(); columnIndex++) {
                row.add(resultSet.getObject(columnIndex + 1));
            }
        } catch (Exception e) {
            throw new TdarRecoverableRuntimeException("SQL Exception");
        }
        addDataRow(sheet, rowIndex, startCol, row);
    }

    public void addPairedHeaderRow(HSSFSheet sheet, int rowNum, int i, List<String> asList) {
        addRow(sheet, rowNum, i, asList, createBasicStyle(sheet.getWorkbook()));
        sheet.getRow(rowNum).getCell(i).setCellStyle(createSummaryStyleGray(sheet.getWorkbook()));
    }

    public void setColumnWidth(HSSFSheet sheet, int i,int size) {
        sheet.setColumnWidth(i, size);
    }

}
