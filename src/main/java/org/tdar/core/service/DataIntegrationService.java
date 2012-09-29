package org.tdar.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.PersonalFileType;
import org.tdar.filestore.PersonalFilestore;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.struts.data.IntegrationRowData;

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

    private Logger logger = Logger.getLogger(getClass());

    @Autowired
    private PostgresDatabase tdarDataImportDatabase;

    @Autowired
    private GenericService genericService;

    @Autowired
    private FilestoreService filestoreService;

    /**
     * Returns a lookup / association list / substitution Map<OntologyNode, OntologyNode> pairing, where
     * all nodes in the Ontology exist in the keySet, and the mapped value represents the appropriately aggregated
     * OntologyNode substitution for the given key. As a concrete example, given the ontology below:
     * 
     * <pre>
     * Wine
     *      WhiteWine
     *          Zinfandel
     *      RedWine
     *          Merlot
     *          Cabernet
     * </pre>
     * 
     * If the user selected Wine and RedWine, the mappings would look like this:
     * 
     * Zinfandel -> Wine
     * WhiteWine -> Wine
     * Wine -> Wine
     * RedWine -> RedWine
     * Merlot -> RedWine
     * Cabernet -> RedWine
     * 
     * 
     * @param hierarchyMap
     * @return
     */
    private Map<OntologyNode, OntologyNode> generateAggregateMap(Map<OntologyNode, List<OntologyNode>> hierarchyMap) {
        HashMap<OntologyNode, OntologyNode> aggregateMap = new HashMap<OntologyNode, OntologyNode>();

        Set<OntologyNode> selectedOntologyNodes = hierarchyMap.keySet();
        for (Map.Entry<OntologyNode, List<OntologyNode>> entry : hierarchyMap.entrySet()) {
            OntologyNode selectedOntologyNode = entry.getKey();
            List<OntologyNode> allChildren = entry.getValue();
            for (OntologyNode childNode : allChildren) {
                if (selectedOntologyNodes.contains(childNode)) {
                    // this child node has been selected - none of it or its children should be included in the aggregate mapping.
                    List<OntologyNode> childNodeChildren = hierarchyMap.get(childNode);
                    aggregateMap.put(childNode, childNode);
                    // associate all
                    for (OntologyNode childNodeChild : childNodeChildren) {
                        aggregateMap.put(childNodeChild, childNode);
                    }
                } else {
                    aggregateMap.put(childNode, selectedOntologyNode);
                }
            }
            aggregateMap.put(selectedOntologyNode, selectedOntologyNode);
        }
        return aggregateMap;
    }

    /**
     * Returns a list of integration data results. Each IntegrationDataResult consists of a list of RowData, each row corresponding to one tuple in the dataset.
     * 
     * @param tableColumns
     * @param tableToSelectedOntologyNodes
     * @param tableToOntologyNodeHierarchyMap
     * @param displayAttributes
     * @return
     */
    public List<IntegrationDataResult> generateIntegrationData(
            Map<DataTable, List<DataTableColumn>> tableColumns,
            Map<DataTable, Map<OntologyNode, List<OntologyNode>>> tableToOntologyNodeHierarchyMap,
            Map<DataTable, List<DataTableColumn>> displayAttributes) {
        // generate projections from each column, first aggregate across common display columns and integration columns
        ArrayList<IntegrationDataResult> results = new ArrayList<IntegrationDataResult>();
        // keeps track of all the columns that we need to select out from this data table
        // now iterate through all tables and generate the column lists.
        // use the OntologyNodes on the integration columns. each DataTable should have one integration column in it...

        for (Map.Entry<DataTable, List<DataTableColumn>> entry : tableColumns.entrySet()) {
            final DataTable table = entry.getKey();
            final List<DataTableColumn> integrationColumns = entry.getValue();
            List<DataTableColumn> columnsToDisplay = new ArrayList<DataTableColumn>();
            if (!CollectionUtils.isEmpty(displayAttributes.get(table))) {
                columnsToDisplay = displayAttributes.get(table);
            }

            Map<OntologyNode, List<OntologyNode>> ontologyNodeHierarchyMap = tableToOntologyNodeHierarchyMap.get(table);
            final Set<OntologyNode> selectedOntologyNodes = ontologyNodeHierarchyMap.keySet();
            // use hierarchy + selected ontology nodes to compute aggregate map.
            final Map<OntologyNode, OntologyNode> aggregatedOntologyNodeMap = generateAggregateMap(ontologyNodeHierarchyMap);
            Set<OntologyNode> allOntologyNodes = aggregatedOntologyNodeMap.keySet();

            getLogger().debug("selected ontology nodes: " + selectedOntologyNodes);
            getLogger().debug("aggregated ontology node map: " + aggregatedOntologyNodeMap);
            getLogger().debug("all ontology nodes: " + allOntologyNodes);
            IntegrationDataResult integrationDataResult = tdarDataImportDatabase.generateIntegrationResult(table, integrationColumns, columnsToDisplay,
                    aggregatedOntologyNodeMap,
                    allOntologyNodes);
            results.add(integrationDataResult);
        }
        return results;
    }

    public HSSFCell createCell(HSSFRow row, int position, String value) {
        return createCell(row, position, value, null);
    }

    public HSSFCell createCell(HSSFRow row, int position, String value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(position);

        if (!StringUtils.isEmpty(value)) {
            if (StringUtils.isNumeric(value)) {
                cell.setCellValue(Double.valueOf(value));
            } else {
                cell.setCellValue(value);
            }
        }
        if (style != null) {
            cell.setCellStyle(style);
        }

        return cell;
    }

    public PersonalFilestoreTicket toExcel(List<List<DataTableColumn>> integrationColumnGroups,
            Map<DataTable, List<DataTableColumn>> displayAttributeMap, List<IntegrationDataResult> integrationDataResults, Person person) {
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
        HSSFCellStyle headerStyle = workbook.createCellStyle(); // HEADER_STYLE
        headerStyle.setFont(font);
        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerStyle.setFillForegroundColor(new HSSFColor.LIGHT_CORNFLOWER_BLUE().getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        HSSFCellStyle dataTableNameStyle = workbook.createCellStyle(); // DATA_TABLE_NAME_STYLE
        dataTableNameStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        dataTableNameStyle.setFillForegroundColor(new HSSFColor.GREY_25_PERCENT().getIndex());

        HSSFCellStyle summaryStyle = workbook.createCellStyle();
        HSSFFont summaryFont = workbook.createFont();
        summaryFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        summaryStyle.setFont(summaryFont);
        summaryStyle.setWrapText(true);

        HSSFSheet sheet = workbook.createSheet("Integration Results");
        // first column is the table where the
        int rowIndex = 0;
        int columnIndex = 0;
        HSSFRow headerRow = sheet.createRow(rowIndex++);
        createCell(headerRow, columnIndex++, "Dataset/Table Name", headerStyle);

        StringBuilder description = new StringBuilder("Data integration between dataset ");
        int integrationSet = 0;

        List<DataTable> tableList = new ArrayList<DataTable>();
        List<List<DataTableColumn>> integrationColumnList = new ArrayList<List<DataTableColumn>>();
        int totalIntegrationColumns = 0;
        // for each result
        for (IntegrationDataResult integrationDataResult : integrationDataResults) {
            int headerColumnIndex = 1;

            DataTable table = integrationDataResult.getDataTable();
            names.add(table.getName());
            getLogger().debug("creating sheet with name: " + table.getName());
            int numIntegrationColumns = integrationDataResult.getIntegrationColumns().size();

            // create header row
            tableList.add(integrationDataResult.getDataTable());
            List<String> columnNames = new ArrayList<String>();
            for (DataTableColumn integrationColumn : integrationDataResult.getIntegrationColumns()) {
                if (integrationSet == 0) {
                    createCell(headerRow, columnIndex++, integrationColumn.getDisplayName(), headerStyle);
                    createCell(headerRow, columnIndex++, "Mapped ontology value for " + integrationColumn.getName(), headerStyle);
                } else {
                    HSSFCell cell = headerRow.getCell(headerColumnIndex);
                    cell.setCellValue(cell.getStringCellValue() + ", " + integrationColumn.getDisplayName());
                    cell = headerRow.getCell(headerColumnIndex + 1);
                    cell.setCellValue(cell.getStringCellValue() + ", " + integrationColumn.getDisplayName());
                }
                columnNames.add(integrationColumn.getDisplayName());
                headerColumnIndex++;
            }
            totalIntegrationColumns = columnNames.size();
            integrationColumnList.add(integrationDataResult.getIntegrationColumns());
            integrationSet++;
            int displayColumnsStartAt = columnIndex;

            // add dataset specific display columns
            for (DataTableColumn displayColumn : integrationDataResult.getColumnsToDisplay()) {
                columnNames.add(displayColumn.getDisplayName() + " (display)");
                createCell(headerRow, columnIndex++, displayColumn.getDisplayName(), headerStyle);
            }

            // add description
            if (integrationSet > 1) {
                description.append(" with dataset \n");
            }
            description.append(table.getDataset().getTitle() + " using data table: ");
            description.append(table.getDisplayName()).append(" and columns: ");
            description.append(StringUtils.join(columnNames, ", "));

            // iterate through the actual data values, row by row
            int rowDataColumnIndex = 0;
            for (IntegrationRowData rowData : integrationDataResult.getRowData()) {
                HSSFRow row = sheet.createRow(rowIndex++);
                createCell(row, rowDataColumnIndex++, table.getDataset().getTitle(), dataTableNameStyle);

                List<String> dataValues = rowData.getDataValues();
                for (int i = 0; i < integrationDataResult.getIntegrationColumns().size(); i++) {
                    List<OntologyNode> mappedValues = rowData.getOntologyValues();

                    if (i < dataValues.size()) {
                        createCell(row, rowDataColumnIndex++, dataValues.get(i));
                    } else { // this should never happen
                        createCell(row, rowDataColumnIndex++, "NO VALUE");
                    }
                    if (i < mappedValues.size()) {
                        createCell(row, rowDataColumnIndex++, (mappedValues.get(i) == null)
                                ? "This value has not been mapped to any ontology node."
                                        : mappedValues.get(i).getDisplayName());
                    } else { // this should never happen
                        createCell(row, rowDataColumnIndex++, "NO MAPPED VALUE");
                    }
                }
                rowDataColumnIndex = displayColumnsStartAt;
                for (int i = numIntegrationColumns; i < dataValues.size(); i++) {
                    createCell(row, rowDataColumnIndex++, dataValues.get(i));
                }

                rowDataColumnIndex = 0;
            }
        }

        headerRow.setRowStyle(headerStyle);
        // freeze header row
        sheet.createFreezePane(0, 1, 0, 1);

        // check that this works in excel on windows:
        // https://issues.apache.org/bugzilla/show_bug.cgi?id=50315
        // FIXME: in poi 3.7 turning this on causes a warning notice in Excel that the file is corrupted, disabling
        // sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columnIndex - 1));

        HSSFSheet summarySheet = workbook.createSheet("Summary");

        HSSFRow row = summarySheet.createRow(0);

        createHeaderCell(summaryStyle, row, "Summary of Integration Results by:" + person.getProperName() + " on " + new SimpleDateFormat().format(new Date()));
        summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        int currentRow = 1;
        currentRow += 2;
        row = summarySheet.createRow(currentRow);

        createHeaderCell(summaryStyle, row, "Table:");
        for (int i = 0; i < tableList.size(); i++) {
            DataTable table = tableList.get(i);
            StringBuilder builder = new StringBuilder(table.getDisplayName());
            builder.append(table.getDataset().getTitle());
            builder.append(" (").append(table.getDataset().getId()).append(")");
            HSSFCell cell = row.createCell(i + 1);
            cell.setCellValue(builder.toString());
            cell.setCellStyle(summaryStyle);
        }
        currentRow++;

        for (int intRowNum = 0; intRowNum < totalIntegrationColumns; intRowNum++) {
            row = summarySheet.createRow(currentRow);
            HSSFRow descriptionRow = summarySheet.createRow(currentRow + 1);
            HSSFRow mappingRow = summarySheet.createRow(currentRow + 2);
            createHeaderCell(summaryStyle, row, " Integration Column:");
            createCell(descriptionRow, 0, "    Description:");
            createCell(mappingRow, 0, "    Mapped Ontology:");

            for (int colIndex = 0; colIndex < integrationColumnList.size(); colIndex++) {
                DataTableColumn column = integrationColumnList.get(colIndex).get(intRowNum);
                createCell(row, colIndex + 1, column.getDisplayName(), summaryStyle);
                createCell(descriptionRow, colIndex + 1, integrationColumnList.get(colIndex).get(intRowNum).getDescription());
                Ontology ontology = column.getDefaultOntology();
                StringBuilder builder = new StringBuilder(ontology.getTitle());
                builder.append(" (").append(ontology.getId()).append(")");
                createCell(mappingRow, colIndex + 1, builder.toString());
            }
            currentRow += 3;

        }

        for (DataTable table : displayAttributeMap.keySet()) {
            int colNum = tableList.indexOf(table);
            for (DataTableColumn column : displayAttributeMap.get(table)) {
                row = summarySheet.createRow(currentRow);
                HSSFRow descriptionRow = summarySheet.createRow(currentRow + 1);
                createHeaderCell(summaryStyle, row, " Display Column:");
                createCell(descriptionRow, 0, "    Description:");

                createCell(row, colNum + 1, column.getDisplayName(), summaryStyle);
                createCell(descriptionRow, colNum + 1, column.getDescription());
                currentRow += 2;
            }
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
            logger.error(iox);
            throw new TdarRecoverableRuntimeException("could not save file");
        }

        return ticket;
    }

    private void createHeaderCell(HSSFCellStyle summaryStyle, HSSFRow row, String text) {
        HSSFCell headerCell = row.createCell(0);
        headerCell.setCellValue(text);
        headerCell.setCellStyle(summaryStyle);
    }

    public void setTdarDataImportDatabase(PostgresDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }

    private Logger getLogger() {
        return logger;
    }

}
