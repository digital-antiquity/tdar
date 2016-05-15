package org.tdar.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TdarSheetsContentHandler implements SheetContentsHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int toRead = -1;
//    private int rowNum = 0;
    private List<List<String>> rows = new ArrayList<>();
    private List<String> row = new ArrayList<>();

    public TdarSheetsContentHandler(int countToRead) {
        toRead = countToRead;
    }

    int count = 0;

    @Override
    public void startRow(int rowNum) {
        count++;
//        this.rowNum = rowNum;
        row = new ArrayList<>();
        addAt(rows, row, rowNum);
    }

    private <E> void addAt(List<E> rows2, E row2, int index) {
        while (rows2.size() < index) {
            rows2.add(null);
        }
        rows2.add(index, row2);
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment arg2) {
        int colNum = getColNum(cellReference);

        if (count <= toRead) {
            addAt(row, formattedValue, colNum);
        }
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {

    }

    public int getColNum(String _colName) {

        // remove any whitespace
        String colName = _colName.trim();
        colName = colName.replaceAll("[0-9]+", "");
        StringBuffer buff = new StringBuffer(colName);

        // string to lower case, reverse then place in char array
        char chars[] = buff.reverse().toString().toLowerCase().toCharArray();

        int retVal = 0, multiplier = 0;

        for (int i = 0; i < chars.length; i++) {
            // retrieve ascii value of character, subtract 96 so number corresponds to place in alphabet. ascii 'a' = 97
            multiplier = chars[i] - 96;
            // mult the number by 26^(position in array)
            retVal += multiplier * Math.pow(26, i);
        }
        return retVal;
    }

    @Override
    public void endRow(int rowNum) {
        logger.info("[{},{}] --> {}", rowNum, row);
    }
}
