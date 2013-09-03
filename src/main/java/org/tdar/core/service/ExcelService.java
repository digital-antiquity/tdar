package org.tdar.core.service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.excel.CellFormat;
import org.tdar.core.service.excel.SheetProxy;

/*
 * This is a service specific to trying to centralize all of the specific issues with writing 
 * excel files. It could handle helper functions for reading in the future too.
 */
@Service
public class ExcelService {

    // Office 2003 row/sheet max
//    public static final int MAX_ROWS_PER_SHEET = 65536;
//    public static final int MAX_COLUMNS_PER_SHEET = 256;

    // official office spec states that sheet max is limited by available RAM but has no max. So this is an arbitrary number.
    public static final int MAX_SHEETS_PER_WORKBOOK = 32;

    public final Logger logger = LoggerFactory.getLogger(getClass());


    public static final int FIRST_ROW = 0;
    public static final int FIRST_COLUMN = 0;
    public static final SpreadsheetVersion DEFAULT_EXCEL_VERSION = SpreadsheetVersion.EXCEL97;
    public static final int MAX_ROWS_PER_WORKBOOK = DEFAULT_EXCEL_VERSION.getMaxRows() * MAX_SHEETS_PER_WORKBOOK;

    /*
     * Add validation to a given column
     */
    public <T extends Enum<?>> void addColumnValidation(HSSFSheet sheet, int i, HSSFDataValidationHelper validationHelper, T[] enums) {
        DataValidationConstraint validationConstraint = validationHelper.createExplicitListConstraint(getEnumNames(enums).toArray(new String[] {}));
        HSSFDataValidation dataValidation = setupSheetValidation(sheet, i, validationConstraint);
        dataValidation.setSuppressDropDownArrow(false);
    }

    private HSSFDataValidation setupSheetValidation(HSSFSheet sheet, int i, DataValidationConstraint validationConstraint) {
        CellRangeAddressList addressList = new CellRangeAddressList();
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, validationConstraint);
        addressList.addCellRangeAddress(1, i, 10000, i);
        dataValidation.setEmptyCellAllowed(true);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);
        return dataValidation;
    }
    
    public HSSFDataValidation addNumericColumnValidation(HSSFSheet sheet, int i, HSSFDataValidationHelper validationHelper, String formula1, String formula2) {
        DataValidationConstraint validationConstraint = validationHelper.createDecimalConstraint(DVConstraint.OperatorType.IGNORED, formula1, formula2);
        HSSFDataValidation dataValidation = setupSheetValidation(sheet, i, validationConstraint);
        return dataValidation;
    }

    public HSSFDataValidation addIntegerColumnValidation(HSSFSheet sheet, int i, HSSFDataValidationHelper validationHelper, String formula1, String formula2) {
        DataValidationConstraint validationConstraint = validationHelper.createDecimalConstraint(DVConstraint.OperatorType.IGNORED, formula1, formula2);
        HSSFDataValidation dataValidation = setupSheetValidation(sheet, i, validationConstraint);
        return dataValidation;
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

    public HSSFColor getTdarRed(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 122, 21, 1);
    }

    public HSSFColor getTdarBeige(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 233, 224, 205);
    }

    public HSSFColor getTdarDkBeige(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 219, 208, 188);
    }

    public HSSFColor getTdarDkGreen(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 160, 157, 91);
    }

    public HSSFColor getTdarLtYellow(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 252, 248, 228);
    }

    private HSSFColor addColorToPalatte(HSSFWorkbook wb, int i, int j, int k) {
        // creating a custom palette for the workbook
        HSSFPalette palette = wb.getCustomPalette();
        return palette.addColor((byte) i, (byte) j, (byte) k);
    }

    /*
     * simple helper to create a row (needed before writing to that row)
     */
    public Row createRow(Sheet wb, int rowNum) {
        Row row = wb.getRow(rowNum);
        if (row == null) {
            row = wb.createRow(rowNum);
        }
        return row;
    }

    /*
     * Create a style that's bold and wraps
     */
    public CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle summaryStyle = workbook.createCellStyle();
        Font summaryFont = workbook.createFont();
        summaryFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        summaryStyle.setFont(summaryFont);
        summaryStyle.setWrapText(true);
        return summaryStyle;
    }

    /*
     * Create a style that's bold and wraps
     */
    public CellStyle createBasicSummaryStyle(Workbook workbook) {
        return CellFormat.BOLD.createStyle(workbook);
    }

    // helper for creating a workbook
    public Workbook createWorkbook() {
        return createWorkbook(DEFAULT_EXCEL_VERSION);
    }

    public Workbook createWorkbook(SpreadsheetVersion version) {
        if (version == DEFAULT_EXCEL_VERSION)
            return new HSSFWorkbook();
        else
            return new XSSFWorkbook();

    }

    // create a workbook, but with a name
    public Sheet createWorkbook(String name) {
        Workbook createWorkbook = createWorkbook();
        return createWorkbook.createSheet(name);
    }

    /*
     * Create a cell and set the value
     */
    public Cell createCell(Row row, int position, String value) {
        return createCell(row, position, value, null);
    }

    /*
     * specify the style for a cell
     */
    public void setCellStyle(Sheet sheet, int rowNum, int colNum, CellStyle style) {
        sheet.getRow(rowNum).getCell(colNum).setCellStyle(style);
    }

    
    public String getCellValue(DataFormatter formatter, FormulaEvaluator evaluator, Row columnNamesRow, int columnIndex) {
        return formatter.formatCellValue(columnNamesRow.getCell(columnIndex), evaluator);
    }


    /*
     * Create a cell and be smart about it. If there's a link, make it a link, if numeric, set the type
     * to say, numeric.
     */
    public Cell createCell(Row row, int position, String value, CellStyle style) {
        Cell cell = row.createCell(position);

        if (!StringUtils.isEmpty(value)) {
            if (value.startsWith("http")) {
                Hyperlink hyperlink = row.getSheet().getWorkbook().getCreationHelper().createHyperlink(Hyperlink.LINK_URL);
                hyperlink.setAddress(value);
                hyperlink.setLabel(value);
                cell.setHyperlink(hyperlink);
            }
            if (StringUtils.isNumeric(value)) {
                cell.setCellValue(Double.valueOf(value));
            } else {
                if (value.length() > DEFAULT_EXCEL_VERSION.getMaxTextLength()) {
                    cell.setCellValue(StringUtils.substring(value, 0, DEFAULT_EXCEL_VERSION.getMaxTextLength() - 1));
                    logger.error("truncated cell that was too long");
                } else {
                    cell.setCellValue(value);
                }
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
    public void createHeaderCell(CellStyle summaryStyle, Row row, int position, String text) {
        Cell headerCell = row.createCell(position);
        headerCell.setCellValue(text);
        headerCell.setCellStyle(summaryStyle);
    }

    /*
     * add a comment
     */
    public void addComment(CreationHelper factory, Drawing drawing, Cell cell, String commentText) {
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
        comment.setAuthor(TdarConfiguration.getInstance().getSiteAcronym());
    }

    /**
     * add a header row with the following fields
     * 
     * @param rowNum
     * @param columnNumber
     * @param fieldNames
     */
    public CellStyle addHeaderRow(Sheet sheet, int rowNum, int columnNumber, List<String> fieldNames) {
        CellStyle headerStyle = createDefaultHeaderStyle(sheet.getWorkbook());
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
    public CellStyle addDocumentHeaderRow(Sheet sheet, int rowNum, int columnNumber, List<String> fieldNames) {
        CellStyle headerStyle = CellFormat.BOLD.setFontSize((short) 14).setBackgroundColor(new HSSFColor.WHITE()).createStyle(sheet.getWorkbook());
        addRow(sheet, rowNum, columnNumber, fieldNames, headerStyle);
        return headerStyle;
    }

    /*
     * add a row with the following fields
     */
    public CellStyle addRow(Sheet sheet, int rowNum, int startCol, List<? extends Object> fields, CellStyle headerStyle) {
        if (rowNum > DEFAULT_EXCEL_VERSION.getMaxRows()) {
            throw new TdarRecoverableRuntimeException("could not write row -- too many rows for excel");
        }
        Row row = sheet.getRow(rowNum);
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
    public void addDataRow(Sheet sheet, int rowNum, int startCol, List<? extends Object> data) {
        addRow(sheet, rowNum, startCol, data, null);
    }

    /**
     * @param workbook
     * @return
     */
    public CellStyle createDefaultHeaderStyle(Workbook workbook) {
        return CellFormat.NORMAL
                .setColor(new HSSFColor.GREY_25_PERCENT())
                .setBorderBottom(CellStyle.BORDER_THIN)
                .setWrapping(true)
                .setBoldweight(Font.BOLDWEIGHT_BOLD)
                .createStyle(workbook);
    }

    /*
     * helper method to try and simplify the process of just getting all of the data out of a result-set and
     * directly into excel
     */
    public void addDataRow(Sheet sheet, int rowIndex, int startCol, ResultSet resultSet) {
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

    public void addSheets(SheetProxy proxy) {
        int sheetIndex = 0;
        String sheetName = proxy.getName();
        Sheet sheet = null;
        int rowNum = proxy.getStartRow();
        int startRow = proxy.getStartRow();
        SpreadsheetVersion version = proxy.getVersion();
        Workbook workbook = proxy.getWorkbook();
        proxy.preProcess();
        Iterator<Object[]> data = proxy.getData();
        // if the startRow is something other than 0, we assume that the caller was working on this sheet prior
        boolean newSheetNeeded = startRow == 0;
        if (newSheetNeeded) {
            sheet = workbook.createSheet(sheetName);
        } else {
            sheet = workbook.getSheet(sheetName);
        }
        addHeaderRow(sheet, rowNum, proxy.getStartCol(), proxy.getHeaderLabels());
        int maxRows = version.getMaxRows();
        if (TdarConfiguration.getInstance().getMaxSpreadSheetRows() > 1) {
            maxRows = TdarConfiguration.getInstance().getMaxSpreadSheetRows();
        }

        while (data.hasNext()) {
            Object[] row = data.next();
            rowNum++;
            addDataRow(sheet, rowNum, proxy.getStartCol(), Arrays.asList(row));
            if (rowNum >= maxRows - 1) {
                if (proxy.isCleanupNeeded()) {
                    cleanupSheet(sheet);
                }
                sheetIndex++;
                sheet = workbook.createSheet(proxy.getSheetName(sheetIndex));
                rowNum = FIRST_ROW;
                addHeaderRow(sheet, rowNum, proxy.getStartCol(), proxy.getHeaderLabels());
            }
        }
        proxy.postProcess();
    }

//    private void prepareSheet(Sheet sheet) {
//        // FIXME: maybe this should be configurable?
//        sheet.createFreezePane(ExcelService.FIRST_COLUMN, 1, 0, 1);
//    }

    private void cleanupSheet(Sheet sheet) {
        // auto-sizing columns
        // FIXME user start row may not be 0
        int lastCol = sheet.getRow(0).getLastCellNum();
        for (int i = 0; i < lastCol; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public void addPairedHeaderRow(Sheet sheet, int rowNum, int i, List<String> asList) {
        addRow(sheet, rowNum, i, asList, CellFormat.NORMAL.createStyle(sheet.getWorkbook()));
        sheet.getRow(rowNum).getCell(i)
                .setCellStyle(CellFormat.BOLD.setColor(new HSSFColor.GREY_25_PERCENT()).setWrapping(true).createStyle(sheet.getWorkbook()));
    }

    public void setColumnWidth(Sheet sheet, int i, int size) {
        sheet.setColumnWidth(i, size);
    }


}
