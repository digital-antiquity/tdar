package org.tdar.core.service.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.dao.resource.CodingSheetDao;
import org.tdar.core.parser.CodingSheetParser;
import org.tdar.core.parser.CodingSheetParserException;
import org.tdar.core.parser.CsvCodingSheetParser;
import org.tdar.core.parser.ExcelCodingSheetParser;

/**
 * Provides coding sheet upload, parsing/import, and persistence functionality.
 * 
 * FIXME: should translation of table columns exist here? Move to standalone DatasetTranslationService?
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 * @latest $Id$
 */
@Service
@Transactional
public class CodingSheetService extends AbstractInformationResourceService<CodingSheet, CodingSheetDao> {

    @Autowired
    public void setDao(CodingSheetDao dao) {
        super.setDao(dao);
    }

    public CodingSheet findByFilename(final String filename) {
        return getDao().findByFilename(filename);
    }

    public List<CodingSheet> findSparseCodingSheetList() {
        return getDao().findSparseResourceBySubmitterType(null, ResourceType.CODING_SHEET);
    }

    public void parseUpload(CodingSheet codingSheet, String filename, InputStream inputStream)
            throws IOException, CodingSheetParserException {
        // FIXME: ensure that this is all in one transaction boundary so if an exception occurs
        // this delete will get rolled back. Also, parse cannot swallow exceptions if the
        // new coding rules are not inputed.
        // ArrayList<CodingRule> existingCodingRules = new ArrayList<CodingRule>(codingSheet.getCodingRules());
        // codingSheet.getCodingRules().clear();
        List<CodingRule> incomingCodingRules = getCodingSheetParser(filename).parse(codingSheet, inputStream);
        // if (getDao().getNumberOfMappedDataTableColumns(codingSheet) > 0) {
        // // implement the same logic as ontologyService to synchronize coding sheets, match on key only
        // logger.info("remapping coding sheet values");
        //
        // for (int i = 0; i < incomingCodingRules.size(); i++) {
        // CodingRule incomingRule = incomingCodingRules.get(i);
        // incomingRule.setCodingSheet(codingSheet);
        // for (int j = 0; j < existingCodingRules.size(); j++) {
        // CodingRule existingRule = existingCodingRules.get(j);
        // if (existingRule != null && existingRule.getCode().equals(incomingRule.getCode())) {
        // long oldId = -1;
        // if (incomingRule.getId() != null)
        // oldId = incomingRule.getId();
        // incomingRule.setId(existingRule.getId());
        // getDao().detachFromSession(existingRule);
        // incomingRule = getDao().merge(incomingRule);
        // incomingCodingRules.set(i, incomingRule);
        // existingCodingRules.set(j, null);
        // logger.trace("incoming:" + incomingRule.getId() + " --> " + incomingRule.getTerm() + " was: " + oldId);
        // logger.trace("existing:" + existingRule.getId() + " --> " + existingRule.getTerm());
        // break;
        // }
        // // getDao().delete(existingRule);
        // }
        // }
        // existingCodingRules.removeAll(Collections.singleton(null));
        // delete(codingSheet.getCodingRules());
        // }
        // else {
        // for (CodingRule rule : incomingCodingRules) {
        // rule.setCodingSheet(codingSheet);
        // }
        // }
        getDao().delete(codingSheet.getCodingRules());
        codingSheet.getCodingRules().addAll(incomingCodingRules);
        getDao().saveOrUpdate(codingSheet);
        // delete(toDelete);
    }

    private CodingSheetParser getCodingSheetParser(String filename) {
        CodingSheetParser parser = CodingSheetParserFactory.getInstance().getParser(filename);
        if (parser == null) {
            throw new CodingSheetParserException("Couldn't parse " + filename
                    + ".  We can only process CSV or Excel files (make sure the file extension is .csv or .xls)");
        }
        return parser;
    }

    public static class CodingSheetParserFactory {
        private final static Map<String, CodingSheetParser> PARSERS = new HashMap<String, CodingSheetParser>();
        public final static CodingSheetParserFactory INSTANCE = new CodingSheetParserFactory();

        private CodingSheetParserFactory() {
            add(new ExcelCodingSheetParser());
            add(new CsvCodingSheetParser());
        }

        public static CodingSheetParserFactory getInstance() {
            return INSTANCE;
        }

        public CodingSheetParser getParser(String filename) {
            if (filename == null || filename.isEmpty()) {
                return null;
            }
            return PARSERS.get(FilenameUtils.getExtension(filename.toLowerCase()));
        }

        private void add(CodingSheetParser parser) {
            for (String supportedFileExtension : parser.getSupportedFileExtensions()) {
                PARSERS.put(supportedFileExtension, parser);
            }
        }
    }

}
