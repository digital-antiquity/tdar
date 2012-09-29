package org.tdar.utils.sensorydata;

import java.io.File;

import static junit.framework.Assert.*;


public class ADSImportUtility {

    /**
     * @param args
     */
//  @Test
  public void testExportFile() throws Exception {
      File path = new File("/data/uark-import");
      File excelFile = new File(path, "Ark_HM_1299/Ark_HM_1299.xlsx");
      assertTrue(excelFile.exists());
      AdsExcelToXmlImportUtility utility = new AdsExcelToXmlImportUtility();
      utility.extractExcelToXml(excelFile);
  }
  
//  @Test
  public void processAllFiles() throws Exception {
      File rootDir = new File("/data/uark-import");
      AdsExcelToXmlImportUtility utility = new AdsExcelToXmlImportUtility();
      utility.extractExcelFiles(rootDir);
      
  }

  public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
