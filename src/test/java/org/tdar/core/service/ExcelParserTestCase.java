package org.tdar.core.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.service.excel.SheetEvaluator;

public class ExcelParserTestCase {

    @Test
    public void testShortExcelFile() throws Exception {
        FileInputStream fis = getDataIntegrationResource("weird_column_headings.xlsx");
        Workbook workbook = WorkbookFactory.create(fis);
        SheetEvaluator evaluator = new SheetEvaluator(workbook.getSheetAt(0));
        assertEquals(1, evaluator.getDataRowStartIndex());
        assertEquals(3, evaluator.getDataColumnEndIndex());
        fis.close();
    }
    
    @Test
    public void testMissingHeaderColumnNames() throws Exception {
        FileInputStream fis = getDataIntegrationResource("no_first_column_name.xlsx");
        Workbook workbook = WorkbookFactory.create(fis);
        SheetEvaluator evaluator = new SheetEvaluator(workbook.getSheetAt(0));
        assertTrue(evaluator.hasHeaders());
        assertNotNull(evaluator.getHeaderColumnNames());
    }

    @Test
    public void testDegenerateExcelFile() throws Exception {
        FileInputStream fis = getDataIntegrationResource("PFRAA_fake_Ferengi_trading_post_data_for tDAR test.xls");
        Workbook workbook = WorkbookFactory.create(fis);
        SheetEvaluator evaluator = new SheetEvaluator(workbook.getSheetAt(0));
        assertEquals(0, evaluator.getDataRowStartIndex());
        assertEquals(0, evaluator.getDataColumnStartIndex());
        assertEquals(1, evaluator.getDataColumnEndIndex());
        assertEquals(2, evaluator.getHeaderColumnNames().size());
        assertTrue(evaluator.hasTabularData());
        evaluator.evaluate(workbook.getSheetAt(1));
        assertEquals(1, evaluator.getDataRowStartIndex());
        assertEquals(0, evaluator.getDataColumnStartIndex());
        assertEquals(34, evaluator.getDataColumnEndIndex());
        assertTrue(evaluator.hasHeaders());
        assertTrue(evaluator.hasTabularData());
        fis.close();
        // assertEquals(0, evaluator.getStartAt());
        // assertEquals(4, evaluator.getMaxCount());
    }

    @Test
    public void testExcelFileWithHeaders() throws Exception {        
        FileInputStream fis = getDataIntegrationResource("Test_header_rows.xls");
        Workbook workbook = WorkbookFactory.create(fis);
        SheetEvaluator evaluator = new SheetEvaluator(workbook.getSheetAt(0));
        List<String> headers = evaluator.getHeaderColumnNames();
        assertEquals(35, headers.size());
        assertEquals(1, evaluator.getDataRowStartIndex());
        assertEquals(0, evaluator.getDataColumnStartIndex());
        assertEquals(34, evaluator.getDataColumnEndIndex());
        assertEquals(headers.size(), evaluator.getDataColumnEndIndex() + 1);
        evaluator.evaluate(workbook.getSheetAt(1));
        assertEquals(headers, evaluator.getHeaderColumnNames());
        assertEquals("Second sheet starts header at row 1", 3, evaluator.getDataRowStartIndex());
        assertEquals(0, evaluator.getDataColumnStartIndex());
        assertEquals(34, evaluator.getDataColumnEndIndex());
        assertEquals(evaluator.getHeaderColumnNames().size(), evaluator.getDataColumnEndIndex() + 1);
        evaluator.evaluate(workbook.getSheetAt(2));
        assertEquals("Third sheet starts header at row 6", 7, evaluator.getDataRowStartIndex());
        assertEquals(headers, evaluator.getHeaderColumnNames());
        evaluator.evaluate(workbook.getSheetAt(3), 25);
        assertEquals(7, evaluator.getDataRowStartIndex());
        assertEquals(34, evaluator.getDataColumnEndIndex());
        fis.close();
    }

    @Test
    public void testSheetEvaluatorDatesFromWadhLang() throws Exception {
        FileInputStream fis = getDataIntegrationResource("dates-from-wadh-lang-o.xls");
        Workbook workbook = WorkbookFactory.create(fis);
        SheetEvaluator evaluator = new SheetEvaluator(workbook.getSheetAt(0));
        assertEquals(2, evaluator.getDataRowStartIndex());
        assertEquals(8, evaluator.getHeaderColumnNames().size());
        assertEquals(2, evaluator.getDataRowStartIndex());
        assertEquals(0, evaluator.getDataColumnStartIndex());
        assertEquals(7, evaluator.getDataColumnEndIndex());
        fis.close();
    }
    
    public void testTooManyColumns() throws Exception {
        FileInputStream fis = getDataIntegrationResource("too-many-columns.xlsx");
        Workbook workbook = WorkbookFactory.create(fis);
        SheetEvaluator evaluator = new SheetEvaluator(workbook.getSheetAt(0));
        assertEquals(1, evaluator.getDataRowStartIndex());
        fis.close();
    }
    
    private FileInputStream getDataIntegrationResource(String filename) throws Exception {
        return new FileInputStream(new File(TestConstants.TEST_DATA_INTEGRATION_DIR, filename));
    }

}
