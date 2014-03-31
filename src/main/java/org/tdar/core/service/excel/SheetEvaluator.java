package org.tdar.core.service.excel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/*
 * The goal of this class is to evaluate the beginning of a worksheet and try and figure out where the
 * "data" starts.  This could be the first row, or there, as in many cases are, a few header rows ahead
 * which may confuse things.  This tries to read the file from bottom to top trying to find the "END"
 * of the data and then use that to find the actual headers that we care about.
 */
public class SheetEvaluator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private int startAt = ExcelService.FIRST_ROW;
    private int maxCellCount = -1;
    private FormulaEvaluator evaluator;
    private DataFormatter formatter = new HSSFDataFormatter();

    public void evaluateBeginning(Sheet currentSheet, int endAt) {

        setStartAt(0);
        Map<Integer, Integer> rowstats = new HashMap<Integer, Integer>();

        // if we're looking at something longer
        if (currentSheet.getLastRowNum() < endAt) {
            endAt = currentSheet.getLastRowNum();
        }
        List<Integer> skip = new ArrayList<Integer>();

        // IF WE HAVE ONLY A FEW MERGED REGIONS AND THE REGION IS SMALL, BUT IS IN THE HEADER
        // THEN, SKIP IT
        if (currentSheet.getNumMergedRegions() > 0 && currentSheet.getNumMergedRegions() < 5) {
            for (int i = 0; i < currentSheet.getNumMergedRegions(); i++) {
                CellRangeAddress mergedRegion = currentSheet.getMergedRegion(i);
                int firstRow = mergedRegion.getFirstRow();
                int lastRow = mergedRegion.getLastRow();
                if (lastRow < 10) {
                    for (int j = firstRow; j <= lastRow; j++) {
                        skip.add(j);
                    }
                }
            }
        }

        evaluator = currentSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

        for (int i = endAt; i >= ExcelService.FIRST_ROW; i--) {
            Row row = currentSheet.getRow(i);
            if (row == null) {
                continue;
            }
            int cellCount = (int) row.getLastCellNum();
            // EVALUATE FOR EMPTY CELLS -- TEST SANITY
            cellCount = evaluateForBlankCells(row, 0, cellCount);

            // if the cellCount is greater than or equal to the current, then reset the start
            if (cellCount >= getMaxCellCount() && !skip.contains(i)) {
                setStartAt(i);
            }

            if (getMaxCellCount() < cellCount) { // find the greatest number of rows
                setMaxCellCount(cellCount);
            }
            Integer currentCount = rowstats.get(cellCount);
            if (currentCount == null) {
                currentCount = 0;
            }
            currentCount++;
            rowstats.put(cellCount, currentCount);
        }
        logger.debug(String.format("excel file starts at: %s; maxCount:%s ", getStartAt(), getMaxCellCount()));
    }

    public void evaluateForBlankCells(Row row, int startColumnIndex) {
        // is the first cell before the first column with a heading?
        if (row.getFirstCellNum() < startColumnIndex) {
            throwTdarRecoverableRuntimeException(row.getRowNum(), row.getFirstCellNum(), startColumnIndex, row.getSheet().getSheetName());
        }
        // is the last cell after the last column with a heading?
        if (getMaxCellCount() < row.getLastCellNum()) {
            int countAbove = evaluateForBlankCells(row, getMaxCellCount(), row.getLastCellNum());
            if (countAbove > getMaxCellCount()) {
                throwTdarRecoverableRuntimeException(row.getRowNum(), row.getLastCellNum(), getMaxCellCount() + 1, row.getSheet().getSheetName());
            }
        }
    }

    private void throwTdarRecoverableRuntimeException(int rowNumber, short offendingColumnIndex, int columnNameBound, String sheetName) {
        throw new TdarRecoverableRuntimeException("sheetEvaluator.row_has_more_columns", "sheetEvaluator.row_has_more_columns_url", Arrays.asList(rowNumber, offendingColumnIndex,columnNameBound,sheetName));
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
        List<?> errors =Arrays.asList(cell.getRowIndex() +1, cell.getColumnIndex()+1);
        try {

            if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
                 throw new TdarRecoverableRuntimeException("sheetEvaluator.parse_excel_error", "sheetEvaluator.parse_excel_error_url", errors);
            }

            return formatter.formatCellValue(cell, evaluator);
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
                    Arrays.asList(cell.getRowIndex() + 1, cell.getColumnIndex() + 1));
        }
    }

    public int getFirstNonHeaderRow() {
        return startAt;
    }

    public int getMaxCellCount() {
        return maxCellCount;
    }

    public void setMaxCellCount(int maxCount) {
        this.maxCellCount = maxCount;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }
}
