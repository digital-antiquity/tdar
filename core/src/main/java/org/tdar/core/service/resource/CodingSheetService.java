package org.tdar.core.service.resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.CodingSheetDao;
import org.tdar.core.parser.CodingSheetParser;
import org.tdar.core.parser.CodingSheetParserException;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.workflow.workflows.GenericColumnarDataWorkflow;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.WorkflowContext;
import org.tdar.utils.ExceptionWrapper;
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
public class CodingSheetService extends ServiceInterface.TypedDaoBase<CodingSheet, CodingSheetDao> {

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
    protected void parseUpload(CodingSheet codingSheet, InformationResourceFileVersion version) throws IOException, CodingSheetParserException {
        // FIXME: ensure that this is all in one transaction boundary so if an exception occurs
        // this delete will get rolled back. Also, parse cannot swallow exceptions if the
        // new coding rules are not inputed.
        // ArrayList<CodingRule> existingCodingRules = new ArrayList<CodingRule>(codingSheet.getCodingRules());
        // codingSheet.getCodingRules().clear();
        FileInputStream stream = null;
        Set<String> duplicates = new HashSet<String>();
        List<CodingRule> incomingCodingRules = new ArrayList<CodingRule>();
        try {
            stream = new FileInputStream(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version));
            incomingCodingRules.addAll(getCodingSheetParser(version.getFilename()).parse(codingSheet, stream));
            Set<String> uniqueSet = new HashSet<String>();
            for (CodingRule rule : incomingCodingRules) {
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
        ResourceRevisionLog rrl = new ResourceRevisionLog("updated coding sheet mapings", codingSheet, authenticatedUser);
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

    @Transactional(readOnly=true)
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
    
}
