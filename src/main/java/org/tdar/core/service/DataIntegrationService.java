package org.tdar.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
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
import org.tdar.filestore.PersonalFileType;
import org.tdar.filestore.PersonalFilestore;
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
    private FilestoreService filestoreService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private OntologyNodeService ontologyNodeService;

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
        //Collections.sort(pivot);
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

        HSSFWorkbook workbook = new HSSFWorkbook();
        List<String> names = new ArrayList<String>();
        // setting up styles using references from:
        // http://poi.apache.org/spreadsheet/quick-guide.html#FillsAndFrills //NOTE: some values like colors and borders do not match current excel

        HSSFFont font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFCellStyle headerStyle = excelService.createHeaderStyle(workbook, font);
        HSSFCellStyle dataTableNameStyle = excelService.createTableNameStyle(workbook);
        HSSFCellStyle summaryStyle = excelService.createSummaryStyle(workbook);

        HSSFSheet sheet = workbook.createSheet("Integration Results");
        // freeze header row
        sheet.createFreezePane(0, 1, 0, 1);
        // first column is the table where the
        int rowIndex = 0;
        int columnIndex = 0;
        HSSFRow headerRow = sheet.createRow(rowIndex++);
        excelService.createCell(headerRow, columnIndex++, "Dataset/Table Name", headerStyle);

        StringBuilder description = new StringBuilder("Data integration between dataset ");

        List<DataTable> tableList = new ArrayList<DataTable>();
        List<String> columnNames = new ArrayList<String>();
        List<String> datasetNames = new ArrayList<String>();
        // create header
        int numDataSheets = 1;
        for (IntegrationColumn integrationColumn : integrationColumns) {
            columnNames.add(integrationColumn.getName());
            excelService.createCell(headerRow, columnIndex++, integrationColumn.getName(), headerStyle);

            if (integrationColumn.isIntegrationColumn()) {
                excelService.createCell(headerRow, columnIndex++, "Mapped ontology value for " + integrationColumn.getName(), headerStyle);
            }
        }

        for (IntegrationDataResult integrationDataResult : integrationDataResults) {
            DataTable table = integrationDataResult.getDataTable();
            datasetNames.add(table.getDataset().getTitle());
            names.add(table.getName());

            tableList.add(table);

            // iterate through the actual data values, row by row
            for (List<String> rowData : integrationDataResult.getRowData()) {
                int rowDataColumnIndex = 0;
                HSSFRow row = sheet.createRow(rowIndex++);
                excelService.createCell(row, rowDataColumnIndex++, table.getDataset().getTitle(), dataTableNameStyle);
                for (String value : rowData) {
                    excelService.createCell(row, rowDataColumnIndex++, value);
                }
            }

            // if we are more than excel can handle, clone the header column and start in a new workboook
            if (rowIndex >= ExcelService.MAX_ROWS_PER_SHEET - 1) {
                numDataSheets++;
                sheet = workbook.createSheet("results continued " + numDataSheets);
                // freeze header row
                sheet.createFreezePane(0, 1, 0, 1);
                HSSFRow newHeaderRow = sheet.createRow(0);
                Iterator<Cell> cellIterator = headerRow.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell nextCell = cellIterator.next();
                    excelService.createCell(newHeaderRow, nextCell.getColumnIndex(), nextCell.getStringCellValue(), headerStyle);
                }
                rowIndex = 1;
            }
        }

        description.append(" with datasets: ").append(StringUtils.join(datasetNames, ", "));
        description.append("\n\t using tables: ").append(StringUtils.join(names, ", "));
        description.append("\n\t using columns:").append(StringUtils.join(columnNames, ", "));

        headerRow.setRowStyle(headerStyle);

        // check that this works in excel on windows:
        // https://issues.apache.org/bugzilla/show_bug.cgi?id=50315
        // FIXME: in poi 3.7 turning this on causes a warning notice in Excel that the file is corrupted, disabling
        // sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columnIndex - 1));

        Map<List<OntologyNode>, Map<DataTable, Integer>> pivot = generatedIntegrationData.getSecond();
        HSSFSheet pivotSheet = workbook.createSheet("Summary");
        int colNum = 0;
        rowIndex = 0;
        HSSFRow row = pivotSheet.createRow(rowIndex++);

        for (String name : columnNames) {
            excelService.createHeaderCell(summaryStyle, row, colNum, name);
            colNum++;
        }
        for (DataTable table : tableList) {
            excelService.createHeaderCell(summaryStyle, row, colNum, table.getDisplayName());
            colNum++;
        }

        for (List<OntologyNode> key : pivot.keySet()) {
            colNum = 0;
            row = pivotSheet.createRow(rowIndex++);
            for (OntologyNode col : key) {
                if (col != null) {
                    excelService.createCell(row, colNum, col.getDisplayName());
                }
                colNum++;
            }
            Map<DataTable, Integer> vals = pivot.get(key);
            for (DataTable table : tableList) {
                Integer integer = vals.get(table);
                if (integer == null) {
                    excelService.createCell(row, colNum, "0");
                } else {
                    excelService.createCell(row, colNum, integer.toString());
                }
                colNum++;
            }
        }

        HSSFSheet summarySheet = workbook.createSheet("Description");

        HSSFRow summaryRow = summarySheet.createRow(0);

        excelService.createHeaderCell(summaryStyle, summaryRow, 0,
                "Summary of Integration Results by:" + person.getProperName() + " on " + new SimpleDateFormat().format(new Date()));
        summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        int currentRow = 1;
        currentRow += 2;
        summaryRow = summarySheet.createRow(currentRow);

        excelService.createHeaderCell(summaryStyle, summaryRow, 0, "Table:");
        for (int i = 0; i < tableList.size(); i++) {
            DataTable table = tableList.get(i);
            StringBuilder builder = new StringBuilder(table.getDisplayName());
            builder.append(table.getDataset().getTitle());
            builder.append(" (").append(table.getDataset().getId()).append(")");
            HSSFCell cell = summaryRow.createCell(i + 1);
            cell.setCellValue(builder.toString());
            cell.setCellStyle(summaryStyle);
        }
        currentRow++;

        for (IntegrationColumn integrationColumn : integrationColumns) {
            summaryRow = summarySheet.createRow(currentRow);
            int repeatNum = 2;
            HSSFRow descriptionRow = summarySheet.createRow(currentRow + 1);
            HSSFRow mappingRow = summarySheet.createRow(currentRow + 2);
            if (integrationColumn.isIntegrationColumn()) {
                excelService.createHeaderCell(summaryStyle, summaryRow, 0, " Integration Column:");
                excelService.createCell(descriptionRow, 0, "    Description:");
                excelService.createCell(mappingRow, 0, "    Mapped Ontology:");
                repeatNum++;
            } else {
                excelService.createHeaderCell(summaryStyle, summaryRow, 0, " Display Column:");
                excelService.createCell(descriptionRow, 0, "    Description:");
            }
            int colIndex = 0;
            for (int i = 0; i < tableList.size(); i++) {
                DataTable table = tableList.get(i);
                DataTableColumn column = integrationColumn.getColumnForTable(table);
                if (column == null) {
                    continue;
                }
                excelService.createCell(summaryRow, colIndex + 1, column.getDisplayName(), summaryStyle);
                excelService.createCell(descriptionRow, colIndex + 1, column.getDescription());
                if (integrationColumn.isIntegrationColumn()) {
                    Ontology ontology = column.getDefaultOntology();
                    StringBuilder builder = new StringBuilder(ontology.getTitle());
                    builder.append(" (").append(ontology.getId()).append(")");
                    excelService.createCell(mappingRow, colIndex + 1, builder.toString());
                }
                colIndex++;
            }
            currentRow += repeatNum;
        }

        // auto-sizing columns
        for (int i = 0; i < columnIndex; i++) {
            sheet.autoSizeColumn(i);
            summarySheet.autoSizeColumn(i);
        }

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

    public String serializeIntegrationContext(List<IntegrationColumn> integrationColumns, Person creator) {
        StringWriter writer = new StringWriter();
        try {
            JAXBContext jc = JAXBContext.newInstance(IntegrationContext.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(new IntegrationContext(creator, integrationColumns), writer);
        } catch (JAXBException exception) {
            exception.printStackTrace();
        }
        return writer.toString();
    }

}
