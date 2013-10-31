package org.tdar.core.service.integration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.excel.CellFormat;
import org.tdar.core.service.excel.SheetProxy;
import org.tdar.filestore.personal.PersonalFileType;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.utils.Pair;

public class DataIntegrationWorkbook  implements Serializable {

    private static final long serialVersionUID = -2452046179173301666L;
    private ExcelService excelService;
    private Workbook workbook;
    private List<IntegrationColumn> integrationColumns;
    private List<IntegrationDataResult> integrationDataResults;
    private StringBuilder description;
    private Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generatedIntegrationResults;
    private Person person;
    private List<String> names;
    private PersonalFilestoreTicket ticket;

    public DataIntegrationWorkbook(ExcelService excelService, Person person, Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generatedIntegrationData) {
        this.setExcelService(excelService);
        this.person = person;
        this.setGeneratedIntegrationResults(generatedIntegrationData);
        setWorkbook(new HSSFWorkbook());

        names = new ArrayList<String>();

        // HSSFCellStyle headerStyle = excelService.createHeaderStyle(workbook);
        CellStyle dataTableNameStyle = CellFormat.NORMAL.setColor(new HSSFColor.GREY_25_PERCENT()).createStyle(getWorkbook());
        CellStyle summaryStyle = excelService.createSummaryStyle(getWorkbook());
        // first column is the table where the
        int rowIndex = 0;
        // int columnIndex = 0;

        setDescription(new StringBuilder("Data integration between dataset "));

        List<DataTable> tableList = new ArrayList<DataTable>();
        List<String> columnNames = new ArrayList<String>();
        List<String> datasetNames = new ArrayList<String>();
        createDataSheet(names, dataTableNameStyle, rowIndex, tableList, columnNames, datasetNames);

        getDescription().append(" with datasets: ").append(StringUtils.join(datasetNames, ", "));
        getDescription().append("\n\t using tables: ").append(StringUtils.join(names, ", "));
        getDescription().append("\n\t using columns:").append(StringUtils.join(columnNames, ", "));

        // headerRow.setRowStyle(headerStyle);

        // check that this works in excel on windows:
        // https://issues.apache.org/bugzilla/show_bug.cgi?id=50315
        // FIXME: in poi 3.7 turning this on causes a warning notice in Excel that the file is corrupted, disabling
        // sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columnIndex - 1));

        Map<List<OntologyNode>, Map<DataTable, Integer>> pivot = generatedIntegrationData.getSecond();
        createSummarySheet(getWorkbook(), tableList, columnNames, pivot);
        createDescriptionSheet(getIntegrationColumns(), person, getWorkbook(), summaryStyle, tableList);

    }
    
    private void generateTicket() {
        PersonalFilestoreTicket ticket = new PersonalFilestoreTicket();
        ticket.setDateGenerated(new Date());
        ticket.setPersonalFileType(PersonalFileType.INTEGRATION);
        ticket.setSubmitter(person);
        ticket.setDescription(getDescription().toString());
        this.setTicket(ticket);
    }
    
    /**
     * 
     * @param integrationColumns
     * @param integrationDataResults
     * @param workbook
     * @param names
     * @param dataTableNameStyle
     * @param rowIndex
     * @param tableList
     * @param columnNames
     * @param datasetNames
     */
    @SuppressWarnings("unchecked")
    public void createDataSheet(List<String> names, CellStyle dataTableNameStyle, int rowIndex, List<DataTable> tableList,
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
        getExcelService().addSheets(sheetProxy);

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


    public ExcelService getExcelService() {
        return excelService;
    }

    public void setExcelService(ExcelService excelService) {
        this.excelService = excelService;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public List<IntegrationColumn> getIntegrationColumns() {
        return integrationColumns;
    }

    public void setIntegrationColumns(List<IntegrationColumn> integrationColumns) {
        this.integrationColumns = integrationColumns;
    }

    public List<IntegrationDataResult> getIntegrationDataResults() {
        return integrationDataResults;
    }

    public void setIntegrationDataResults(List<IntegrationDataResult> integrationDataResults) {
        this.integrationDataResults = integrationDataResults;
    }

    public StringBuilder getDescription() {
        return description;
    }

    public void setDescription(StringBuilder description) {
        this.description = description;
    }

    public Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> getGeneratedIntegrationResults() {
        return generatedIntegrationResults;
    }

    public void setGeneratedIntegrationResults(Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generatedIntegrationResults) {
        this.generatedIntegrationResults = generatedIntegrationResults;
    }

    public Person getPerson() {
        return person;
    }
    
    public void setPerson(Person person) {
        this.person = person;
    }

    public PersonalFilestoreTicket getTicket() {
        if (ticket == null) {
            generateTicket();
        }
        return ticket;
    }

    public void setTicket(PersonalFilestoreTicket ticket) {
        this.ticket = ticket;
    }

    public String getFileName() {
        String fileName = "tdar-integration-" + StringUtils.join(names, "_") + ".xls";
        return fileName;
    }
    
    public File writeToFile() throws IOException {
        File resultFile = File.createTempFile(getFileName(), ".xls", TdarConfiguration.getInstance().getTempDirectory());
        resultFile.deleteOnExit();
        getWorkbook().write(new FileOutputStream(resultFile));
        return resultFile;
    }
}
