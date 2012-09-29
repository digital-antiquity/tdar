package org.tdar.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.DataTableColumnDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.excel.CellFormat;
import org.tdar.core.service.excel.SheetProxy;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.OntologyNodeService;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.personal.PersonalFileType;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationContext;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.utils.Pair;

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
public class DataIntegrationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PostgresDatabase tdarDataImportDatabase;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private DataTableColumnDao dataTableColumnDao;

    @Autowired
    private PersonalFilestoreService filestoreService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private InformationResourceService informationResourceService;

    @Autowired
    private OntologyNodeService ontologyNodeService;

    @Autowired
    private XmlService xmlService;

    public void setTdarDataImportDatabase(PostgresDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }

    @SuppressWarnings("unchecked")
    public IntegrationDataResult generateIntegrationResult(final DataTable table, final List<IntegrationColumn> integrationColumns,
            final Map<List<OntologyNode>, Map<DataTable, Integer>> pivot) {
        // formulate a SELECT statement
        String selectSql = tdarDataImportDatabase.generateOntologyEnhancedSelect(table, integrationColumns, pivot);

        IntegrationDataResult integrationDataResult = new IntegrationDataResult();
        integrationDataResult.setDataTable(table);
        integrationDataResult.setIntegrationColumns(integrationColumns);
        logger.debug(selectSql);

        List<String[]> rowDataList = new ArrayList<String[]>();
        // if we have a "WHERE clause, then we can actually do something (otherwise, we probably have an empty filter list
        if (selectSql.toLowerCase().contains(" where ")) {
            rowDataList = tdarDataImportDatabase.query(selectSql, new ParameterizedRowMapper<String[]>() {
                @Override
                public String[] mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                    // grab the data from this result set, populate the IntegrationDataResult

                    // FIXME: create temporary postgres table, populate it with integration results, update it as necessary,
                    // then stream it into the generated excel file

                    ArrayList<String> values = new ArrayList<String>();
                    values.add(table.getDataset().getTitle());
                    ArrayList<OntologyNode> ontologyNodes = new ArrayList<OntologyNode>();
                    int resultSetPosition = 1;
                    for (IntegrationColumn integrationColumn : integrationColumns) {
                        // note SQL iterator is 1 based; java iterator is 0 based
                        DataTableColumn column = table.getColumnByName(resultSet.getMetaData().getColumnName(resultSetPosition));
                        String value = "";
                        if (column != null) { // RAW VALUE
                            value = tdarDataImportDatabase.getResultSetValueAsString(resultSet, resultSetPosition, column);
                        }
                        if (column != null && !integrationColumn.isDisplayColumn() && StringUtils.isEmpty(value)) {
                            value = PostgresDatabase.NULL_EMPTY_INTEGRATION_VALUE;
                        }
                        values.add(value);
                        ontologyNodes.add(OntologyNode.NULL); // initialize the array so we have columns line up
                        if (column != null && !integrationColumn.isDisplayColumn()) { // MAPPED VALUE if not display column
                            String mappedVal = null;
                            // FIXME: get the appropriately aggregated OntologyNode for the given value, add a method in DataIntegrationService
                            // OntologyNode mappedOntologyNode = integrationColumn.getMappedOntologyNode(value, column);
                            OntologyNode mappedOntologyNode = getMappedOntologyNode(value, column, integrationColumn);
                            if (mappedOntologyNode != null) {
                                mappedVal = mappedOntologyNode.getDisplayName();
                                ontologyNodes.set(ontologyNodes.size() - 1, mappedOntologyNode);
                            }
                            if (mappedVal == null) {
                                mappedVal = PostgresDatabase.NULL_EMPTY_MAPPED_VALUE;
                            }
                            values.add(mappedVal);
                        }
                        resultSetPosition++;
                    }
                    if (pivot.get(ontologyNodes) == null) {
                        pivot.put(ontologyNodes, new HashMap<DataTable, Integer>());
                    }
                    Integer groupCount = pivot.get(ontologyNodes).get(table);
                    if (groupCount == null) {
                        pivot.get(ontologyNodes).put(table, 0);
                        groupCount = 0;
                    }
                    pivot.get(ontologyNodes).put(table, groupCount + 1);
                    return values.toArray(new String[0]);
                }
            });
        }
        integrationDataResult.setRowData(rowDataList);
        return integrationDataResult;
    }

    /*
     * sets transient boolean on CodingRule to mark that it's mapped
     */
    public void updateMappedCodingRules(DataTableColumn column) {
        CodingSheet codingSheet = column.getDefaultCodingSheet();
        logger.info("col {}", column);
        logger.info("sheet {}", codingSheet);
        logger.info("any rules {}", CollectionUtils.isEmpty(codingSheet.getCodingRules()));
        if (column == null || codingSheet == null || CollectionUtils.isEmpty(codingSheet.getCodingRules()))
            return;
        logger.info("select distinct values");
        List<String> values = tdarDataImportDatabase.selectDistinctValues(column);
        logger.info("iterating over coding rules");
        for (CodingRule rule : codingSheet.getCodingRules()) {
            logger.info("term: {}, values: {} ", rule.getTerm(), values);
            if (values.contains(rule.getTerm())) {
                rule.setMappedToData(column);
                logger.trace("\t{}", rule);
            }
        }
    }

    /**
     * Returns the appropriate OntologyNode mapped to the given String value for the given IntegrationColumn.
     * 
     * If the OntologyNode exists in the 
     * 
     * @param value
     * @param column
     * @param integrationColumn
     * @return
     */
    public OntologyNode getMappedOntologyNode(String value, DataTableColumn column, IntegrationColumn integrationColumn) {
        // FIXME: this lookup should be moved out of this method, otherwise we are creating / recreating these data structures over and over
        // and over and over
        CodingSheet codingSheet = column.getDefaultCodingSheet();
        Set<OntologyNode> filteredOntologyNodes = new HashSet<OntologyNode>(integrationColumn.getFilteredOntologyNodes());
        Map<OntologyNode, OntologyNode> closestParentMap = integrationColumn.getNearestParentMap();
        Map<String, OntologyNode> termToNodeMap = codingSheet.getTermToOntologyNodeMap();
        OntologyNode matchingNode = termToNodeMap.get(value);
        if (filteredOntologyNodes.contains(matchingNode)) {
            return matchingNode;
        }
        else {
            return closestParentMap.get(matchingNode);
        }
    }

    public Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generateIntegrationData(
            List<IntegrationColumn> integrationColumns, List<DataTable> tables) {

        // for each column, rehydrate the column and selected ontology nodes
        for (IntegrationColumn integrationColumn : integrationColumns) {
            integrationColumn.setColumns(genericDao.loadFromSparseEntities(integrationColumn.getColumns(), DataTableColumn.class));
            logger.trace("before: {} - {}", integrationColumn, integrationColumn.getFilteredOntologyNodes());
            integrationColumn.setFilteredOntologyNodes(genericDao.loadFromSparseEntities(integrationColumn.getFilteredOntologyNodes(), OntologyNode.class));
            // for each of the integration columns, grab the unique set of all children within an ontology

            // that is, even if child is not selected, should get all children for query and pull up
            integrationColumn.setOntologyNodesForSelect(ontologyNodeService.getAllChildren(integrationColumn.getFilteredOntologyNodes()));

            logger.trace("after: {} - {}", integrationColumn, integrationColumn.getFilteredOntologyNodes());
            logger.info("integration column: {}", integrationColumn);
        }

        // generate projections from each column, first aggregate across common display columns and integration columns
        List<IntegrationDataResult> results = new ArrayList<IntegrationDataResult>();
        // keeps track of all the columns that we need to select out from this data table
        // now iterate through all tables and generate the column lists.
        // use the OntologyNodes on the integration columns. each DataTable should have one integration column in it...
        Map<List<OntologyNode>, Map<DataTable, Integer>> pivot = new LinkedHashMap<List<OntologyNode>, Map<DataTable, Integer>>();

        for (DataTable table : tables) {
            // generate results per table
            IntegrationDataResult integrationDataResult = generateIntegrationResult(table, integrationColumns, pivot);
            results.add(integrationDataResult);
        }
        // Collections.sort(pivot);
        return Pair.create(results, pivot);
    }

    public PersonalFilestoreTicket toExcel(List<IntegrationColumn> integrationColumns,
            Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generatedIntegrationData,
            Person person) {
        List<IntegrationDataResult> integrationDataResults = generatedIntegrationData.getFirst();
        if (CollectionUtils.isEmpty(integrationDataResults)) {
            return null;
        }

        Workbook workbook = new HSSFWorkbook();

        List<String> names = new ArrayList<String>();
        // setting up styles using references from:
        // http://poi.apache.org/spreadsheet/quick-guide.html#FillsAndFrills //NOTE: some values like colors and borders do not match current excel

        // HSSFCellStyle headerStyle = excelService.createHeaderStyle(workbook);
        CellStyle dataTableNameStyle = CellFormat.NORMAL.setColor(new HSSFColor.GREY_25_PERCENT()).createStyle(workbook);
        CellStyle summaryStyle = excelService.createSummaryStyle(workbook);
        // first column is the table where the
        int rowIndex = 0;
        // int columnIndex = 0;

        StringBuilder description = new StringBuilder("Data integration between dataset ");

        List<DataTable> tableList = new ArrayList<DataTable>();
        List<String> columnNames = new ArrayList<String>();
        List<String> datasetNames = new ArrayList<String>();
        createDataSheet(integrationColumns, integrationDataResults, workbook, names, dataTableNameStyle, rowIndex, tableList,
                columnNames, datasetNames);

        description.append(" with datasets: ").append(StringUtils.join(datasetNames, ", "));
        description.append("\n\t using tables: ").append(StringUtils.join(names, ", "));
        description.append("\n\t using columns:").append(StringUtils.join(columnNames, ", "));

        // headerRow.setRowStyle(headerStyle);

        // check that this works in excel on windows:
        // https://issues.apache.org/bugzilla/show_bug.cgi?id=50315
        // FIXME: in poi 3.7 turning this on causes a warning notice in Excel that the file is corrupted, disabling
        // sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columnIndex - 1));

        Map<List<OntologyNode>, Map<DataTable, Integer>> pivot = generatedIntegrationData.getSecond();
        createSummarySheet(workbook, tableList, columnNames, pivot);
        createDescriptionSheet(integrationColumns, person, workbook, summaryStyle, tableList);

        String fileName = "tdar-integration-" + StringUtils.join(names, "_");
        PersonalFilestoreTicket ticket = new PersonalFilestoreTicket();
        ticket.setDateGenerated(new Date());
        ticket.setPersonalFileType(PersonalFileType.INTEGRATION);
        ticket.setSubmitter(person);
        ticket.setDescription(description.toString());
        genericDao.save(ticket);

        try {
            File resultFile = File.createTempFile(fileName, ".xls", TdarConfiguration.getInstance().getTempDirectory());
            resultFile.deleteOnExit();
            workbook.write(new FileOutputStream(resultFile));
            PersonalFilestore filestore = filestoreService.getPersonalFilestore(person);
            filestore.store(ticket, resultFile, fileName + ".xls");
        } catch (Exception iox) {
            logger.error("an error occured when producing the integration excel file: {}", iox);
            throw new TdarRecoverableRuntimeException("could not save file");
        }

        return ticket;
    }

    private void createDescriptionSheet(List<IntegrationColumn> integrationColumns, Person person, Workbook workbook,
            CellStyle summaryStyle,
            List<DataTable> tableList) {
        Sheet summarySheet = workbook.createSheet("Description");
        Row summaryRow = summarySheet.createRow(0);
        // FIXME: Should I have the ontology mappings too??
        excelService.createHeaderCell(summaryStyle, summaryRow, 0,
                "Summary of Integration Results by:" + person.getProperName() + " on " + new SimpleDateFormat().format(new Date()));
        summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        int currentRow = 3;

        List<String> summaryLabels = new ArrayList<String>();
        summaryLabels.add("Table:");
        for (int i = 0; i < tableList.size(); i++) {
            DataTable table = tableList.get(i);
            StringBuilder builder = new StringBuilder(table.getDisplayName());
            builder.append(table.getDataset().getTitle());
            builder.append(" (").append(table.getDataset().getId()).append(")");
            summaryLabels.add(builder.toString());
        }
        excelService.addHeaderRow(summarySheet, 1, 0, summaryLabels);

        for (IntegrationColumn integrationColumn : integrationColumns) {
            List<String> labels = new ArrayList<String>();
            List<String> descriptions = new ArrayList<String>();
            List<String> mappings = new ArrayList<String>();

            descriptions.add("    Description:");
            if (integrationColumn.isIntegrationColumn()) {
                labels.add(" Integration Column:");
                mappings.add("    Mapped Ontology:");
            } else {
                labels.add(" Display Column:");
            }

            for (int i = 0; i < tableList.size(); i++) {
                DataTable table = tableList.get(i);
                DataTableColumn column = integrationColumn.getColumnForTable(table);
                if (column == null) {
                    continue;
                }
                labels.add(column.getDisplayName());
                descriptions.add(column.getDescription());
                if (integrationColumn.isIntegrationColumn()) {
                    Ontology ontology = column.getDefaultOntology();
                    StringBuilder builder = new StringBuilder(ontology.getTitle()).append(" (").append(ontology.getId()).append(")");
                    mappings.add(builder.toString());
                }
            }
            excelService.addDataRow(summarySheet, currentRow++, 0, labels);
            excelService.addDataRow(summarySheet, currentRow++, 0, descriptions);
            if (!mappings.isEmpty()) {
                excelService.addDataRow(summarySheet, currentRow++, 0, mappings);
            }
        }

        // auto-sizing columns
        for (int i = 0; i < summaryLabels.size(); i++) {
            summarySheet.autoSizeColumn(i);
        }
    }

    private void createSummarySheet(Workbook workbook, List<DataTable> tableList, List<String> columnNames,
            Map<List<OntologyNode>, Map<DataTable, Integer>> pivot) {
        int rowIndex;
        Sheet pivotSheet = workbook.createSheet("Summary");

        rowIndex = 2;
        List<String> rowHeaders = new ArrayList<String>(columnNames);
        for (DataTable table : tableList) {
            rowHeaders.add(table.getDisplayName());
        }

        excelService.addHeaderRow(pivotSheet, ExcelService.FIRST_ROW, ExcelService.FIRST_COLUMN, rowHeaders);

        for (List<OntologyNode> key : pivot.keySet()) {
            List<String> rowData = new ArrayList<String>();
            for (OntologyNode col : key) {
                if (col != null) {
                    rowData.add(col.getDisplayName());
                }
            }
            Map<DataTable, Integer> vals = pivot.get(key);
            for (DataTable table : tableList) {
                Integer integer = vals.get(table);
                if (integer == null) {
                    rowData.add("0");
                } else {
                    rowData.add(integer.toString());
                }
            }
            excelService.addDataRow(pivotSheet, rowIndex++, 0, rowData);
        }
    }

    @SuppressWarnings("unchecked")
    private void createDataSheet(List<IntegrationColumn> integrationColumns, List<IntegrationDataResult> integrationDataResults,
            Workbook workbook, List<String> names, CellStyle dataTableNameStyle, int rowIndex, List<DataTable> tableList,
            List<String> columnNames, List<String> datasetNames) {

        // Create header
        List<String> headerLabels = new ArrayList<String>();
        headerLabels.add("Dataset/Table Name");
        for (IntegrationColumn integrationColumn : integrationColumns) {
            columnNames.add(integrationColumn.getName());
            headerLabels.add(integrationColumn.getName());

            if (integrationColumn.isIntegrationColumn()) {
                headerLabels.add("Mapped ontology value for " + integrationColumn.getName());
            }
        }

        List<Iterator<String[]>> iterators = new ArrayList<Iterator<String[]>>();
        // compile the rowdata
        for (IntegrationDataResult integrationDataResult : integrationDataResults) {
            DataTable table = integrationDataResult.getDataTable();
            names.add(table.getName());
            tableList.add(table);
            iterators.add(integrationDataResult.getRowData().iterator());
        }

        // FIXME: support for cell style data table name (C1)
        SheetProxy sheetProxy = new SheetProxy(workbook, "Integration Results");

        sheetProxy.setData(IteratorUtils.chainedIterator(iterators));
        sheetProxy.setHeaderLabels(headerLabels);
        sheetProxy.setFreezeRow(1);
        sheetProxy.setStartRow(rowIndex);
        excelService.addSheets(sheetProxy);

    }

    public String serializeIntegrationContext(List<IntegrationColumn> integrationColumns, Person creator) throws Exception {
        StringWriter sw = new StringWriter();
        xmlService.convertToXML(new IntegrationContext(creator, integrationColumns), sw);
        return sw.toString();
    }

    @Transactional
    public List<CodingRule> findMappedCodingRules(DataTableColumn column) {
        List<String> distinctColumnValues = tdarDataImportDatabase.selectNonNullDistinctValues(column);
        return dataTableColumnDao.findMappedCodingRules(column, distinctColumnValues);
    }

    @Transactional
    public CodingSheet createGeneratedCodingSheet(DataTableColumn column, Person submitter) {
        return createGeneratedCodingSheet(column, submitter, column.getDefaultOntology());
    }

    @Transactional
    public CodingSheet createGeneratedCodingSheet(DataTableColumn column, Person submitter, Ontology ontology) {
        if (column == null) {
            logger.debug("{} tried to create an identity coding sheet for {} with no values", submitter, column);
        }
        CodingSheet codingSheet = new CodingSheet();
        codingSheet.setGenerated(true);
        codingSheet.setTitle("Generated identity coding sheet for " + column.getDisplayName());
        codingSheet.markUpdated(submitter);
        codingSheet.setDate(Calendar.getInstance().get(Calendar.YEAR));
        codingSheet.setDefaultOntology(ontology);
        codingSheet.setCategoryVariable(ontology.getCategoryVariable());
        codingSheet.setDescription(String.format("This identity coding sheet was generated by tDAR for %s column from %s dataset (%s) on %s",
                column, column.getDataTable().getDataset().getTitle(), column.getDataTable().getDataset().getId(), codingSheet.getDateCreated()));
        genericDao.save(codingSheet);
        // generate identity coding rules
        List<String> dataColumnValues = tdarDataImportDatabase.selectNonNullDistinctValues(column);
        List<CodingRule> rules = new ArrayList<CodingRule>();
        for (int index = 0; index < dataColumnValues.size(); index++) {
            String dataValue = dataColumnValues.get(index);
            CodingRule rule = new CodingRule(codingSheet, dataValue);
            genericDao.save(rule);
            rules.add(rule);
        }
        try {
            String baseFileName = codingSheet.getTitle().replace(" ", "_");
            String csvText = convertCodingSheetToCSV(
                    codingSheet, rules);
            FileProxy fileProxy = new FileProxy(baseFileName + ".csv", FileProxy.createTempFileFromString(csvText),
                    VersionType.UPLOADED);
            fileProxy.addVersion(new FileProxy(baseFileName + ".txt", FileProxy.createTempFileFromString(csvText), VersionType.UPLOADED_TEXT));

            informationResourceService.processFileProxy(codingSheet, fileProxy);
        } catch (Exception e) {
            logger.debug("could not process coding sheet", e);
        }

        return codingSheet;
    }

    public String convertCodingSheetToCSV(CodingSheet sheet) {
        return convertCodingSheetToCSV(sheet, sheet.getCodingRules());
    }

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

    public List<List<DataTableColumn>> getIntegrationColumnSuggestions(Collection<DataTable> selectedDataTables) {
        // iterate through all of the columns and get a map of the ones associated
        // with any ontology.
        HashMap<Ontology, List<DataTableColumn>> dataTableAutoMap = new HashMap<Ontology, List<DataTableColumn>>();

        for (DataTable table : selectedDataTables) {
            List<DataTableColumn> dataTableColumns;
            // TODO: remove feature toggle
            if (TdarConfiguration.getInstance().getLeftJoinDataIntegrationFeatureEnabled()) {
                dataTableColumns = table.getLeftJoinColumns();
            } else {
                dataTableColumns = table.getDataTableColumns();
            }
            for (DataTableColumn column : dataTableColumns) {
                if (column.getDefaultOntology() != null) {
                    if (!dataTableAutoMap.containsKey(column.getDefaultOntology())) {
                        dataTableAutoMap.put(column.getDefaultOntology(), new ArrayList<DataTableColumn>());
                    }
                    dataTableAutoMap.get(column.getDefaultOntology()).add(column);
                }
            }
        }

        // okay now we have a map of the data table columns,
        List<List<DataTableColumn>> columnAutoList = new ArrayList<List<DataTableColumn>>();
        for (Ontology key : dataTableAutoMap.keySet()) {
            ArrayList<Long> seen = new ArrayList<Long>();
            ArrayList<Long> seen2 = new ArrayList<Long>();
            List<DataTableColumn> columnList = new ArrayList<DataTableColumn>();
            List<DataTableColumn> columnList2 = new ArrayList<DataTableColumn>();
            // go through the hashMap and try and pair out by set of rules
            // assuming that there is one column per table at a time
            // and there might be a case where there are more than one
            for (DataTableColumn column : dataTableAutoMap.get(key)) {
                if (!seen.contains(column.getDataTable().getId())) {
                    columnList.add(column);
                    seen.add(column.getDataTable().getId());
                } else if (!seen2.contains(column.getDataTable().getId())) {
                    columnList2.add(column);
                    seen2.add(column.getDataTable().getId());
                } // give up
            }

            // might want to tune this to some logic like:
            // if just one table, then anything with an ontology
            // if more than one, just show lists with at least two ontologies
            if (columnList.size() > 0)
                columnAutoList.add(columnList);
            if (columnList2.size() > 0)
                columnAutoList.add(columnList2);
        }
        return columnAutoList;
    }

}
