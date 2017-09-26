package org.tdar.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;

public class ExcelUnit {

    private Workbook workbook;
    private Sheet sheet;
    private DataFormatter formatter = new HSSFDataFormatter();
    private FormulaEvaluator formulaEvaluator;

    public void open(File file) {
        try {
            setWorkbook(WorkbookFactory.create(new FileInputStream(file)));
            setFormulaEvaluator(getWorkbook().getCreationHelper().createFormulaEvaluator());

        } catch (Exception e) {
            Assert.fail("could not open file: " + file.getName() + " :" + e);
        }
    }

    public void open(InputStream inputStream) {
        try {
            WorkbookFactory.create(inputStream);
            setFormulaEvaluator(getWorkbook().getCreationHelper().createFormulaEvaluator());

        } catch (Exception e) {
            Assert.fail("could not open input stream");
        }
    }

    public void assertCellEquals(int row, int column, Object obj) {
        Cell cell = getCell(row, column);
        Assert.assertEquals(obj.toString(), getCellAsString(cell));
    }

    public void assertCellCommentEquals(int row, int column, Object obj) {
        Cell cell = getCell(row, column);
        if (cell == null) {
            Assert.fail("cell is null");
        }
        if (cell.getCellComment() == null) {
            Assert.fail("cell does not have a comment");
        }
        Assert.assertEquals(obj.toString(), cell.getCellComment().getString().toString());
    }

    private String getCellAsString(Cell cell) {
        return formatter.formatCellValue(cell, formulaEvaluator);
    }

    public void assertCellContains(int row, int column, Object obj) {
        Cell cell = getCell(row, column);
        String value = getCellAsString(cell);
        Assert.assertTrue(value.contains(obj.toString()));
    }

    public void assertRowContains(int rowNum, Object obj) {
        boolean seen = rowContains(rowNum, obj);
        Assert.assertTrue(String.format("row did not contain %s", obj), seen);
    }

    public void assertRowDoesNotContain(int rowNum, Object obj) {
        boolean seen = rowContains(rowNum, obj);
        Assert.assertFalse(String.format("row did contain %s", obj), seen);
    }

    private boolean rowContains(int rowNum, Object obj) {
        boolean seen = false;
        Row row = sheet.getRow(rowNum);
        for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
            if (cellContains(rowNum, i, obj)) {
                seen = true;
            }
        }
        return seen;
    }

    public void assertCellContainsIgnoreCase(int row, int column, Object obj) {
        Assert.assertFalse(String.format("cell does not contain (caseInsensitive): %s | %s", obj, getCell(row, column).getStringCellValue()),
                cellContainsIgnoreCase(row, column, obj));
    }

    private boolean cellContainsIgnoreCase(int row, int column, Object obj) {
        Cell cell = getCell(row, column);
        String value = getCellAsString(cell).toLowerCase();
        return value.contains(obj.toString().toLowerCase());
    }

    private boolean cellContains(int row, int column, Object obj) {
        Cell cell = getCell(row, column);
        String value = getCellAsString(cell);
        return value.contains(obj.toString());
    }

    public void assertCellDoesNotContainIgnoreCase(int row, int column, Object obj) {
        Assert.assertFalse(String.format("cell does not contain: %s", obj), cellContainsIgnoreCase(row, column, obj));
    }

    private boolean isCellBold(Cell cell) {
        try {
            return sheet.getWorkbook().getFontAt(cell.getCellStyle().getFontIndex()).getBold();
        } catch (Exception e) {
            return false;
        }
    }

    public void assertCellIsBold(Cell cell) {
        Assert.assertTrue("cell should be bold", isCellBold(cell));
    }

    public void assertCellNotBold(Cell cell) {
        Assert.assertFalse("cell should not be bold", isCellBold(cell));
    }

    public void assertCellIsItalic(Cell cell) {
        try {
            Assert.assertTrue("cell is not italic", sheet.getWorkbook().getFontAt(cell.getCellStyle().getFontIndex()).getItalic());
        } catch (Exception e) {
            Assert.fail("cell is not italic");
        }
    }

    public void assertCellIsSizeInPoints(Cell cell, short size) {
        String message = "cell size is not " + size;
        try {
            Assert.assertEquals(message, size, sheet.getWorkbook().getFontAt(cell.getCellStyle().getFontIndex()).getFontHeightInPoints());
        } catch (Exception e) {
            Assert.fail(message);
        }
    }

    public void assertCellEqualsString(int row, int column, String obj) {
        Cell cell = getCell(row, column);
        Assert.assertEquals(formulaEvaluator.evaluate(cell).getStringValue(), obj);
    }

    @SuppressWarnings("deprecation")
    public void assertCellEqualsNumeric(int row, int column, double obj) {
        Cell cell = getCell(row, column);
        assertEquals(formulaEvaluator.evaluate(cell).getNumberValue(), obj);
    }

    public void assertCellEqualsBoolean(int row, int column, boolean obj) {
        Cell cell = getCell(row, column);
        Assert.assertEquals(formulaEvaluator.evaluate(cell).getBooleanValue(), obj);
    }

    public void assertCellIsNumbericType(int row, int column) {
        assertCellType(getCell(row, column), Cell.CELL_TYPE_NUMERIC);
    }

    public void assertCellIsString(int row, int column) {
        assertCellType(getCell(row, column), Cell.CELL_TYPE_STRING);
    }

    public void assertCellIsFormula(int row, int column) {
        assertCellType(getCell(row, column), Cell.CELL_TYPE_FORMULA);
    }

    private void assertCellType(Cell cell, int type) {
        switch (type) {
            case Cell.CELL_TYPE_BLANK:
                if (type != cell.getCellType()) {
                    Assert.fail("cell was not 'blank'");
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                if (type != cell.getCellType()) {
                    Assert.fail("cell was not 'boolean'");
                }
                break;
            case Cell.CELL_TYPE_ERROR:
                if (type != cell.getCellType()) {
                    Assert.fail("cell was not 'error'");
                }
                break;
            case Cell.CELL_TYPE_FORMULA:
                if (type != cell.getCellType()) {
                    Assert.fail("cell was not 'formula'");
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (type != cell.getCellType()) {
                    Assert.fail("cell was not 'numeric'");
                }
                break;
            case Cell.CELL_TYPE_STRING:
                if (type != cell.getCellType()) {
                    Assert.fail("cell was not 'string'");
                }
                break;
        }
    }

    public Cell getCell(int row, int column) {
        if (sheet == null) {
            selectSheet(0);
        }
        Cell cell = sheet.getRow(row).getCell(column);
        return cell;
    }

    public void selectSheet(String name) {
        try {
            setSheet(workbook.getSheet(name));
            Assert.assertNotNull("could not find sheet: " + name, sheet);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    public void selectSheet(int number) {
        try {
            setSheet(workbook.getSheetAt(number));
            Assert.assertNotNull("could not find sheet number: " + number, sheet);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public FormulaEvaluator getFormulaEvaluator() {
        return formulaEvaluator;
    }

    public void setFormulaEvaluator(FormulaEvaluator formulaEvaluator) {
        this.formulaEvaluator = formulaEvaluator;
    }

    public void assertRowIsEmpty(int i) {
        Assert.assertTrue("row should be empty", isRowEmpty(i));
    }

    public void assertRowNotEmpty(int i) {
        Assert.assertFalse("row should not be empty", isRowEmpty(i));
    }

    private boolean isRowEmpty(int i) {
        Row row = getSheet().getRow(i);
        if (row == null) {
            return true;
        }
        for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
            if (!isCellEmpty(row.getCell(j))) {
                return false;
            }
        }
        return true;
    }

    private boolean isCellEmpty(Cell cell) {
        return StringUtils.isBlank(getCellAsString(cell));
    }
}
