/**
 * I'm not really testing POI,  but rather my understanding of it.   This file will most likely go away...
 */
package org.tdar.utils.sensorydata;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.sensory.ScannerTechnologyType;
import org.tdar.core.bean.resource.sensory.SensoryDataScan;
import org.tdar.utils.sensorydata.enums.ScanField;

public class AdsTemplateTest {

    private static final String PATH = TestConstants.TEST_ROOT_DIR;
    private static final String PATH_ADS_TEMPLATE1 = PATH + "sensory/ark_hm_0060.xlsx";
    private static final String PATH_ADS_TEMPLATE2 = PATH + "sensory/ark_hm_0073.xlsx";
    private static final String PATH_ADS_TEMPLATE3 = PATH + "sensory/ark_hm_0234.xlsx";
    
    private static final String SHEETNAMES_PROJECT = "1-Project Description";
    
    private DataFormatter formatter = new HSSFDataFormatter();
    

    
    private Workbook loadWorkbook(String path) throws InvalidFormatException, FileNotFoundException, IOException {
        File file = new File(path);
        Workbook book = WorkbookFactory.create(new FileInputStream(file));
        return book;
    }

    
    private SensoryData loadThenProcess(String path) throws Exception {
        File file = new File(path);
        AdsTemplateProcessor processor = new AdsTemplateProcessor();
        SensoryData sd = processor.getSensoryDataFromAdsTemplate(file);
        //TODO: need an easier way to test this (like a hashmap + reflection)
        return sd;
    }
    
    
    
    @Test
    public void testFileOpen() throws Exception{
        Workbook book = loadWorkbook(PATH_ADS_TEMPLATE1);
        Sheet projectSheet = book.getSheet(SHEETNAMES_PROJECT);
        Assert.assertNotNull(projectSheet);
    }
    
    
    @Test
    public void testGetPairs() throws Exception{
        Workbook book = loadWorkbook(PATH_ADS_TEMPLATE2);
        FormulaEvaluator evaluator = book.getCreationHelper().createFormulaEvaluator();
        Sheet sheet = book.getSheet(SHEETNAMES_PROJECT);
        Assert.assertNotNull(sheet);
        Row row = sheet.getRow(sheet.getFirstRowNum());
        Cell cell = row.getCell(0);
        assertEquals("expecting first key", "Project Name", cell.getStringCellValue());
    }
    
    @Test
    public void testScanEnumLookup() {
        String label = "Lense or FOV Details (Triangulation scans only)";
        assertEquals(ScanField.LENSE_OR_FOV_DETAILS, ScanField.fromLabel(label));
    }
    
    
    @Test
    public void testSensoryDataProjectInfo() throws Exception{
        SensoryData sensoryData = loadThenProcess(PATH_ADS_TEMPLATE1);
        assertEquals("Ark_HM_0060", sensoryData.getTitle() );
        assertEquals(new Double(0.377), sensoryData.getEstimatedDataResolution());
        assertEquals("NA", sensoryData.getAdditionalProjectNotes());
        assertTrue("survey date should be 2008",  sensoryData.getDate().equals(2008));
        for(SensoryDataScan scan : sensoryData.getSensoryDataScans()) {
            assertEquals(ScannerTechnologyType.TRIANGULATION, scan.getScannerTechnology());
        }
    }
    
    @Test
    @Ignore
    public void testExport() throws Exception {
//        SensoryDataExporter exporter = new SensoryDataExporter();
//        PrintWriter pw = new PrintWriter(System.out);
//        SensoryData sensoryData = loadThenProcess(PATH_ADS_TEMPLATE1);
//        exporter.export(sensoryData, pw);
//        pw.flush();
//        System.out.println("hi");
        
    }
    
    
    
    
}
