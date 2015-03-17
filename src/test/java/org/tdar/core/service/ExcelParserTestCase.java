package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.core.service.excel.SheetEvaluator;
import org.tdar.utils.MathUtils;

public class ExcelParserTestCase {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testShortExcelFile() throws Exception {
        FileInputStream fis = getDataIntegrationResource("weird_column_headings.xlsx");
        Workbook workbook = WorkbookFactory.create(fis);
        SheetEvaluator evaluator = new SheetEvaluator(workbook.getSheetAt(0));
        assertEquals(1, evaluator.getDataRowStartIndex());
        assertEquals(3, evaluator.getDataColumnEndIndex());
        verifyGeneratedColumnNames(evaluator, false);
        fis.close();
    }
    

    @Test
    public void testFormat() {
        String st = "-1.0";
        logger.debug("{}",NumberUtils.isNumber(st));
        double d = NumberUtils.toDouble(st);
        logger.debug("{}", d);
    }
    
    @Test
    public void testMissingHeaderColumnNames() throws Exception {
        FileInputStream fis = getDataIntegrationResource("no_first_column_name.xlsx");
        Workbook workbook = WorkbookFactory.create(fis);
        SheetEvaluator evaluator = new SheetEvaluator(workbook.getSheetAt(0));
        assertTrue(evaluator.hasHeaders());
        List<String> headers = evaluator.getHeaderColumnNames();
        assertNotNull(headers);
        assertEquals(evaluator.getDataColumnEndIndex() + 1, headers.size());
        assertEquals(1, evaluator.getDataRowStartIndex());
        assertEquals(0, evaluator.getDataColumnStartIndex());
        assertEquals(3, evaluator.getDataColumnEndIndex());
        assertEquals("Should have autogenerated column 1 header to replace missing column", "Column #1", headers.get(0));
        fis.close();
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
        verifyGeneratedColumnNames(evaluator, true);
        evaluator.evaluate(workbook.getSheetAt(1));
        assertEquals(1, evaluator.getDataRowStartIndex());
        assertEquals(0, evaluator.getDataColumnStartIndex());
        assertEquals(34, evaluator.getDataColumnEndIndex());
        assertTrue(evaluator.hasHeaders());
        assertTrue(evaluator.hasTabularData());
        fis.close();
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
        verifyGeneratedColumnNames(evaluator, false);
        evaluator.evaluate(workbook.getSheetAt(1));
        assertEquals(headers, evaluator.getHeaderColumnNames());
        assertEquals("Second sheet starts header at row 1", 3, evaluator.getDataRowStartIndex());
        assertEquals(0, evaluator.getDataColumnStartIndex());
        assertEquals(34, evaluator.getDataColumnEndIndex());
        assertEquals(evaluator.getHeaderColumnNames().size(), evaluator.getDataColumnEndIndex() + 1);
        verifyGeneratedColumnNames(evaluator, false);
        evaluator.evaluate(workbook.getSheetAt(2));
        assertEquals("Third sheet starts header at row 6", 7, evaluator.getDataRowStartIndex());
        assertEquals(headers, evaluator.getHeaderColumnNames());
        evaluator.evaluate(workbook.getSheetAt(3), 25);
        assertEquals(7, evaluator.getDataRowStartIndex());
        assertEquals(34, evaluator.getDataColumnEndIndex());
        verifyGeneratedColumnNames(evaluator, false);
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
    
    @Test
    public void testTooManyColumns() throws Exception {
        FileInputStream fis = getDataIntegrationResource("too-many-columns.xlsx");
        Workbook workbook = WorkbookFactory.create(fis);
        SheetEvaluator evaluator = new SheetEvaluator(workbook.getSheetAt(0));
        assertEquals(1, evaluator.getDataRowStartIndex());
        assertEquals(20, evaluator.getDataColumnEndIndex());
        assertEquals(228, evaluator.getMaxCellCount());
        assertNotNull(evaluator.getHeaderColumnNames());
        assertEquals(evaluator.getDataColumnEndIndex() + 1, evaluator.getHeaderColumnNames().size());
        verifyGeneratedColumnNames(evaluator, false);
        logger.debug("{}", evaluator.getHeaderColumnNames());
        assertTrue(evaluator.hasTabularData());
        assertTrue(evaluator.isDegenerate());
        fis.close();
    }
    
    private void verifyGeneratedColumnNames(SheetEvaluator evaluator, boolean generated) {
        List<String> headerColumnNames = evaluator.getHeaderColumnNames();
        for (int i = 0; i <= evaluator.getDataColumnEndIndex(); i++) {
            String autogeneratedColumnName = "Column #" + (i + 1);
            if (generated) {
                assertTrue(headerColumnNames.contains(autogeneratedColumnName));
            }
            else {
                assertFalse(headerColumnNames.contains(autogeneratedColumnName));
            }
        }
    }
    
    private FileInputStream getDataIntegrationResource(String filename) throws Exception {
        return new FileInputStream(new File(TestConstants.TEST_DATA_INTEGRATION_DIR, filename));
    }

}
