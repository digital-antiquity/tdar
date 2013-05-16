package org.tdar.core.service.resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.dao.resource.CodingSheetDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.parser.CodingSheetParser;
import org.tdar.core.parser.CodingSheetParserException;
import org.tdar.core.service.workflow.workflows.GenericColumnarDataWorkflow;
import org.tdar.filestore.WorkflowContext;
import org.tdar.utils.ExceptionWrapper;

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

    public static final String COULD_NOT_PARSE_FILE = "Couldn't parse %s.  We can only process CSV or Excel files (make sure the file extension is .csv or .xls)";
    private static final String ERROR_PARSE_UNKNOWN = "The system was unable to parse your coding sheet. Please review your submission try again.";
    public static final String ERROR_PARSE_DUPLICATE_CODES = "Codes in your coding sheet must be unique.  We detected the following duplicate codes: ";

    public List<CodingSheet> findSparseCodingSheetList() {
        return getDao().findSparseResourceBySubmitterType(null, ResourceType.CODING_SHEET);
    }

    public void ingestCodingSheet(CodingSheet codingSheet, WorkflowContext ctx) {
        // 1. save metadata for coding sheet file
        // 1.1 Create CodingSheet object, and save the metadata
        Collection<InformationResourceFileVersion> files = codingSheet.getLatestVersions(VersionType.UPLOADED);
        getLogger().debug("processing uploaded coding sheet files: {}", files);

        if (files.size() != 1) {
            getLogger().warn("Unexpected number of files associated with this coding sheet, expected 1 got " + files.size());
            return;
        }

        /*
         * two cases, either:
         * 1) 1 file uploaded (csv | tab | xls)
         * 2) tab entry into form (2 files uploaded 1 archival, 2 not)
         */

        InformationResourceFileVersion toProcess = files.iterator().next();
        if (files.size() > 1) {
            for (InformationResourceFileVersion file : files) {
                if (file.isArchival())
                    toProcess = file;
            }
        }
        // should always be 1 based on the check above
        getLogger().debug("adding coding rules");
        try {
            parseUpload(codingSheet, toProcess);
            saveOrUpdate(codingSheet);
        } catch (Throwable e) {
            ctx.getExceptions().add(new ExceptionWrapper(e.getMessage(), ExceptionUtils.getFullStackTrace(e)));
            ctx.setErrorFatal(true);
            ctx.setProcessedSuccessfully(false);
            saveOrUpdate(toProcess.getInformationResourceFile());
        }

    }

    @Transactional
    protected void parseUpload(CodingSheet codingSheet, InformationResourceFileVersion version)
            throws IOException, CodingSheetParserException {
        // FIXME: ensure that this is all in one transaction boundary so if an exception occurs
        // this delete will get rolled back. Also, parse cannot swallow exceptions if the
        // new coding rules are not inputed.
        // ArrayList<CodingRule> existingCodingRules = new ArrayList<CodingRule>(codingSheet.getCodingRules());
        // codingSheet.getCodingRules().clear();
        FileInputStream stream = null;
        Set<String> duplicates = new HashSet<String>();
        List<CodingRule> incomingCodingRules = new ArrayList<CodingRule>();
        try {
            stream = new FileInputStream(version.getTransientFile());
            incomingCodingRules.addAll(getCodingSheetParser(version.getFilename()).parse(codingSheet, stream));
            Set<String> uniqueSet = new HashSet<String>();
            for (CodingRule rule : incomingCodingRules) {
                boolean unique = uniqueSet.add(rule.getCode());
                if (!unique) {
                    duplicates.add(rule.getCode());
                }
            }
        } catch (Exception e) {
            throw new TdarRecoverableRuntimeException(ERROR_PARSE_UNKNOWN);
        } finally {
            if (stream != null)
                IOUtils.closeQuietly(stream);
        }

        if (CollectionUtils.isNotEmpty(duplicates)) {
            throw new CodingSheetParserException(ERROR_PARSE_DUPLICATE_CODES, duplicates);
        }

        Map<String, CodingRule> codeToRuleMap = codingSheet.getCodeToRuleMap();
        for (CodingRule rule : incomingCodingRules) {
            CodingRule existingRule = codeToRuleMap.get(rule.getCode());
            if (existingRule != null) {
                rule.setOntologyNode(existingRule.getOntologyNode());
            }
        }
        getDao().delete(codingSheet.getCodingRules());
        codingSheet.getCodingRules().addAll(incomingCodingRules);
        getDao().saveOrUpdate(codingSheet);
    }

    private CodingSheetParser getCodingSheetParser(String filename) throws CodingSheetParserException {
        CodingSheetParser parser = CodingSheetParserFactory.getInstance().getParser(filename);
        if (parser == null) {
            throw new CodingSheetParserException(String.format(COULD_NOT_PARSE_FILE, filename));
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

    @Transactional
    public void reconcileOntologyReferencesOnRulesAndDataTableColumns(CodingSheet codingSheet, Ontology ontology) {
        if (ontology == null && codingSheet.getDefaultOntology() == null)
            return;

        if (ObjectUtils.notEqual(ontology, codingSheet.getDefaultOntology())) {
            if (Persistable.Base.isNullOrTransient(ontology)) {
                // clamp the ontology to null
                ontology = null;
            }
            else {
                for (CodingRule rule : codingSheet.getCodingRules()) {
                    OntologyNode existingNode = rule.getOntologyNode();
                    OntologyNode node = null;
                    // try and match on the IRI to start with
                    if (existingNode != null) {
                        node = ontology.getNodeByIri(existingNode.getIri());
                        if (node == null) {
                            // if not try and match on the term
                            node = ontology.getNodeByName(rule.getTerm());
                            if (node == null) {
                                // and if that was null, try the existing nodes display name
                                node = ontology.getNodeByName(existingNode.getDisplayName());
                            }
                        }
                    }
                    rule.setOntologyNode(node);
                }
                getDao().saveOrUpdate(codingSheet.getCodingRules());
            }
            // find all DataTableColumns referencing this CodingSheet and change its ontology
            codingSheet.setDefaultOntology(ontology);
            // If we've not hit "save" yet, there's no point in actually calling this, also this stops transient refernece
            // exceptions that were dying
            if (Persistable.Base.isNotTransient(codingSheet)) {
                getDao().updateDataTableColumnOntologies(codingSheet, ontology);
            }
        }
    }

}
