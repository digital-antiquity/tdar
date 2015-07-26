package org.tdar.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.exception.TdarRuntimeException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XSSFReaderHelper {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ReadOnlySharedStringsTable strings;
    private StylesTable styles;
    private XSSFReader xssfReader;

    public void openFile(File excelFile) throws IOException {
        OPCPackage container;
        try {
            container = OPCPackage.open(excelFile.getAbsolutePath());
            strings = new ReadOnlySharedStringsTable(container);
            xssfReader = new XSSFReader(container);
            styles = xssfReader.getStylesTable();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (OpenXML4JException e) {
            e.printStackTrace();
        }
    }

    public String processSheet(String name, int num) throws IOException, SAXException, InvalidFormatException {
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        InputStream sheetInputStream = null;
        String currentSheetName = null;
        while (iter.hasNext()) {
            sheetInputStream = iter.next();
            currentSheetName = iter.getSheetName();
            logger.info(currentSheetName);
            if (StringUtils.equalsIgnoreCase(name, currentSheetName) || StringUtils.isBlank(name)) {
                break;
            } else {
                sheetInputStream = null;
            }
        }

        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxFactory.newSAXParser();
            XMLReader sheetParser = saxParser.getXMLReader();

            ContentHandler handler = new XSSFSheetXMLHandler(styles, strings, new TdarSheetsContentHandler(num), false// means result instead of formula
            );
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (NullPointerException npe) {
            logger.error("{}", npe);
        } catch (IndexOutOfBoundsException aob) {
            logger.error("{}", aob);
        } catch (ParserConfigurationException e) {
            throw new TdarRuntimeException("xssfReaderHelpher.sax_parser_broken", e);
        } finally {
            if (sheetInputStream != null) {
                IOUtils.closeQuietly(sheetInputStream);
            }
        }
        return currentSheetName;
    }

}
