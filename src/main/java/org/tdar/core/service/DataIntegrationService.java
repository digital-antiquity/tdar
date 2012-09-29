package org.tdar.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.OntologyNodeService;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.personalFilestore.PersonalFileType;
import org.tdar.filestore.personalFilestore.PersonalFilestore;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationContext;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.utils.Pair;

/**
 * $Id$
 * 
 * Provides data integration functionality for datasets that have
 * been translated and mapped to ontologies.
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
    private GenericService genericService;

    @Autowired
    private PersonalFilestoreService filestoreService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private OntologyNodeService ontologyNodeService;

    @Autowired
    private XmlService xmlService;

    public void setTdarDataImportDatabase(PostgresDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }

    public Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generateIntegrationData(
            List<IntegrationColumn> integrationColumns, List<DataTable> tables) {

        // for each column, rehydrate the column and selected ontology nodes
        for (IntegrationColumn column : integrationColumns) {
            column.setColumns(genericService.rehydrateSparseIdBeans(column.getColumns(), DataTableColumn.class));
            logger.trace("before: {} - {}", column, column.getFilteredOntologyNodes());
            column.setFilteredOntologyNodes(genericService.rehydrateSparseIdBeans(column.getFilteredOntologyNodes(), OntologyNode.class));
            // for each of the integration columns, grab the unique set of all children within an ontology

            // that is, even if child is not selected, should get all children for query and pull up
            column.setOntologyNodesForSelect(ontologyNodeService.getAllChildren(column.getFilteredOntologyNodes()));

            logger.trace("after: {} - {}", column, column.getFilteredOntologyNodes());
            logger.info("integration column: {}" + column);
        }

        // generate projections from each column, first aggregate across common display columns and integration columns
        ArrayList<IntegrationDataResult> results = new ArrayList<IntegrationDataResult>();
        // keeps track of all the columns that we need to select out from this data table
        // now iterate through all tables and generate the column lists.
        // use the OntologyNodes on the integration columns. each DataTable should have one integration column in it...
        Map<List<OntologyNode>, Map<DataTable, Integer>> pivot = new LinkedHashMap<List<OntologyNode>, Map<DataTable, Integer>>();

        for (DataTable table : tables) {
            // generate results per table
            IntegrationDataResult integrationDataResult = tdarDataImportDatabase.generateIntegrationResult(table, integrationColumns, pivot);
            results.add(integrationDataResult);
        }
        // Collections.sort(pivot);
        return new Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>>(results, pivot);
    }

    public PersonalFilestoreTicket toExcel(List<IntegrationColumn> integrationColumns,
            Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generatedIntegrationData,
            Person person) {
        List<IntegrationDataResult> integrationDataResults = generatedIntegrationData.getFirst();
        if (CollectionUtils.isEmpty(integrationDataResults)) {
            return null;
        }

        // get total number of display and integration columns
        // line them up integration, integration (filtered), display...

        HSSFSheet sheet = excelService.createWorkbook("Integration Results");
        HSSFWorkbook workbook = sheet.getWorkbook();

        List<String> names = new ArrayList<String>();
        // setting up styles using references from:
        // http://poi.apache.org/spreadsheet/quick-guide.html#FillsAndFrills //NOTE: some values like colors and borders do not match current excel

        // HSSFCellStyle headerStyle = excelService.createHeaderStyle(workbook);
        HSSFCellStyle dataTableNameStyle = excelService.createTableNameStyle(workbook);
        HSSFCellStyle summaryStyle = excelService.createSummaryStyle(workbook);
        List<String> headerLabels = new ArrayList<String>();
        // freeze header row
        sheet.createFreezePane(ExcelService.FIRST_COLUMN, 1, 0, 1);
        // first column is the table where the
        int rowIndex = 1;
        // int columnIndex = 0;
        headerLabels.add("Dataset/Table Name");

        StringBuilder description = new StringBuilder("Data integration between dataset ");

        List<DataTable> tableList = new ArrayList<DataTable>();
        List<String> columnNames = new ArrayList<String>();
        List<String> datasetNames = new ArrayList<String>();
        sheet = createDataSheet(integrationColumns, integrationDataResults, sheet, workbook, names, dataTableNameStyle, headerLabels, rowIndex, tableList,
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
        createDescriptionSheet(integrationColumns, person, sheet, workbook, summaryStyle, tableList);

        String fileName = "tdar-integration-" + StringUtils.join(names, "_");
        PersonalFilestoreTicket ticket = new PersonalFilestoreTicket();
        ticket.setDateGenerated(new Date());
        ticket.setPersonalFileType(PersonalFileType.INTEGRATION);
        ticket.setSubmitter(person);
        ticket.setDescription(description.toString());
        genericService.save(ticket);

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

    private void createDescriptionSheet(List<IntegrationColumn> integrationColumns, Person person, HSSFSheet sheet, HSSFWorkbook workbook,
            HSSFCellStyle summaryStyle,
            List<DataTable> tableList) {
        HSSFSheet summarySheet = workbook.createSheet("Description");
        HSSFRow summaryRow = summarySheet.createRow(0);

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
            sheet.autoSizeColumn(i);
            summarySheet.autoSizeColumn(i);
        }
    }

    private void createSummarySheet(HSSFWorkbook workbook, List<DataTable> tableList, List<String> columnNames,
            Map<List<OntologyNode>, Map<DataTable, Integer>> pivot) {
        int rowIndex;
        HSSFSheet pivotSheet = workbook.createSheet("Summary");

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

    private HSSFSheet createDataSheet(List<IntegrationColumn> integrationColumns, List<IntegrationDataResult> integrationDataResults, HSSFSheet sheet,
            HSSFWorkbook workbook, List<String> names, HSSFCellStyle dataTableNameStyle, List<String> headerLabels, int rowIndex, List<DataTable> tableList,
            List<String> columnNames, List<String> datasetNames) {
        // create header
        int numDataSheets = 1;
        for (IntegrationColumn integrationColumn : integrationColumns) {
            columnNames.add(integrationColumn.getName());
            headerLabels.add(integrationColumn.getName());

            if (integrationColumn.isIntegrationColumn()) {
                headerLabels.add("Mapped ontology value for " + integrationColumn.getName());
            }
        }
        excelService.addHeaderRow(sheet, ExcelService.FIRST_ROW, ExcelService.FIRST_COLUMN, headerLabels);

        for (IntegrationDataResult integrationDataResult : integrationDataResults) {
            DataTable table = integrationDataResult.getDataTable();
            datasetNames.add(table.getDataset().getTitle());
            names.add(table.getName());

            tableList.add(table);

            // iterate through the actual data values, row by row
            for (List<String> rowData_ : integrationDataResult.getRowData()) {
                List<String> rowData = new ArrayList<String>(rowData_);
                rowData.add(ExcelService.FIRST_COLUMN, table.getDataset().getTitle());
                rowIndex++;
                excelService.addDataRow(sheet, rowIndex, ExcelService.FIRST_COLUMN, rowData);
                excelService.setCellStyle(sheet, rowIndex, ExcelService.FIRST_COLUMN, dataTableNameStyle);
            }

            // if we are more than excel can handle, clone the header column and start in a new workboook
            if (rowIndex >= ExcelService.MAX_ROWS_PER_SHEET - 1) {
                numDataSheets++;
                sheet = workbook.createSheet("results continued " + numDataSheets);
                // freeze header row
                sheet.createFreezePane(0, 1, 0, 1);
                // check if adding a new style causes an issue here
                excelService.addHeaderRow(sheet, ExcelService.FIRST_ROW, ExcelService.FIRST_COLUMN, headerLabels);
                rowIndex = 1;
            }
        }
        return sheet;
    }

    public String serializeIntegrationContext(List<IntegrationColumn> integrationColumns, Person creator) throws Exception {
        StringWriter sw = new StringWriter();
        xmlService.convertToXML(new IntegrationContext(creator, integrationColumns), sw);
        return sw.toString();
    }

}
