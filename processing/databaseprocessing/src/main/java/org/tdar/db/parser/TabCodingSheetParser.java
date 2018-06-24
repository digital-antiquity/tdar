package org.tdar.db.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;

/**
 * $Id$
 * <p>
 * Parses CSV coding sheets using <a href='http://opencsv.sourceforge.net'>OpenCSV</a>.
 * 
 * Should we switch to <a href='http://supercsv.sourceforge.net'>Super CSV</a> instead.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class TabCodingSheetParser extends CsvCodingSheetParser {

    @Override
    public CSVReader getReader(InputStream stream) {
        return new CSVReader(new BufferedReader(new InputStreamReader(stream), '\t'));
    }

}