package org.tdar.core.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;

/**
 * $Id$
 * 
 * Parses Excel coding sheets.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class ExcelCodingSheetParser implements CodingSheetParser {

    private final static Logger logger = LoggerFactory.getLogger(ExcelCodingSheetParser.class);
    private DataFormatter excelDataFormatter = new HSSFDataFormatter();

    @Override
    public List<CodingRule> parse(CodingSheet codingSheet, InputStream stream) throws CodingSheetParserException {
        List<CodingRule> codingRules = new ArrayList<CodingRule>();
        try {
            Workbook workbook = WorkbookFactory.create(stream);
            // XXX: assumes coding sheet is in the first sheet only.
            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (Row row : sheet) {
                if (row == null) {
                    logger.warn("null row.");
                    continue;
                }
                Cell codeCell = row.getCell(CODE_INDEX);
                Cell termCell = row.getCell(TERM_INDEX);
                Cell descriptionCell = row.getCell(DESCRIPTION_INDEX);
                if ((codeCell == null) || (termCell == null)) {
                    logger.warn("null code/term cell: " + codeCell + termCell + " - skipping");
                    continue;
                }

                CodingRule codingRule = new CodingRule();
                String code = excelDataFormatter.formatCellValue(codeCell, evaluator);
                String term = excelDataFormatter.formatCellValue(termCell, evaluator);
                if (StringUtils.isBlank(code) || StringUtils.isBlank(term)) {
                    logger.warn(String.format("Empty code (%s) or term (%s) - skipping", code, term));
                    continue;
                }
                codingRule.setCode(code);
                codingRule.setTerm(term);
                codingRule.setCodingSheet(codingSheet);
                if (descriptionCell != null) {
                    codingRule.setDescription(excelDataFormatter.formatCellValue(descriptionCell, evaluator));
                }
                codingRules.add(codingRule);
            }
        } catch (IllegalStateException e) {
            logger.error("Couldn't parse excel file", e);
            throw new CodingSheetParserException("excelCodingSheetParser.could_not_parse_missing_fields", e);
        } catch (IOException e) {
            logger.error("Couldn't construct POI Workbook from input stream", e);
            throw new CodingSheetParserException("excelCodingSheetParser.could_not_parse_poi", e);
        }
        catch (Throwable exception) {
            logger.error("Couldn't create POI Workbook from input stream", exception);
            throw new CodingSheetParserException("excelCodingSheetParser.could_not_parse_poi", exception);
        }
        return codingRules;
    }

}
