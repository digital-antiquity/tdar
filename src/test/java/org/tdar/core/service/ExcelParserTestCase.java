package org.tdar.core.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.service.excel.SheetEvaluator;

public class ExcelParserTestCase {

    @Test
    public void testShortExcelFile() throws InvalidFormatException, FileNotFoundException, IOException {
        SheetEvaluator evaluator = new SheetEvaluator();
        File f = new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "weird_column_headings.xlsx");
        Workbook workbook = WorkbookFactory.create(new FileInputStream(f));
        evaluator.evaluateBeginning(workbook.getSheetAt(0), 25);
        assertEquals(0, evaluator.getStartAt());
        assertEquals(3, evaluator.getMaxCellCount());
    }

    @Test
    public void testDegenerateExcelFile() throws InvalidFormatException, FileNotFoundException, IOException {
        SheetEvaluator evaluator = new SheetEvaluator();
        File f = new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "PFRAA_fake_Ferengi_trading_post_data_for tDAR test.xls");
        Workbook workbook = WorkbookFactory.create(new FileInputStream(f));
        evaluator.evaluateBeginning(workbook.getSheetAt(0), 25);
        assertEquals(1, evaluator.getStartAt());
        assertEquals(0, evaluator.getMaxCellCount());
        evaluator = new SheetEvaluator();
        evaluator.evaluateBeginning(workbook.getSheetAt(1), 25);
        assertEquals(0, evaluator.getStartAt());
        assertEquals(34, evaluator.getMaxCellCount());
        // assertEquals(0, evaluator.getStartAt());
        // assertEquals(4, evaluator.getMaxCount());
    }

    @Test
    public void testExcelFileWithHeaders() throws InvalidFormatException, FileNotFoundException, IOException {
        SheetEvaluator evaluator = new SheetEvaluator();
        File f = new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "Test_header_rows.xls");
        Workbook workbook = WorkbookFactory.create(new FileInputStream(f));
        evaluator.evaluateBeginning(workbook.getSheetAt(0), 25);
        assertEquals(0, evaluator.getStartAt());
        assertEquals(34, evaluator.getMaxCellCount());
        evaluator = new SheetEvaluator();
        evaluator.evaluateBeginning(workbook.getSheetAt(1), 25);
        assertEquals(2, evaluator.getStartAt());
        assertEquals(34, evaluator.getMaxCellCount());
        evaluator.evaluateBeginning(workbook.getSheetAt(2), 25);
        assertEquals(6, evaluator.getStartAt());
        assertEquals(34, evaluator.getMaxCellCount());
        evaluator.evaluateBeginning(workbook.getSheetAt(3), 25);
        assertEquals(6, evaluator.getStartAt());
        assertEquals(34, evaluator.getMaxCellCount());
    }

    @Test
    public void testANother() throws InvalidFormatException, FileNotFoundException, IOException {
        SheetEvaluator evaluator = new SheetEvaluator();
        File f = new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "dates-from-wadh-lang-o.xls");
        Workbook workbook = WorkbookFactory.create(new FileInputStream(f));
        evaluator.evaluateBeginning(workbook.getSheetAt(0), 25);
        assertEquals(1, evaluator.getStartAt());
        assertEquals(7, evaluator.getMaxCellCount());
    }

}
