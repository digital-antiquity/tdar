package org.tdar.core.service.resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.resource.CodingSheetDao;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.db.parser.CodingSheetParser;
import org.tdar.db.parser.CodingSheetParserException;
import org.tdar.db.parser.TCodingRule;
import org.tdar.exception.ExceptionWrapper;
import org.tdar.fileprocessing.workflows.GenericColumnarDataWorkflow;
import org.tdar.fileprocessing.workflows.WorkflowContext;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.VersionType;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

/**
 * Provides coding sheet upload, parsing/import, and persistence functionality.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 * @latest $Id$
 */
@Service
@Transactional
public class CodingSheetServiceImpl extends ServiceInterface.TypedDaoBase<CodingSheet, CodingSheetDao> implements CodingSheetService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    SerializationService serializationService;

    public List<CodingSheet> findSparseCodingSheetList() {
        return getDao().findSparseResourceBySubmitterType(null, ResourceType.CODING_SHEET);
    }

    /*
     * Once the @link WorkflowService has processed and parsed the coding sheet, it must be ingested into the database.
     */
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
                if (file.isArchival()) {
                    toProcess = file;
                }
            }
        }
        // should always be 1 based on the check above
        getLogger().debug("adding coding rules");
        try {
            parseUpload(codingSheet, toProcess);
            saveOrUpdate(codingSheet);
        } catch (Throwable e) {
            ExceptionWrapper e2 = new ExceptionWrapper(e.getMessage(), e);
            e2.setFatal(true);
            ctx.getExceptions().add(e2);
            ctx.setErrorFatal(true);
            ctx.setProcessedSuccessfully(false);
            getDao().saveOrUpdate(toProcess.getInformationResourceFile());
        }

    }

    /*
     * Parses a coding sheet file and adds it to the coding sheet. Part of the Post-Workflow process, and @see ingestCodingSheet; exposed for testing
     */
    @Transactional
    public void parseUpload(CodingSheet codingSheet, InformationResourceFileVersion version) throws IOException, CodingSheetParserException {
        // FIXME: ensure that this is all in one transaction boundary so if an exception occurs
        // this delete will get rolled back. Also, parse cannot swallow exceptions if the
        // new coding rules are not inputed.
        // ArrayList<CodingRule> existingCodingRules = new ArrayList<CodingRule>(codingSheet.getCodingRules());
        // codingSheet.getCodingRules().clear();
        FileInputStream stream = null;
        Set<String> duplicates = new HashSet<String>();
        List<TCodingRule> incomingCodingRules = new ArrayList<>();
        try {
            stream = new FileInputStream(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version));
            incomingCodingRules.addAll(getCodingSheetParser(version.getFilename()).parse(stream));
            Set<String> uniqueSet = new HashSet<String>();
            for (TCodingRule rule : incomingCodingRules) {
                boolean unique = uniqueSet.add(rule.getCode());
                if (!unique) {
                    duplicates.add(rule.getCode());
                }
            }
        } catch (Exception e) {
            throw new CodingSheetParserException(MessageHelper.getMessage("codingSheet.could_not_parse_unknown"));
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
        logger.debug("{} rules found, {} duplicates", incomingCodingRules.size(), duplicates.size());

        if (CollectionUtils.isNotEmpty(duplicates)) {
            throw new CodingSheetParserException("codingSheet.duplicate_keys", duplicates);
        }

        Map<String, CodingRule> codeToRuleMap = codingSheet.getCodeToRuleMap();
        deleteUnusedCodingRules(codingSheet, incomingCodingRules);
        addOrCopyNewOrExistingRules(codingSheet, incomingCodingRules, codeToRuleMap);
        getDao().saveOrUpdate(codingSheet);
    }

    private void addOrCopyNewOrExistingRules(CodingSheet codingSheet, List<TCodingRule> incomingCodingRules, Map<String, CodingRule> codeToRuleMap) {
        for (TCodingRule rule : incomingCodingRules) {
            CodingRule existingRule = codeToRuleMap.get(rule.getCode());
            if (existingRule == null) {
                existingRule = new CodingRule();
                existingRule.setCode(rule.getCode());
                existingRule.setCodingSheet(codingSheet);
                codingSheet.getCodingRules().add(existingRule);
            }
            existingRule.setDescription(rule.getDescription());
            existingRule.setTerm(rule.getTerm());
        }
    }

    private void deleteUnusedCodingRules(CodingSheet codingSheet, List<TCodingRule> incomingCodingRules) {
        Iterator<CodingRule> iterator = codingSheet.getCodingRules().iterator();
        while (iterator.hasNext()) {
            CodingRule rule = iterator.next();
            boolean seen = false;
            for (TCodingRule trule : incomingCodingRules) {
                if (StringUtils.equals(trule.getCode(), rule.getCode())) {
                    seen = true;
                    break;
                }
            }
            if (seen == false) {
                iterator.remove();
            }
        }
    }

    /*
     * Factory wrapper to identify a coding sheet parser for a given coding sheet return it
     * 
     * @return CodingSheetParser parser
     * 
     * @throws CodingSheetParserException when a parser cannot be found
     */
    private CodingSheetParser getCodingSheetParser(String filename) throws CodingSheetParserException {
        CodingSheetParser parser = CodingSheetParserFactory.getInstance().getParser(filename);
        if (parser == null) {
            throw new CodingSheetParserException("codingSheet.could_not_parse", Arrays.asList(filename));
        }
        return parser;
    }

    /*
     * Singleton CodingSheetFactory to return the appropriate parser for a @link CodingSheet
     */
    public static class CodingSheetParserFactory {
        public final static CodingSheetParserFactory INSTANCE = new CodingSheetParserFactory();

        public static CodingSheetParserFactory getInstance() {
            return INSTANCE;
        }

        public CodingSheetParser getParser(String filename) {
            if ((filename == null) || filename.isEmpty()) {
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

    /*
     * Each @link CodingRule on the @link CodingSheet can be mapped to an @link OntologyNode ; when saving, we may need to reconcile
     * these nodes and relationships when re-parsing or modifying a @link CodingSheet because the CodingRules may have changed
     */
    @Transactional
    public void reconcileOntologyReferencesOnRulesAndDataTableColumns(CodingSheet codingSheet, Ontology ontology_) {
        Ontology ontology = ontology_;
        if ((ontology == null) && (codingSheet.getDefaultOntology() == null)) {
            return;
        }

        if (ObjectUtils.notEqual(ontology, codingSheet.getDefaultOntology())) {
            if (PersistableUtils.isNullOrTransient(ontology)) {
                // clamp the ontology to null
                ontology = null;
            } else {
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
        }
    }

    /*
     * @return List<CodingSheet> associated with the ontology
     */
    @Transactional(readOnly = true)
    public List<CodingSheet> findAllUsingOntology(Ontology ontology) {
        return getDao().findAllUsingOntology(ontology, Arrays.asList(Status.ACTIVE));
    }

    @Transactional(readOnly = false)
    public List<String> updateCodingSheetMappings(CodingSheet codingSheet, TdarUser authenticatedUser, List<CodingRule> incomingRules) {
        List<String> mappingIssues = new ArrayList<>();
        getLogger().debug("saving coding rule -> ontology node mappings for {} - this will generate a new default coding sheet!", codingSheet);
        for (CodingRule transientRule : incomingRules) {
            OntologyNode ontologyNode = transientRule.getOntologyNode();
            getLogger().debug(" matching column values: {} -> node ids {}", transientRule, ontologyNode);

            if (ontologyNode != null && StringUtils.isNotBlank(ontologyNode.getDisplayName()) && ontologyNode.getId() == null) {
                getLogger().warn("mapping>> ontology node label has text {}, but id is null for rule: {}", ontologyNode, transientRule);
                mappingIssues.add(transientRule.getTerm());
            }

            CodingRule rule = codingSheet.getCodingRuleById(transientRule.getId());
            Ontology ontology = codingSheet.getDefaultOntology();

            if (ontologyNode != null) {
                if (rule == null && PersistableUtils.isNullOrTransient(transientRule.getId())) {
                    String code = transientRule.getCode();
                    logger.debug("{} -- {} ", code, transientRule);
                    if (StringUtils.equals(code, CodingRule.MISSING.getCode())
                            || StringUtils.equals(code, CodingRule.UNMAPPED.getCode())
                            || StringUtils.equals(code, CodingRule.NULL.getCode())) {
                        codingSheet.getCodingRules().add(transientRule);
                        transientRule.setCodingSheet(codingSheet);
                        getDao().saveOrUpdate(transientRule);
                        rule = transientRule;
                    }
                }
                rule.setOntologyNode(ontology.getOntologyNodeById(ontologyNode.getId()));
            }
        }
        codingSheet.markUpdated(authenticatedUser);
        saveOrUpdate(codingSheet);
        ResourceRevisionLog rrl = new ResourceRevisionLog("updated coding sheet mapings", codingSheet, authenticatedUser, RevisionLogType.EDIT);
        try {
            rrl.setPayload(serializationService.convertToXML(codingSheet));
        } catch (Exception e) {
            logger.error("issue serializing to XML", e);
        }
        getDao().saveOrUpdate(rrl);
        codingSheet.getResourceRevisionLog().add(rrl);
        getDao().saveOrUpdate(codingSheet.getCodingRules());
        return mappingIssues;
    }

    @Transactional(readOnly = true)
    public boolean isOkToMapOntology(CodingSheet persistable) {
        Ontology defaultOntology = persistable.getDefaultOntology();
        if (PersistableUtils.isNullOrTransient(defaultOntology) || CollectionUtils.isNotEmpty(defaultOntology.getFilesWithProcessingErrors())) {
            getLogger().debug("cannot map, ontology issues, null or transient");
            return false;
        }
        if (CollectionUtils.isEmpty(persistable.getCodingRules()) || CollectionUtils.isNotEmpty(persistable.getFilesWithProcessingErrors())) {
            getLogger().debug("cannot map, coding sheet has errors or no rules");
            return false;
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * We have a few special rules:
     * NULL , MISSING, and UNMAPPED
     * 
     * for these rules, we want to group them separately for the user
     */
    public List<CodingRule> addSpecialCodingRules(CodingSheet codingSheet, List<CodingRule> codingRules) {
        List<CodingRule> special = new ArrayList<>();
        if (!TdarConfiguration.getInstance().includeSpecialCodingRules()) {
            return special;
        }
        Map<String, CodingRule> codeToRuleMap = codingSheet.getCodeToRuleMap();
        CodingRule _null = codeToRuleMap.get(CodingRule.NULL.getCode());
        if (_null != null) {
            codingRules.remove(_null);
            special.add(_null);
        } else {
            special.add(CodingRule.NULL);
        }

        CodingRule _missing = codeToRuleMap.get(CodingRule.MISSING.getCode());
        if (_missing != null) {
            codingRules.remove(_missing);
            special.add(_missing);
        } else {
            special.add(CodingRule.MISSING);
        }

        CodingRule _unmapped = codeToRuleMap.get(CodingRule.UNMAPPED.getCode());
        if (_unmapped != null) {
            codingRules.remove(_unmapped);
            special.add(_unmapped);

        } else {
            special.add(CodingRule.UNMAPPED);
        }
        return special;
    }

}
