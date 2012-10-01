package org.tdar.utils.sensorydata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.SensoryData;

import de.schlichtherle.io.FileOutputStream;

@Deprecated
public class AdsExcelToXmlImportUtility {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    AdsTemplateProcessor processor = new AdsTemplateProcessor();
//    SensoryDataExporter exporter = new SensoryDataExporter();
    
    public static void main(String[] args) {
        
    }
    
    
    public void extractExcelToXml(File excelFile, File outFile) throws FileNotFoundException {
        try {
            SensoryData sensoryData = processor.getSensoryDataFromAdsTemplate(excelFile);
            
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            PrintWriter pw = new PrintWriter(fileOutputStream);
//            exporter.export(sensoryData, pw);
            pw.flush();
            pw.close();
            
        } catch(AdsImportException aix) {
            logger.error("failed to extract {} due to error: {}", excelFile, aix.getMessage());
            return;
        }
    }
    
    public void extractExcelToXml(File excelFile) throws FileNotFoundException{
        String outFilename = FilenameUtils.removeExtension(excelFile.getName()) + ".xml";
        File outFile = new File(excelFile.getParent(), outFilename);
        extractExcelToXml(excelFile, outFile);
    }
    
    public void extractExcelToXml(String pathToExcel) throws FileNotFoundException {
        File excelFile = new File(pathToExcel);
        extractExcelToXml(excelFile);
    }
    
    public void extractExcelFiles(File rootDir) throws FileNotFoundException {
        Collection<File> excelFiles = FileUtils.listFiles(rootDir,  new String[] {"xlsx", "xls"}, true);
        for(File file: excelFiles) {
            extractExcelToXml(file);
        }
        
    }
    
}
