package org.tdar.core.parser;

import java.io.InputStream;
import java.util.List;

import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;

/**
 * $Id$
 * 
 * Interface for parsing coding sheets from an InputStream.
 * Assumes each row of a coding sheet is in the format [code, term, description]
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface CodingSheetParser {

    final static int CODE_INDEX = 0;
    final static int TERM_INDEX = 1;
    final static int DESCRIPTION_INDEX = 2;

    List<CodingRule> parse(CodingSheet codingSheet, InputStream stream) throws CodingSheetParserException;

}