package org.tdar.core.service.excel;

import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
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
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.excel.CellFormat.Style;
import org.tdar.utils.DataUtil;


/**
 * This is a service specific to trying to centralize all of the specific issues with writing
 * excel files. It could handle helper functions for reading in the future too.
 * 
 * @author abrin
 * 
 */
public class ExcelWorkbookWriter {

    private static final String TRUNCATED = "[TRUNCATED]";

    // official office spec states that sheet max is limited by available RAM but has no max. So this is an arbitrary number.
    public static final int MAX_SHEETS_PER_WORKBOOK = 32;

    // sheet name cannot exceed 31 chars
    private static final int MAX_SHEET_NAME_LENGTH = 31;

    private final Logger logger = LoggerFactory.getLogger(getClass());


    public static final int FIRST_ROW = 0;
    public static final int FIRST_COLUMN = 0;
    public static final SpreadsheetVersion DEFAULT_EXCEL_VERSION = SpreadsheetVersion.EXCEL97;
    public static final int MAX_ROWS_PER_WORKBOOK = DEFAULT_EXCEL_VERSION.getMaxRows() * MAX_SHEETS_PER_WORKBOOK;
    private SpreadsheetVersion version = DEFAULT_EXCEL_VERSION;

    String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
    UrlValidator urlValidator = new UrlValidator(schemes);

    /**
     * Add validation to a given column
     * 
     * @param sheet
     * @param i
     * @param validationHelper
     * @param enums
     */
    public <T extends Enum<?>> void addColumnValidation(HSSFSheet sheet, int i, HSSFDataValidationHelper validationHelper, T[] enums) {
        DataValidationConstraint validationConstraint = validationHelper.createExplicitListConstraint(getEnumNames(enums).toArray(new String[] {}));
        HSSFDataValidation dataValidation = setupSheetValidation(sheet, i, validationConstraint);
        dataValidation.setSuppressDropDownArrow(false);
    }

    /**
     * Sets up validation for the entire worksheet
     * 
     * @param sheet
     * @param i
     * @param validationConstraint
     * @return
     */
    private HSSFDataValidation setupSheetValidation(HSSFSheet sheet, int i, DataValidationConstraint validationConstraint) {
        CellRangeAddressList addressList = new CellRangeAddressList();
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, validationConstraint);
        addressList.addCellRangeAddress(1, i, 10000, i);
        dataValidation.setEmptyCellAllowed(true);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);
        return dataValidation;
    }

    /**
     * Adds Numeric validation for a column of a Worksheet
     * 
     * @param sheet
     * @param i
     * @param validationHelper
     * @param formula1
     * @param formula2
     * @return
     */
    public HSSFDataValidation addNumericColumnValidation(HSSFSheet sheet, int i, HSSFDataValidationHelper validationHelper, String formula1, String formula2) {
        DataValidationConstraint validationConstraint = validationHelper.createDecimalConstraint(DVConstraint.OperatorType.IGNORED, formula1, formula2);
        HSSFDataValidation dataValidation = setupSheetValidation(sheet, i, validationConstraint);
        return dataValidation;
    }

    /**
     * Adds Integer validation for a column of a Worksheet
     * 
     * @param sheet
     * @param i
     * @param validationHelper
     * @param formula1
     * @param formula2
     * @return
     */
    public HSSFDataValidation addIntegerColumnValidation(HSSFSheet sheet, int i, HSSFDataValidationHelper validationHelper, String formula1, String formula2) {
        DataValidationConstraint validationConstraint = validationHelper.createDecimalConstraint(DVConstraint.OperatorType.IGNORED, formula1, formula2);
        HSSFDataValidation dataValidation = setupSheetValidation(sheet, i, validationConstraint);
        return dataValidation;
    }

    /**
     * Produces a list of all enum Names for a given enum array.
     * 
     * @param enums
     * @return
     */

    private <T extends Enum<?>> List<String> getEnumNames(T[] enums) {
        List<String> toReturn = new ArrayList<String>();
        for (T value : enums) {
            toReturn.add(value.name());
        }
        return toReturn;
    }

    /**
     * Adds a the tDAR red to the color palate
     * 
     * @param wb
     * @return
     */
    public HSSFColor getTdarRed(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 122, 21, 1);
    }

    /**
     * Adds a the tDAR beige to the color palate
     * 
     * @param wb
     * @return
     */
    public HSSFColor getTdarBeige(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 233, 224, 205);
    }

    /**
     * Adds a the tDAR dark beige to the color palate
     * 
     * @param wb
     * @return
     */
    public HSSFColor getTdarDkBeige(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 219, 208, 188);
    }

    /**
     * Adds a the tDAR dark green to the color palate
     * 
     * @param wb
     * @return
     */
    public HSSFColor getTdarDkGreen(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 160, 157, 91);
    }

    /**
     * Adds a the tDAR light yellow to the color palate
     * 
     * @param wb
     * @return
     */
    public HSSFColor getTdarLtYellow(HSSFWorkbook wb) {
        return addColorToPalatte(wb, 252, 248, 228);
    }

    /**
     * Adds a generic color to the color palate
     * 
     * @param wb
     * @return
     */
    private HSSFColor addColorToPalatte(HSSFWorkbook wb, int i, int j, int k) {
        // creating a custom palette for the workbook
        HSSFPalette palette = wb.getCustomPalette();
        return palette.addColor((byte) i, (byte) j, (byte) k);
    }

    /**
     * simple helper to create a row (needed before writing to that row)
     * 
     * @param wb
     * @param rowNum
     * @return
     */
    public Row createRow(Sheet wb, int rowNum) {
        Row row = wb.getRow(rowNum);
        if (row == null) {
            row = wb.createRow(rowNum);
        }
        return row;
    }

    /**
     * Create a style that's bold and wraps
     * 
     * @param workbook
     * @return
     */
    public CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle summaryStyle = workbook.createCellStyle();
        Font summaryFont = workbook.createFont();
        summaryFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        summaryStyle.setFont(summaryFont);
        summaryStyle.setWrapText(true);
        return summaryStyle;
    }

    /**
     * Create a style that's bold
     * 
     * @param workbook
     * @return
     */
    public CellStyle createBasicSummaryStyle(Workbook workbook) {
        return CellFormat.build(Style.BOLD).createStyle(workbook);
    }

    /**
     * Helper to create a workbook
     * 
     * @return
     */
    public Workbook createWorkbook() {
        return createWorkbook(DEFAULT_EXCEL_VERSION);
    }

    /**
     * Create a workbook with a specific Excel Version
     * 
     * @param version
     * @return
     */
    public Workbook createWorkbook(SpreadsheetVersion version) {
        this.version = version;
        if (version == DEFAULT_EXCEL_VERSION) {
            return new HSSFWorkbook();
        } else {
            return new XSSFWorkbook();
        }

    }

    /**
     * create a workbook, but with a specific name
     * 
     * @param name
     * @return
     */
    public Sheet createWorkbook(String name) {
        Workbook createWorkbook = createWorkbook();
        return createWorkbook.createSheet(name);
    }

    /**
     * create a workbook, but with a specific name and vesrion
     * 
     * @param name
     * @param version
     * @return
     */
    public Sheet createWorkbook(String name, SpreadsheetVersion version) {
        Workbook createWorkbook = createWorkbook(version);
        return createWorkbook.createSheet(name);
    }

    /**
     * Create a cell and set the value
     * 
     * @param row
     * @param position
     * @param value
     * @return
     */
    public Cell createCell(Row row, int position, String value) {
        return createCell(row, position, value, null);
    }

    /**
     * Set the Style of a cell
     * 
     * @param sheet
     * @param rowNum
     * @param colNum
     * @param style
     */
    public void setCellStyle(Sheet sheet, int rowNum, int colNum, CellStyle style) {
        sheet.getRow(rowNum).getCell(colNum).setCellStyle(style);
    }

    /**
     * Get the value of a cell
     * 
     * @param formatter
     * @param evaluator
     * @param columnNamesRow
     * @param columnIndex
     * @return
     */
    public String getCellValue(DataFormatter formatter, FormulaEvaluator evaluator, Row columnNamesRow, int columnIndex) {
        return formatter.formatCellValue(columnNamesRow.getCell(columnIndex), evaluator);
    }
    
    /**
     * Create a cell and be smart about it. If there's a link, make it a link, if numeric, set the type
     * to say, numeric.
     * 
     * @param row
     * @param position
     * @param value
     * @param style
     * @return
     */
    public Cell createCell(Row row, int position, String value, CellStyle style) {
        Cell cell = row.createCell(position);

        if (!StringUtils.isEmpty(value)) {
            if (value.startsWith("http") && urlValidator.isValid(value) ) {
                Hyperlink hyperlink = row.getSheet().getWorkbook().getCreationHelper().createHyperlink(org.apache.poi.common.usermodel.Hyperlink.LINK_URL);
                hyperlink.setAddress(value);
                hyperlink.setLabel(value);
                cell.setHyperlink(hyperlink);
            }
            if (StringUtils.isNumeric(value)) {
                cell.setCellValue(Double.valueOf(value));
            } else {
                if (value.length() > DEFAULT_EXCEL_VERSION.getMaxTextLength()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(TRUNCATED);
                    sb.append(" ");
                    sb.append(StringUtils.substring(value, 0, DEFAULT_EXCEL_VERSION.getMaxTextLength() - TRUNCATED.length() - 2));
                    cell.setCellValue(sb.toString());
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

    /**
     * Create a header cell with text
     * 
     * @param summaryStyle
     * @param row
     * @param position
     * @param text
     */
    public void createHeaderCell(CellStyle summaryStyle, Row row, int position, String text) {
        Cell headerCell = row.createCell(position);
        headerCell.setCellValue(text);
        headerCell.setCellStyle(summaryStyle);
    }

    /**
     * add an excel comment
     * 
     * @param factory
     * @param drawing
     * @param cell
     * @param commentText
     */
    public void addComment(CreationHelper factory, Drawing drawing, Cell cell, String commentText) {
        if (StringUtils.isEmpty(commentText)) {
            return;
        }
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
        CellStyle headerStyle = CellFormat.build(Style.BOLD).setFontSize((short) 14).createStyle(sheet.getWorkbook());
        addRow(sheet, rowNum, columnNumber, fieldNames, headerStyle);
        return headerStyle;
    }

    /**
     * add a comment
     * 
     * @param sheet
     * @param rowNum
     * @param startCol
     * @param fields
     * @param headerStyle
     * @return
     */
    public CellStyle addRow(Sheet sheet, int rowNum, int startCol, List<? extends Object> fields, CellStyle headerStyle) {
        if (rowNum > version.getMaxRows()) {
            throw new TdarRecoverableRuntimeException("excelService.too_many_rows");
        }
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        for (Object datum : fields) {
            String value = DataUtil.extractStringValue(datum);
            createCell(row, startCol, value, headerStyle);
            startCol++;
        }
        return headerStyle;
    }

    /**
     * Add a full data row based on the list of Objects
     * 
     * @param rowNum
     */
    public void addDataRow(Sheet sheet, int rowNum, int startCol, List<? extends Object> data) {
        addRow(sheet, rowNum, startCol, data, null);
    }

    /**
     * Create and define a default header sty;e
     * 
     * @param workbook
     * @return
     */
    public CellStyle createDefaultHeaderStyle(Workbook workbook) {
        return CellFormat.build(Style.BOLD)
                .setColor(new HSSFColor.GREY_25_PERCENT())
                .setBorderBottom(CellStyle.BORDER_THIN)
                .setWrapping(true)
                .createStyle(workbook);
    }

    /**
     * helper method to try and simplify the process of just getting all of the data out of a result-set and
     * directly into excel
     * 
     * @param sheet
     * @param rowIndex
     * @param startCol
     * @param resultSet
     */
    public void addDataRow(Sheet sheet, int rowIndex, int startCol, ResultSet resultSet) {
        List<Object> row = new ArrayList<Object>();
        try {
            for (int columnIndex = 0; columnIndex < resultSet.getMetaData().getColumnCount(); columnIndex++) {
                row.add(resultSet.getObject(columnIndex + 1));
            }
        } catch (Exception e) {
            throw new TdarRecoverableRuntimeException("excelService.sql_exception");
        }
        addDataRow(sheet, rowIndex, startCol, row);
    }

    private String normalizeSheetName(Workbook workbook, String sheetName) {
        String name = sheetName;
        if (name.length() > MAX_SHEET_NAME_LENGTH) {
            name = sheetName.substring(0, MAX_SHEET_NAME_LENGTH);
            int num = 2; // suffix to use for uniqueifying the name - keep incrementing until it doesn't exist in the workbook
            while (workbook.getSheet(name) != null) {
                String suffix = "_" + Integer.toString(num++);
                // NOTE: this could be a regexp, but with the escaping it's less readable then would be preferred
                name = name.replace("/", "-");
                name = name.replace("\\", "-");
                name = name.replace("[", "-");
                name = name.replace("]", "-");
                name = name.replace("*", "-");
                name = name.replace("?", "-");
                name = name.replace(":", "-");
                name = name.substring(0, name.length() - suffix.length()) + suffix;
            }
        }
        return name;
    }

    public void addSheets(SheetProxy proxy) {
        int sheetIndex = 0;
        Sheet sheet = null;
        int rowNum = proxy.getStartRow();
        int startRow = proxy.getStartRow();
        if (this.version != proxy.getVersion()) {
            this.version = proxy.getVersion();
        }
        Workbook workbook = proxy.getWorkbook();
        String sheetName = normalizeSheetName(workbook, proxy.getName());
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
        rowNum++;
        if (proxy.getNoteRow() != null) {
            addDataRow(sheet, rowNum, proxy.getStartCol(), Arrays.asList(proxy.getNoteRow()));
        }
        int maxRows = version.getMaxRows() -1;
        if (TdarConfiguration.getInstance().getMaxSpreadSheetRows() > 1) {
            maxRows = TdarConfiguration.getInstance().getMaxSpreadSheetRows();
        }
        while (data.hasNext()) {
            Object[] row = null;
            try {
                row = data.next();
            } catch (RuntimeException re) {
                logger.warn("RuntimeException, table empty?", re);
                break;
            }
            rowNum++;

            if (rowNum % 10_000 == 0) {
                logger.debug("writing row {} of {}", rowNum, sheet.getSheetName());
            }
            if (rowNum == maxRows) {
                logger.debug("resetting rows to 0");
                if (proxy.isCleanupNeeded()) {
                    autoSizeColumnsOnSheet(sheet);
                }
                sheetIndex++;
                sheet = workbook.createSheet(normalizeSheetName(workbook, proxy.getSheetName(sheetIndex)));
                rowNum = FIRST_ROW;
                addHeaderRow(sheet, rowNum, proxy.getStartCol(), proxy.getHeaderLabels());
                rowNum++;
            }
            addDataRow(sheet, rowNum, proxy.getStartCol(), Arrays.asList(row));
        }
        
        if (proxy.hasFreezeRow()) {
            sheet.createFreezePane(0, proxy.getStartRow()+1, 0, proxy.getFreezeRow()+1);
        }
        
        if (proxy.isAutosizeCols()) {
            autoSizeColumnsOnSheet(sheet);
        }
        
        proxy.postProcess();
    }

    /**
     * try to auto-size each column
     * 
     * @param sheet
     */
    private void autoSizeColumnsOnSheet(Sheet sheet) {
        // auto-sizing columns
        // FIXME user start row may not be 0
        if (sheet == null || sheet.getRow(0) == null) {
            return;
        }
        
        if (sheet instanceof SXSSFSheet) {
            logger.debug("can't auto-size a SXSSF sheet");
            return;
        }

        int lastCol = sheet.getRow(0).getLastCellNum();
        for (int i = 0; i < lastCol; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Add a key-value pair column eg:
     * KEY: value
     * KEY: value
     * ..
     * 
     * @param sheet
     * @param rowNum
     * @param i
     * @param asList
     */
    public void addPairedHeaderRow(Sheet sheet, int rowNum, int i, List<String> asList) {
        addRow(sheet, rowNum, i, asList, CellFormat.build(Style.NORMAL).createStyle(sheet.getWorkbook()));
        sheet.getRow(rowNum).getCell(i)
                .setCellStyle(CellFormat.build(Style.BOLD).setColor(new HSSFColor.GREY_25_PERCENT()).setWrapping(true).createStyle(sheet.getWorkbook()));
    }

    /**
     * Set the width of a column
     * 
     * @param sheet
     * @param i
     * @param size
     */
    public void setColumnWidth(Sheet sheet, int i, int size) {
        sheet.setColumnWidth(i, size);
    }

}
