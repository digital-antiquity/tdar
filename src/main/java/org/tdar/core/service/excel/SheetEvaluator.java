package org.tdar.core.service.excel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ExcelService;

/**
 * Evaluates the first N rows of a single POI Sheet to determine the bounding indexes for the data and the headers.
 * This could be the first row or a few header rows ahead which may confuse things.
 * 
 * It first reads in all the data into a List of DataRows and then analyzes and scores them
 * to compare for the highest chance of being a header row. The heuristic for determining
 * whether or not a row is a header row is still imperfect and is based on the percentage of  
 * alphabetic data in the topmost row. 
 * 
 * FIXME: If a sheet has no clear header row and the data is mostly alphabetic, its first row will be coerced into a header row 
 * and that data will then be converted into data table column names, which is not ideal. 
 *   
 */
public class SheetEvaluator {
    private static final int DEFAULT_ROWS_TO_EVALUATE = 25;
    private static final Pattern ALPHABETIC_PATTERN = Pattern.compile("[a-zA-Z]+");
    private static final double ALPHABETIC_DATA_DENSITY_THRESHOLD = 0.60d;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    // 0-based row index of the header, -1 if no headers found.
    private int headerRowIndex = -1;
    private List<String> headerColumnNames;
    // 0-based row index specifying where the actual data begins, after the header row
    private int dataRowStartIndex = ExcelService.FIRST_ROW;
    // 0-based column index specifying where the actual data begins
    private int dataColumnStartIndex = ExcelService.FIRST_COLUMN;
    // 0-based column index specifying where the actual data ends
    private int dataColumnEndIndex = -1;
    // 0-based column index for the last cell column reported by POI / Excel
    private int maxCellCount = -1;
    private FormulaEvaluator formulaEvaluator;
    private DataFormatter formatter = new HSSFDataFormatter();

    public SheetEvaluator() {
    }

    public SheetEvaluator(Sheet sheet) {
        this(sheet, DEFAULT_ROWS_TO_EVALUATE);
    }

    public SheetEvaluator(Sheet sheet, int endRow) {
        evaluate(sheet, endRow);
    }

    public void reset() {
        headerRowIndex = -1;
        headerColumnNames = null;
        maxCellCount = -1;
        dataRowStartIndex = ExcelService.FIRST_ROW;
        dataColumnStartIndex = ExcelService.FIRST_COLUMN;
        dataColumnEndIndex = -1;
    }

    public void evaluate(Sheet sheet) {
        evaluate(sheet, DEFAULT_ROWS_TO_EVALUATE);
    }

    public void evaluate(Sheet sheet, int endRow) {
        logger.trace("evaluating sheet {}", sheet.getSheetName());
        if (headerRowIndex != -1) {
            reset();
        }
        // clamp endRow if the sheet doesn't have enough rows to scan
        endRow = Math.min(endRow, sheet.getLastRowNum());
        // locate and skip any merged regions at the start of the sheet
        Set<Integer> mergedRegionRowIndexes = getMergedRegionsIndexes(sheet);
        // look for something headerish in the data and develop some heuristics based on how many
        // rows of consistent data we can find.
        formulaEvaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        List<DataRow> dataRows = new ArrayList<>();
        for (int rowIndex = ExcelService.FIRST_ROW; rowIndex < endRow; rowIndex++) {
            if (mergedRegionRowIndexes.contains(rowIndex)) {
                // this row is a merged region that we can't sensibly use for anything at all
                logger.trace("skipping merged region row index {}", rowIndex);
                continue;
            }
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            dataRows.add(new DataRow(row));
        }
        // analyze DataRows and score them for likelihood of being a header or data row
        int maxNumberOfDataValues = 0;
        DataRow candidateHeaderRow = null;
        // reset start index to be ridiculously large and then take the min of it and all data carrying rows.
        dataColumnStartIndex = Short.MAX_VALUE;
        for (DataRow dataRow : dataRows) {
            logger.trace("analyzing {}", dataRow);
            if (dataRow.hasData()) {
                dataColumnStartIndex = Math.min(dataColumnStartIndex, dataRow.getColumnStartIndex());
                dataColumnEndIndex = Math.max(dataColumnEndIndex, dataRow.getColumnEndIndex());
                maxCellCount = Math.max(maxCellCount, dataRow.getMaxCellCount());
                if (dataRow.getNumberOfDataValues() > maxNumberOfDataValues) {
                    maxNumberOfDataValues = dataRow.getNumberOfDataValues();
                    if (dataRow.isHeaderish()) {
                        candidateHeaderRow = dataRow;
                    }
                }
            }
        }
        logger.trace("data column start/end: [{}, {}]", dataColumnStartIndex, dataColumnEndIndex);
        initializeHeaders(candidateHeaderRow);
        logger.debug("initialized headers: {}", headerColumnNames);
    }

    public Set<Integer> getMergedRegionsIndexes(Sheet sheet) {
        Set<Integer> indexes = new HashSet<>();
        int numMergedRegions = sheet.getNumMergedRegions();
        // FIXME: why upper bound < 5?
        if (numMergedRegions > 0 && numMergedRegions < 5) {
            for (int i = 0; i < numMergedRegions; i++) {
                CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                int firstRow = mergedRegion.getFirstRow();
                int lastRow = mergedRegion.getLastRow();
                // FIXME: why upper bound < 10?
                if (lastRow < 10) {
                    for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                        indexes.add(rowIndex);
                    }
                }
            }
        }
        return indexes;
    }

    public void validate(Row row) {
        // is the first cell before the first column with a heading?
        if (row.getFirstCellNum() < dataColumnStartIndex) {
            // FIXME: an inappropriate exception message will be generated in this case.
            throwTdarRecoverableRuntimeException(row.getRowNum(), row.getFirstCellNum(), dataColumnStartIndex, row.getSheet().getSheetName());
        }
        // is the last cell after the last column with a heading?
        short lastDataCellIndex = getLastDataCellIndex(row);
        if (dataColumnEndIndex < lastDataCellIndex) {
            throwTdarRecoverableRuntimeException(row.getRowNum(), lastDataCellIndex + 1, dataColumnEndIndex + 1, row.getSheet().getSheetName());
        }
    }
    
    /**
     * Returns the last cell index with data (ignores blanks) 
     * @param row
     * @return
     */
    public short getLastDataCellIndex(Row row) {
        for (short ci = row.getLastCellNum(); ci >= row.getFirstCellNum(); --ci) {
            if (StringUtils.isBlank(getCellValueAsString(row.getCell(ci)))) {
                continue;
            }
            return ci;
        }
        return row.getFirstCellNum();
    }

    private void throwTdarRecoverableRuntimeException(int rowNumber, int numberOfDataColumns, int columnNameBound, String sheetName) {
        throw new TdarRecoverableRuntimeException("sheetEvaluator.row_has_more_columns", "sheetEvaluator.row_has_more_columns_url", Arrays.asList(rowNumber,
                numberOfDataColumns, columnNameBound, sheetName));
    }

    private int evaluateForBlankCells(Row row, int endAt, int cellCount) {
        for (int j = cellCount; j >= endAt; j--) {
            String value = getCellValueAsString(row.getCell(j));
            if (StringUtils.isBlank(value)) {
                cellCount--;
            }
        }
        return cellCount;
    }

    public String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        List<Integer> errors = Arrays.asList(cell.getRowIndex() + 1, cell.getColumnIndex() + 1);
        try {
            if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
                throw new TdarRecoverableRuntimeException("sheetEvaluator.parse_excel_error", "sheetEvaluator.parse_excel_error_url", errors);
            }
            return formatter.formatCellValue(cell, formulaEvaluator);
        } catch (IndexOutOfBoundsException e) {
            logger.trace("row {} col: {}", cell.getRowIndex(), cell.getColumnIndex());
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    return cell.getStringCellValue();
                case Cell.CELL_TYPE_NUMERIC:
                    return Double.toString(cell.getNumericCellValue());
                case Cell.CELL_TYPE_BOOLEAN:
                    return Boolean.toString(cell.getBooleanCellValue());
                default:
                    throw new TdarRecoverableRuntimeException("sheetEvaluator.parse_error", errors);
            }
        } catch (RuntimeException re) {
            throw new TdarRecoverableRuntimeException("sheetEvaluator.parse_excel_error_unknown_type", "sheetEvaluator.parse_excel_error_url",
                    errors);
        }
    }

    public int getMaxCellCount() {
        return maxCellCount;
    }

    public int getStartAt() {
        return dataRowStartIndex;
    }

    public int getHeaderRowIndex() {
        return headerRowIndex;
    }
    
    private void initializeHeaders(DataRow dataRow) {
        if (dataRow == null) {
            // generate column names given the max number of data values we need to store
            headerColumnNames = new ArrayList<String>();
            for (int i = dataColumnStartIndex; i <= dataColumnEndIndex; i++) {
                headerColumnNames.add("Column #" + (i+ 1));
            }
        }
        else {
            this.headerColumnNames = dataRow.extractHeaders(dataColumnStartIndex, dataColumnEndIndex);
            setHeaderRowIndex(dataRow.getRowIndex());
        }
    }

    public void setHeaderRowIndex(int headerRowIndex) {
        this.headerRowIndex = headerRowIndex;
        // FIXME: assumption that data rows start immediately after a header row
        dataRowStartIndex = headerRowIndex + 1;
    }

    public List<String> getHeaderColumnNames() {
        return headerColumnNames;
    }

    public int getDataRowStartIndex() {
        return dataRowStartIndex;
    }

    public int getDataColumnStartIndex() {
        return dataColumnStartIndex;
    }

    public int getDataColumnEndIndex() {
        return dataColumnEndIndex;
    }
    
    public boolean hasHeaders() {
        return headerRowIndex != -1 && CollectionUtils.isNotEmpty(headerColumnNames);
    }

    public boolean hasTabularData() {
        // FIXME: this needs to be more sophisticated and based on analysis of multiple rows
        return dataColumnEndIndex > dataColumnStartIndex;
    }
    
    public boolean isDegenerate() {
        return dataColumnEndIndex < maxCellCount 
                || ! hasHeaders()
                || headerColumnNames.size() < dataColumnEndIndex;
    }
    
    @Override
    public String toString() {
        return String.format("header row %d data row %d - [%d, %d]", headerRowIndex, dataRowStartIndex, dataColumnStartIndex, dataColumnEndIndex); 
    }

    private static boolean hasAlphabeticCharacters(String value) {
        return ALPHABETIC_PATTERN.matcher(value).find();
    }

    private class DataRow {
        // maximum number of blanks allowable for a row to still be considered a header.
        private static final int HEADER_MAX_BLANKS_THRESHOLD = 2;
        // excludes very long strings from being considered header material  
        private static final double HEADER_AVG_DATA_LENGTH_THRESHOLD = 30.0d;
        // only look for headers in rows 0 -> this threshold 
        private static final int HEADER_ROW_INDEX_THRESHOLD = 10;
        private final Row row;
        private final int columnStartIndex;
        private int columnEndIndex;
        // number of non-blank data values in this row
        private int numberOfDataValues = 0;
        // number of data values in this row with at least one alphabetic character
        private int numberOfAlphabeticValues = 0;
        private double averageDataValueLength = 0.0d;

        public DataRow(Row row) {
            this.row = row;
            columnStartIndex = Math.max(-1, row.getFirstCellNum());
            int totalDataValueLength = 0;
            for (Cell cell : row) {
                String value = getCellValueAsString(cell);
                if (StringUtils.isNotBlank(value)) {
                    totalDataValueLength += value.length();
                    columnEndIndex = cell.getColumnIndex();
                    numberOfDataValues++;
                }
                if (hasAlphabeticCharacters(value)) {
                    numberOfAlphabeticValues++;
                }
            }
            averageDataValueLength = totalDataValueLength / (double) numberOfDataValues;
        }

        /**
         * Returns a list of Strings based on the assumption that this Row is the appropriate
         * header for the given Sheet. If data values in a given cell are empty / missing from 
         * this row, it will generate a header name "Column #N" where N is the cell index of the missing value + 1.
         *  
         * @param dataColumnStartIndex 
         * @param dataColumnEndIndex
         * @return
         */
        public List<String> extractHeaders(int dataColumnStartIndex, int dataColumnEndIndex) {
            ArrayList<String> headers = new ArrayList<String>();
            for (int ci = dataColumnStartIndex; ci <= dataColumnEndIndex; ci++) {
                String value = getCellValueAsString(row.getCell(ci));
                if (StringUtils.isBlank(value)) {
                    value = "Column #" + (ci + 1);
                }
                headers.add(value);
            }
            return headers;
        }

        public int getRowIndex() {
            return row.getRowNum();
        }

        public int getNumberOfDataValues() {
            return numberOfDataValues;
        }

        public boolean hasData() {
            return numberOfDataValues > 0;
        }
        
        public int getMaxCellCount() {
            return row.getLastCellNum();
        }

        public int getColumnEndIndex() {
            return columnEndIndex;
        }
        
        public int getNumberOfBlankValues() {
            return columnEndIndex - numberOfDataValues;
        }

        /**
         * Returns the percentage of data values with at least one alphabetic character
         * out of all non-blank data values in the row. For example, given H1, H2, H3, H4, 27, 29 
         * it would report 4/6 (in double representation). 
         *  
         * @return
         */
        private double percentageOfAlphabeticDataValues() {
            return numberOfAlphabeticValues / (double) numberOfDataValues;
        }

        public boolean isHeaderish() {
            return hasData()
                    && percentageOfAlphabeticDataValues() > ALPHABETIC_DATA_DENSITY_THRESHOLD
                    && averageDataValueLength < HEADER_AVG_DATA_LENGTH_THRESHOLD
                    && getNumberOfBlankValues() < HEADER_MAX_BLANKS_THRESHOLD
                    && getRowIndex() < HEADER_ROW_INDEX_THRESHOLD;
        }

        @Override
        public String toString() {
            return String.format("row %d, column range [%d, %d], alpha values %d, data values %d, blank values %d, alphabet ratio %f, average data value length %f",
                    getRowIndex(), columnStartIndex, columnEndIndex, numberOfAlphabeticValues, numberOfDataValues, getNumberOfBlankValues(), percentageOfAlphabeticDataValues(), averageDataValueLength);

        }

        public int getColumnStartIndex() {
            return columnStartIndex;
        }

    }

}
