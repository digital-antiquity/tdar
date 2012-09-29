package org.tdar.core.service.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.dao.resource.CodingSheetDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.parser.CodingSheetParser;
import org.tdar.core.parser.CodingSheetParserException;
import org.tdar.filestore.workflows.GenericColumnarDataWorkflow;

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
        Set<String> uniqueSet = new HashSet<String>();
        for (CodingRule rule : incomingCodingRules) {
            uniqueSet.add(rule.getCode());
        }
        logger.trace("incoming rules: {}", incomingCodingRules);
        logger.trace("unique set: {}", uniqueSet);
        if (incomingCodingRules.size() != uniqueSet.size()) {
            throw new TdarRecoverableRuntimeException(String.format("Code names must be unique, %s incoming %s unique names",
                    incomingCodingRules.size(), uniqueSet.size()));
        }

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
        public final static CodingSheetParserFactory INSTANCE = new CodingSheetParserFactory();

        public static CodingSheetParserFactory getInstance() {
            return INSTANCE;
        }

        public CodingSheetParser getParser(String filename) {
            if (filename == null || filename.isEmpty()) {
                return null;
            }
            GenericColumnarDataWorkflow workflow = new GenericColumnarDataWorkflow();
            String extension = FilenameUtils.getExtension(filename.toLowerCase());
            Class<? extends CodingSheetParser> parserClass = workflow.getCodingSheetParserForExtension(extension);
            try {
                return parserClass.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("No parser defined for format: " + extension, e);
            }
        }

    }

}
