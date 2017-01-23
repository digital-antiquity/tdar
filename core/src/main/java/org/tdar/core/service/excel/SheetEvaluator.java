package org.tdar.core.service.excel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ExcelWorkbookWriter;

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
    private final Logger logger = LoggerFactory.getLogger(getClass());
    // 0-based row index of the header, -1 if no headers found.
    private int headerRowIndex = -1;
    private List<String> headerColumnNames;
    // 0-based row index specifying where the actual data begins, after the header row
    private int dataRowStartIndex = ExcelWorkbookWriter.FIRST_ROW;
    // 0-based column index specifying where the actual data begins
    private int dataColumnStartIndex = ExcelWorkbookWriter.FIRST_COLUMN;
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
        dataRowStartIndex = ExcelWorkbookWriter.FIRST_ROW;
        dataColumnStartIndex = ExcelWorkbookWriter.FIRST_COLUMN;
        dataColumnEndIndex = -1;
    }

    public void evaluate(Sheet sheet) {
        evaluate(sheet, DEFAULT_ROWS_TO_EVALUATE);
    }

    public void evaluate(Sheet sheet, int endRow_) {
        int endRow = endRow_;
        logger.trace("evaluating sheet {}", sheet.getSheetName());
        if (headerRowIndex != -1) {
            reset();
        }
        // clamp endRow if the sheet doesn't have enough rows to scan
        endRow = Math.min(endRow, sheet.getLastRowNum());
        // locate and skip any merged regions at the start of the sheet
        // look for something headerish in the data and develop some heuristics based on how many
        // rows of consistent data we can find.
        formulaEvaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        List<DataRow> dataRows = new ArrayList<>();

        for (int rowIndex = ExcelWorkbookWriter.FIRST_ROW; rowIndex < endRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            boolean skipRow = evaluateRowForMergedRegions(sheet, rowIndex, row);

            if (skipRow) {
                continue;
            }
            // if (mergedRegionRowIndexes.contains(rowIndex)) {
            // // this row is a merged region that we can't sensibly use for anything at all
            // logger.debug("skipping merged region row index {}", rowIndex);
            // continue;
            // }
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
                // min-maxing data column start, end index bounds and maxing the total number of reported cells
                dataColumnStartIndex = Math.min(dataColumnStartIndex, dataRow.getColumnStartIndex());
                dataColumnEndIndex = Math.max(dataColumnEndIndex, dataRow.getColumnEndIndex());
                maxCellCount = Math.max(maxCellCount, dataRow.getMaxCellCount());
                if (dataRow.getNumberOfDataValues() > maxNumberOfDataValues) {
                    maxNumberOfDataValues = dataRow.getNumberOfDataValues();
                    // assumes that the row with the most actual data values is
                    // also a likely candidate for a header row.
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

    /**
     * Iterates over cells in merged regions and sets values to be the value of the merged region if
     * we're merging just a few columns together on a single row
     * 
     * @param sheet
     * @param rowIndex
     * @param row
     * @return
     */
    private boolean evaluateRowForMergedRegions(Sheet sheet, int rowIndex, Row row) {
        boolean skipRow = false;
        int numMergedRegions = sheet.getNumMergedRegions();
        for (int i = 0; i < numMergedRegions; i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            logger.trace("{},{}",region.getFirstRow(), region.getFirstColumn());

            // find overlap of current row in region
            if (rowIndex >= region.getFirstRow() && rowIndex <= region.getLastRow()) {
                Cell merged = sheet.getRow(region.getFirstRow()).getCell(region.getFirstColumn());
                // and skip if region is more than one row
                if (region.getLastRow() - region.getFirstRow() > 1) {
                    logger.debug("skipping merged region with multiple rows {}", rowIndex);
                    skipRow = true;
                    continue;
                }
                // if more than 3 columns
                if (region.getLastColumn() - region.getFirstColumn() > 3) {
                    logger.debug("skipping merged region with more than 3 columns {}", rowIndex);
                    skipRow = true;
                    continue;
                }
                for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
                    if (row.getCell(j) == null) {
                        continue;
                    }
                    row.getCell(j).setCellValue(formatter.formatCellValue(merged, formulaEvaluator));
                }
            }
        }
        return skipRow;
    }

    public void validate(Row row) {
        // is the first data cell index before the first header column index?
        if (row.getFirstCellNum() < dataColumnStartIndex) {
            // FIXME: an inappropriate exception message will be generated in this case.
            throwInvalidColumnCountException(row.getRowNum(), row.getFirstCellNum(), dataColumnStartIndex, row.getSheet().getSheetName());
        }
        // is the last data cell column index > the header's last column index?
        short lastDataCellIndex = getLastDataCellIndex(row);
        if (dataColumnEndIndex < lastDataCellIndex) {
            throwInvalidColumnCountException(row.getRowNum(), lastDataCellIndex + 1, dataColumnEndIndex + 1, row.getSheet().getSheetName());
        }
    }

    /**
     * Returns the last cell index with data (ignores blanks)
     * 
     * @param row
     * @return
     */
    public short getLastDataCellIndex(Row row) {
        for (short ci = row.getLastCellNum(); ci >= row.getFirstCellNum(); --ci) {
            if (StringUtils.isNotBlank(getCellValueAsString(row.getCell(ci)))) {
                return ci;
            }
        }
        return row.getFirstCellNum();
    }

    private void throwInvalidColumnCountException(int rowNumber, int numberOfDataColumns, int columnNameBound, String sheetName) {
        throw new TdarRecoverableRuntimeException("sheetEvaluator.row_has_more_columns", "sheetEvaluator.row_has_more_columns_url", Arrays.asList(rowNumber,
                numberOfDataColumns, columnNameBound, sheetName));
    }

    public String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        // setting up error array
        List<Object> errors = new ArrayList<>();
        errors.add(cell.getSheet().getSheetName());
        errors.add(CellReference.convertNumToColString(cell.getColumnIndex()) + (cell.getRowIndex() + 1));

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
        } catch (NotImplementedException nie) {
            String function = nie.getCause().getMessage();
            errors.add(function);
            throw new TdarRecoverableRuntimeException("sheetEvaluator.parse_excel_error_cannot_process_function", "sheetEvaluator.bad_function_url",
                    errors);
        } catch (RuntimeException re) {
            logger.debug("exception:", re);
            throw new TdarRecoverableRuntimeException("sheetEvaluator.parse_excel_error_unknown_type", "sheetEvaluator.parse_excel_error_url",
                    errors);
        }
    }

    public int getMaxCellCount() {
        return maxCellCount;
    }

    private void initializeHeaders(DataRow dataRow) {
        if (dataRow == null) {
            // generate column names given the max number of data values we need to store
            headerColumnNames = new ArrayList<String>();
            for (int i = dataColumnStartIndex; i <= dataColumnEndIndex; i++) {
                headerColumnNames.add("Column #" + (i + 1));
            }
        } else {
            headerColumnNames = dataRow.extractHeaders();
            setHeaderRowIndex(dataRow.getRowIndex());
        }
    }

    private void setHeaderRowIndex(int headerRowIndex) {
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

    /**
     * Returns true if this SheetEvaluator detected and extracted a header from the given Sheet.
     * 
     * @return
     */
    public boolean hasHeaders() {
        return headerRowIndex != -1 && CollectionUtils.isNotEmpty(headerColumnNames);
    }

    /**
     * Returns true if this sheet contains tabular data.
     * 
     * @return
     */
    public boolean hasTabularData() {
        // FIXME: this needs to be more sophisticated and based on analysis of multiple rows
        return dataColumnEndIndex > dataColumnStartIndex;
    }

    /**
     * Returns true if this sheet has "issues", e.g., if any of the following conditions are true:
     * 1. ending data index is less than the max number of reported cells
     * 2. no headers found
     * 3. number of header values is less than the max number of data values scanned (NOTE: this condition may never hold now that DataRow.extractHeaders()
     * fills in blank header columns).
     * 
     * @return
     */
    public boolean isDegenerate() {
        return dataColumnEndIndex < maxCellCount
                || !hasHeaders()
                || headerColumnNames.size() < (dataColumnEndIndex - dataColumnStartIndex);
    }

    @Override
    public String toString() {
        return String.format("header row %d data row %d - [%d, %d]", headerRowIndex, dataRowStartIndex, dataColumnStartIndex, dataColumnEndIndex);
    }

    private static boolean hasAlphabeticCharacters(String value) {
        return ALPHABETIC_PATTERN.matcher(value).find();
    }

    /**
     * Utility bookkeeping class that maintains heuristics and can extract headers from a given data row in the given Sheet.
     *
     */
    private class DataRow {
        // threshold ratio of alphabetic data values to total data values needed for a row to be considered headerish
        private static final double ALPHABETIC_DATA_DENSITY_THRESHOLD = 0.6d;
        // maximum percentage of blanks data values that a potential header row can have, currently set to 30%
        private static final double HEADER_MAX_BLANKS_THRESHOLD = 0.3d;
        // excludes very long strings from being considered header material
        private static final double HEADER_AVG_DATA_LENGTH_THRESHOLD = 30.0d;
        // only look for headers in rows 0 -> 10
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
            columnStartIndex = row.getFirstCellNum();
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
        public List<String> extractHeaders() {
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

        public int getTotalNumberOfCells() {
            return columnEndIndex - columnStartIndex;
        }

        public int getNumberOfBlankValues() {
            return getTotalNumberOfCells() - numberOfDataValues;
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

        private double percentageOfBlankValues() {
            return getNumberOfBlankValues() / (double) getTotalNumberOfCells();
        }

        public boolean isHeaderish() {
            return hasData()
                    && percentageOfAlphabeticDataValues() > ALPHABETIC_DATA_DENSITY_THRESHOLD
                    && averageDataValueLength < HEADER_AVG_DATA_LENGTH_THRESHOLD
                    && percentageOfBlankValues() <= HEADER_MAX_BLANKS_THRESHOLD
                    && getRowIndex() <= HEADER_ROW_INDEX_THRESHOLD;
        }

        @Override
        public String toString() {
            return String.format(
                    "row %d, column range [%d, %d], alpha values %d, data values %d, blank values %d, alphabet ratio %f, average data value length %f",
                    getRowIndex(), columnStartIndex, columnEndIndex, numberOfAlphabeticValues, numberOfDataValues, getNumberOfBlankValues(),
                    percentageOfAlphabeticDataValues(), averageDataValueLength);

        }

        public int getColumnStartIndex() {
            return columnStartIndex;
        }

    }

}
