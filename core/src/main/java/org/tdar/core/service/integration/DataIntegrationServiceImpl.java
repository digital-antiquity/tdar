package org.tdar.core.service.integration;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.integration.IntegrationColumnPartProxy;
import org.tdar.core.dao.integration.TableDetailsProxy;
import org.tdar.core.dao.resource.DataTableColumnDao;
import org.tdar.core.dao.resource.DataTableDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.OntologyNodeDao;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.RightsResolver;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UserRightsProxyService;
import org.tdar.core.service.collection.CollectionRightsComparator;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;
import org.tdar.core.service.resource.FileProxyWrapper;
import org.tdar.db.model.IntegrationDatabase;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.VersionType;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * $Id$
 * 
 * Provides data integration functionality for datasets that have been translated and mapped to ontologies.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Service
public class DataIntegrationServiceImpl implements DataIntegrationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserRightsProxyService proxyService;

    @Autowired
    private IntegrationDatabase tdarDataImportDatabase;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private DataTableColumnDao dataTableColumnDao;

    @Autowired
    private DataTableDao dataTableDao;

    @Autowired
    private PersonalFilestoreService filestoreService;

    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private FileAnalyzer analyzer;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private OntologyNodeDao ontologyNodeDao;

    @Autowired
    private SerializationService serializationService;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#setTdarDataImportDatabase(org.tdar.db.model.abstracts.IntegrationDatabase)
     */
    @Override
    public void setTdarDataImportDatabase(IntegrationDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#updateMappedCodingRules(org.tdar.core.bean.resource.datatable.DataTableColumn)
     */
    @Override
    public void updateMappedCodingRules(DataTableColumn column) {
        if (column == null) {
            return;
        }
        logger.debug("updating mapping rules on {}", column);
        CodingSheet codingSheet = column.getDefaultCodingSheet();
        if ((codingSheet == null) || CollectionUtils.isEmpty(codingSheet.getCodingRules())) {
            logger.debug("aborting, no coding rules or coding sheet {}", codingSheet);
            return;
        }
        logger.trace("selecting distinct values from column");

        Set<String> values = new HashSet<>(tdarDataImportDatabase.selectDistinctValues(column.getDataTable(), column, false));
        logger.trace("values: {} ", values);
        logger.trace("matching coding rule terms to column values");

        if (TdarConfiguration.getInstance().useMapInNodeParticipation()) {
            // we make an assumption in places that the "term" in a coding rule is unique, but this might not
            // be the case, hence, we need to group them
            Map<String, List<CodingRule>> ruleMap = new HashMap<>();
            for (CodingRule rule : codingSheet.getCodingRules()) {
                ruleMap.putIfAbsent(rule.getTerm(), new ArrayList<>());
                ruleMap.get(rule.getTerm()).add(rule);
            }

            for (String val : values) {
                List<CodingRule> list = ruleMap.get(val);
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                for (CodingRule rule : list) {
                    logger.trace("mapping rule {} to column {}", rule, column);
                    rule.setMappedToData(column);
                }
            }
        } else {
            logger.trace("values: {} ", values);
            logger.trace("matching coding rule terms to column values");
            for (CodingRule rule : codingSheet.getCodingRules()) {
                if (values.contains(rule.getTerm())) {
                    logger.trace("mapping rule {} to column {}", rule, column);
                    rule.setMappedToData(column);
                }
            }

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#serializeIntegrationContext(java.util.List, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public String serializeIntegrationContext(List<IntegrationColumn> integrationColumns, TdarUser creator) throws Exception {
        StringWriter sw = new StringWriter();
        serializationService.convertToXML(new IntegrationContext(creator, integrationColumns), sw);
        return sw.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#findMappedCodingRules(org.tdar.core.bean.resource.datatable.DataTableColumn)
     */
    @Override
    @Transactional
    public List<CodingRule> findMappedCodingRules(DataTableColumn column) {
        List<String> distinctColumnValues = tdarDataImportDatabase.selectNonNullDistinctValues(column.getDataTable(), column, false);
        return dataTableColumnDao.findMappedCodingRules(column, distinctColumnValues);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#createGeneratedCodingSheet(com.opensymphony.xwork2.TextProvider,
     * org.tdar.core.bean.resource.datatable.DataTableColumn, org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Ontology)
     */
    @Override
    @Transactional
    public CodingSheet createGeneratedCodingSheet(TextProvider provider, Dataset dataset, DataTableColumn column, TdarUser submitter, Ontology ontology) {
        if (column == null) {
            logger.debug("{} tried to create an identity coding sheet for {} with no values", submitter, column);
        }

        CodingSheet codingSheet = dataTableColumnDao.setupGeneratedCodingSheet(column, dataset, submitter, provider, ontology);
        // generate identity coding rules
        List<String> dataColumnValues = tdarDataImportDatabase.selectNonNullDistinctValues(column.getDataTable(), column, true);
        Set<CodingRule> rules = new HashSet<>();
        for (int index = 0; index < dataColumnValues.size(); index++) {
            String dataValue = dataColumnValues.get(index);
            CodingRule rule = new CodingRule(codingSheet, dataValue);
            rules.add(rule);
        }
        genericDao.save(rules);
        try {
            String baseFileName = codingSheet.getTitle().replace(" ", "_");
            String csvText = convertCodingSheetToCSV(codingSheet, rules);
            FileProxy fileProxy = new FileProxy(baseFileName + ".csv", FileProxy.createTempFileFromString(csvText), VersionType.UPLOADED);
            fileProxy.addVersion(new FileProxy(baseFileName + ".txt", FileProxy.createTempFileFromString(csvText), VersionType.UPLOADED_TEXT));
            // prepare the metadata
            FileProxyWrapper wrapper = new FileProxyWrapper(codingSheet, analyzer, datasetDao, Arrays.asList(fileProxy));
            wrapper.processMetadataForFileProxies();
        } catch (Exception e) {
            logger.debug("could not process coding sheet", e);
        }

        return codingSheet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#convertCodingSheetToCSV(org.tdar.core.bean.resource.CodingSheet)
     */
    @Override
    public String convertCodingSheetToCSV(CodingSheet sheet) {
        return convertCodingSheetToCSV(sheet, sheet.getCodingRules());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#convertCodingSheetToCSV(org.tdar.core.bean.resource.CodingSheet, java.util.Collection)
     */
    @Override
    public String convertCodingSheetToCSV(CodingSheet sheet, Collection<CodingRule> rules) {
        // not all coding sheets have their rules directly attached at the moment (eg the generated ones)
        StringWriter sw = new StringWriter();
        CSVWriter writer = new CSVWriter(sw);
        for (CodingRule rule : rules) {
            writer.writeNext(new String[] { rule.getCode(), rule.getTerm(), rule.getDescription() });
        }
        IOUtils.closeQuietly(writer);
        return sw.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#getIntegrationSuggestions(java.util.Collection, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Ontology, List<DataTable>> getIntegrationSuggestions(Collection<DataTable> bookmarkedDataTables, boolean showOnlyShared) {
        HashMap<Ontology, List<DataTable>> allOntologies = new HashMap<>();
        if (CollectionUtils.isEmpty(bookmarkedDataTables)) {
            return Collections.emptyMap();
        }
        for (DataTable table : bookmarkedDataTables) {
            for (DataTableColumn column : table.getDataTableColumns()) {
                Ontology ontology = column.getMappedOntology();
                if (ontology != null) {
                    List<DataTable> values = allOntologies.get(ontology);
                    if (values == null) {
                        values = new ArrayList<>();
                    }
                    values.add(table);
                    allOntologies.put(ontology, values);
                }
            }
        }
        if (showOnlyShared) {
            HashMap<Ontology, List<DataTable>> toReturn = new HashMap<>();
            for (Entry<Ontology, List<DataTable>> entry : allOntologies.entrySet()) {
                if (entry.getValue().size() == bookmarkedDataTables.size()) {
                    toReturn.put(entry.getKey(), entry.getValue());
                }
            }
            return toReturn;
        }

        return allOntologies;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#getFilteredOntologyNodes(org.tdar.core.service.integration.IntegrationColumn)
     */
    @Override
    @Transactional(readOnly = false)
    public Set<OntologyNode> getFilteredOntologyNodes(IntegrationColumn integrationColumn) {
        List<IntegrationColumnPartProxy> list = getNodeParticipationByColumn(PersistableUtils.extractIds(integrationColumn.getColumns()));
        Set<OntologyNode> nodes = new HashSet<>();
        for (IntegrationColumnPartProxy icp : list) {
            nodes.addAll(icp.getFlattenedNodes());
        }
        return nodes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#getNodeParticipationByColumn(java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public List<IntegrationColumnPartProxy> getNodeParticipationByColumn(List<Long> dataTableColumnIds) {
        List<IntegrationColumnPartProxy> results = new ArrayList<>();
        Set<DataTableColumn> dataTableColumns = new HashSet<>(genericDao.findAll(DataTableColumn.class, dataTableColumnIds));
        Map<Ontology, Set<DataTableColumn>> columnsByOntology = buildOntologyToColumnMap(dataTableColumns);

        // For each ontology, update tdar-data database mappings, and get the list of correlated & mapped ontology nodes
        for (Ontology ontology : columnsByOntology.keySet()) {
            logger.trace("ontology: {}", ontology);
            for (DataTableColumn col : columnsByOntology.get(ontology)) {
                if (!col.isActuallyMapped()) {
                    continue;
                }
                IntegrationColumnPartProxy icp = new IntegrationColumnPartProxy();
                icp.setDataTableColumn(col);
                icp.setSharedOntology(ontology);
                results.add(icp);
                // get the actual mappings from tdardata
                updateMappedCodingRules(col);
                applyLegacyFilter(ontology, col, icp);

            }
            logger.trace("nodesByColumn: {}", results);
        }
        return results;
    }

    /**
     * Originally ported from IntegrationColumn.flatten(); this method takes the integration column and gets the associated coding sheet
     * and thus, ontology and ontology nodes to find what should be marked as "checked"
     * 
     * @param ontology
     * @param col
     * @param icp
     */
    private void applyLegacyFilter(Ontology ontology, DataTableColumn col, IntegrationColumnPartProxy icp) {
        SortedMap<Integer, List<OntologyNode>> ontologyNodeParentChildMap = ontology.toOntologyNodeMap();

        // check mapping first to see if the value should be translated a second
        // time to the common ontology format.

        // check if distinctValues has any values in common with mapped data values
        // if the two lists are disjoint (nothing in common), then there is no
        // data value if one of the distinct values is already equivalent to the ontology
        // node label, go with that.

        for (OntologyNode ontologyNode : ontology.getOntologyNodes()) {
            List<OntologyNode> children = ontologyNodeParentChildMap.get(Integer.valueOf(ontologyNode.getIntervalStart()));
            List<CodingRule> rules = col.getDefaultCodingSheet().findRulesMappedToOntologyNode(ontologyNode);

            // Step 1: find direct value matches
            if (CollectionUtils.isNotEmpty(rules)) {
                for (CodingRule rule : rules) {
                    if ((rule != null) && rule.isMappedToData(col)) {
                        markAdded(col, icp, ontologyNode);
                    }
                }
            }

            // NOTE: this can be removed once the legacy filter page is removed
            // check if any of the children of this node matches
            if (CollectionUtils.isNotEmpty(children)) {
                ontologyNode.setParent(true);
            }
        }
    }

    /**
     * convenience method setting mapped to true for an ontologyNode, and thena dding it to the flattened nodes list
     * 
     * @param col
     * @param icp
     * @param ontologyNode
     */
    private void markAdded(DataTableColumn col, IntegrationColumnPartProxy icp, OntologyNode ontologyNode) {
        ontologyNode.setMappedDataValues(true);
        ontologyNode.getColumnHasValueMap().put(col, true);
        icp.getFlattenedNodes().add(ontologyNode);
    }

    /**
     * Creates a reverse-map of Ontologies->DataTableColumns for mapped dataTableColumns
     * 
     * @param dataTableColumns
     * @return
     */
    private Map<Ontology, Set<DataTableColumn>> buildOntologyToColumnMap(Set<DataTableColumn> dataTableColumns) {
        Map<Ontology, Set<DataTableColumn>> columnsByOntology = new HashMap<>();

        // for each DataTableColumn, create an inverse-map of Ontologies->DataTableColumns
        for (DataTableColumn dataTableColumn : dataTableColumns) {
            Ontology mappedOntology = dataTableColumn.getMappedOntology();

            if (mappedOntology == null) {
                continue;
            }

            Set<DataTableColumn> columns = columnsByOntology.get(mappedOntology);
            if (columns == null) {
                columns = new HashSet<>();
                columnsByOntology.put(mappedOntology, columns);
            }
            columns.add(dataTableColumn);
        }
        return columnsByOntology;
    }

    private void validateIntegrationContext(IntegrationContext context) {
        List<String> bad = new ArrayList<>();
        for (IntegrationColumn col : context.getIntegrationColumns()) {
            if (col.isIntegrationColumn()) {
                for (DataTableColumn c : col.getColumns()) {
                    if (c == null || c.getDefaultCodingSheet() == null || c.getDefaultCodingSheet().getDefaultOntology() == null) {
                        bad.add(c.getDisplayName());
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(bad)) {
            throw new TdarRecoverableRuntimeException("dataIntegrationService.missing_mapped_columns", Arrays.asList(bad));
        }

        List<String> unauthorizedDatasets = new ArrayList<>();
        for (DataTable dt : context.getDataTables()) {
            Dataset dataset = dataTableDao.findDatasetForTable(dt);
            if (!authorizationService.canViewConfidentialInformation(context.getCreator(), dataset)) {
                unauthorizedDatasets.add(dataset.getTitle());
            }
        }

        if (CollectionUtils.isNotEmpty(unauthorizedDatasets)) {
            throw new TdarRecoverableRuntimeException("dataIntegrationService.cannot_integrate_permissions", Arrays.asList(StringUtils.join(
                    unauthorizedDatasets, ", ")));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#generateModernIntegrationResult(java.lang.String, com.opensymphony.xwork2.TextProvider,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional
    public ModernIntegrationDataResult generateModernIntegrationResult(String integration, TextProvider provider, TdarUser authenticatedUser) throws Exception {
        logger.trace("XXX: DISPLAYING FILTERED RESULTS :XXX");
        logger.debug("incoming JSON: {}", integration);
        IntegrationWorkflowData workflow = serializationService.readObjectFromJson(integration, IntegrationWorkflowData.class);

        if (CollectionUtils.isNotEmpty(workflow.getErrors())) {
            throw new TdarRecoverableRuntimeException("dataIntegrationService.invalid_integration", workflow.getErrors());
        }
        if (workflow.getFieldErrors() != null && workflow.getFieldErrors().size() != 0) {
            throw new TdarRecoverableRuntimeException("dataIntegrationService.invalid_integration", Arrays.asList(workflow.getFieldErrors()));
        }
        IntegrationContext integrationContext = workflow.toIntegrationContext(genericDao, provider);
        for (IntegrationColumn col : integrationContext.getIntegrationColumns()) {
            if (col.isIntegrationColumn()) {
                col.buildNodeChildHierarchy(ontologyNodeDao);
            }
        }

        // logger.debug(serializationService.convertToXML(integrationContext));

        integrationContext.setCreator(authenticatedUser);
        ResourceRevisionLog log = new ResourceRevisionLog("display filtered results (payload: tableToDisplayColumns)", null, authenticatedUser, null);
        log.setTimestamp(new Date());
        log.setPayload(integration);
        genericDao.saveOrUpdate(log);
        logger.trace(integration);
        // ADD ERROR CHECKING LOGIC

        validateIntegrationContext(integrationContext);

        for (IntegrationColumn col : integrationContext.getIntegrationColumns()) {
            if (col.isIntegrationColumn()) {
                if (CollectionUtils.isEmpty(col.getFilteredOntologyNodes())) {
                    col.getFilteredOntologyNodes().addAll(col.getSharedOntology().getOntologyNodes());
                }
            }
        }
        // // ok, at this point we have the integration columns that we're interested in + the ontology
        // // nodes that we want to use to filter values of interest and for aggregation.
        // // getLogger().debug("table columns are: " + tableToIntegrationColumns);
        // // getLogger().debug("ontology node hierarchy map: " + tableToOntologyHierarchyMap);
        // if (CollectionUtils.isEmpty(tableToIntegrationColumns)) {
        // addActionError("Either no integration columns or filter values were selected, please go back and select both");
        // return INPUT;
        // }
        //

        Set<Dataset> sets = new HashSet<>();
        for (DataTable table : integrationContext.getDataTables()) {
            logger.debug("{}", table);
            Dataset ds = dataTableDao.findDatasetForTable(table);
            logger.debug("{}", ds);
            if (ds != null) {
                sets.add(ds);
            }
        }
        integrationContext.setDatasets(new ArrayList<>(sets));
        
        ModernIntegrationDataResult result = tdarDataImportDatabase.generateIntegrationResult(integrationContext, integration, provider);
        storeResult(result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#storeResult(org.tdar.core.service.integration.ModernIntegrationDataResult)
     */
    @Override
    @Transactional
    public PersonalFilestoreTicket storeResult(ModernIntegrationDataResult result) {
        ModernDataIntegrationWorkbook workbook = result.getWorkbook();
        PersonalFilestoreTicket ticket = workbook.getTicket();
        genericDao.save(ticket);

        try {
            File resultFile = workbook.writeToTempFile();
            PersonalFilestore filestore = filestoreService.getPersonalFilestore(result.getIntegrationContext().getCreator());
            filestore.store(ticket, resultFile, workbook.getFileName());
            result.setTicket(ticket);
        } catch (Exception exception) {
            logger.error("an error occurred when producing the integration excel file", exception);
            throw new TdarRecoverableRuntimeException("dataIntegrationService.could_not_save_file");
        }

        return ticket;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.integration.DataIntegrationService#getTableDetails(java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public TableDetailsProxy getTableDetails(List<Long> dataTableIds) {
        TableDetailsProxy proxy = new TableDetailsProxy();
        proxy.getDataTables().addAll(genericDao.findAll(DataTable.class, dataTableIds));
        Map<Ontology, List<DataTable>> suggestions = getIntegrationSuggestions(proxy.getDataTables(), false);
        proxy.getMappedOntologies().addAll(suggestions.keySet());
        return proxy;
    }

    @Override
    @Transactional(readOnly = false)
    public void saveSettingsForController(DataIntegrationWorkflow persistable, TdarUser authenticatedUser, List<UserRightsProxy> proxies) {
        List<AuthorizedUser> authorizedUsers = new ArrayList<>();
        proxyService.convertProxyToItems(proxies, authenticatedUser, authorizedUsers, null);
        CollectionRightsComparator comparator = new CollectionRightsComparator(persistable.getAuthorizedUsers(), authorizedUsers);
        if (comparator.rightsDifferent()) {
            RightsResolver rco = authorizationService.getRightsResolverFor(persistable, authenticatedUser, InternalTdarRights.EDIT_ANYTHING);
            comparator.makeChanges(rco, persistable, authenticatedUser);
        }
        comparator = null;

        genericDao.saveOrUpdate(persistable);
    }
}
