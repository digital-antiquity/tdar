package org.tdar.core.service.integration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbutils.ResultSetIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.excel.SheetProxy;
import org.tdar.filestore.personal.PersonalFileType;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationContext;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.TextProvider;

/**
 * Proxy class to handle the generation of the Excel Workbook at the end of the DataIntegration
 * 
 * @author abrin
 * 
 */
public class ModernDataIntegrationWorkbook implements Serializable {

    private static final long serialVersionUID = -1913436731447584442L;
    private transient ExcelService excelService;
    private Workbook workbook;
    private IntegrationContext context;
    private StringBuilder description = new StringBuilder();
    private TdarUser person;
    private List<String> names;
    private PersonalFilestoreTicket ticket;
    private TextProvider provider;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private ResultSet resultSet;
    private Map<List<OntologyNode>, HashMap<String, IntContainer>> pivot;
    private ModernIntegrationDataResult result;

    public ModernDataIntegrationWorkbook(TextProvider provider, ExcelService excelService, ModernIntegrationDataResult result) {
        this.setExcelService(excelService);
        this.result = result;
        result.setWorkbook(this);
        this.context = result.getIntegrationContext();
        this.person = context.getCreator();
        this.provider = provider;
        setWorkbook(new HSSFWorkbook());
        names = new ArrayList<String>();
    }

    /**
     * Generate the Excel File
     */
    public void generate() {
        // HSSFCellStyle headerStyle = excelService.createHeaderStyle(workbook);
        CellStyle summaryStyle = excelService.createSummaryStyle(getWorkbook());
        // first column is the table where the
        setDescription(new StringBuilder(provider.getText("dataIntegrationWorkbook.descr")).append(" "));
        createDataSheet();
        List<DataTable> tableList = new ArrayList<DataTable>();
        List<String> columnNames = new ArrayList<String>();
        List<String> datasetNames = new ArrayList<String>();

        getDescription().append(MessageHelper.getMessage("dataIntegrationWorkbook.descr_with_datasets")).append(" ")
                .append(StringUtils.join(datasetNames, ", ")).append("\n\t ").append(MessageHelper.getMessage("dataIntegrationWorkbook.descr_using_tables"))
                .append(": ").append(StringUtils.join(names, ", ")).append("\n\t ")
                .append(MessageHelper.getMessage("dataIntegrationWorkbook.descr_using_columns"))
                .append(":").append(StringUtils.join(columnNames, ", "));

        // headerRow.setRowStyle(headerStyle);

        // check that this works in excel on windows:
        // https://issues.apache.org/bugzilla/show_bug.cgi?id=50315
        // FIXME: in poi 3.7 turning this on causes a warning notice in Excel that the file is corrupted, disabling
        // sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columnIndex - 1));

        createSummarySheet();
        createDescriptionSheet(context.getIntegrationColumns(), person, getWorkbook(), summaryStyle, tableList);

    }

    /**
     * Generate a @link PersonalFilestoreTicket for the excel file
     */
    private void generateTicket() {
        PersonalFilestoreTicket ticket = new PersonalFilestoreTicket();
        ticket.setDateGenerated(new Date());
        ticket.setPersonalFileType(PersonalFileType.INTEGRATION);
        ticket.setSubmitter(person);
        ticket.setDescription(getDescription().toString());
        this.setTicket(ticket);
    }

    /**
     * Create the workbook for the actual data
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
    public void createDataSheet() {
        // Create header
        // CellStyle dataTableNameStyle = CellFormat.build(Style.NORMAL).setColor(new HSSFColor.GREY_25_PERCENT()).createStyle(getWorkbook());

        List<String> headerLabels = new ArrayList<String>();
        headerLabels.add(MessageHelper.getMessage("dataIntegrationWorkbook.data_table"));
        for (IntegrationColumn integrationColumn : context.getIntegrationColumns()) {
            headerLabels.add(integrationColumn.getName());

            if (integrationColumn.isIntegrationColumn()) {
                headerLabels.add(provider.getText("dataIntegrationWorkbook.data_mapped_value", Arrays.asList(integrationColumn.getName())));
            }
        }

        // FIXME: support for cell style data table name (C1)
        SheetProxy sheetProxy = new SheetProxy(workbook, MessageHelper.getMessage("dataIntegrationWorkbook.data_worksheet"));

        // sheetProxy.setData(IteratorUtils.chainedIterator(iterators));
        sheetProxy.setHeaderLabels(headerLabels);
        sheetProxy.setFreezeRow(1);
        sheetProxy.setStartRow(0);
        Iterable<Object[]> iterator = ResultSetIterator.iterable(resultSet);
        // ModernDataIntegrationWorkbook workbook, IntegrationContext context, DataTable table, ResultSet resultSet
        InteegrationResultSetDecorator ird = new InteegrationResultSetDecorator(iterator.iterator(), getContext());
        sheetProxy.setData(ird);
        result.setPivotData(ird.getPivot());
        getExcelService().addSheets(sheetProxy);
        logger.debug("previewData: {}", ird.getPreviewData());
        result.setPreviewData(ird.getPreviewData());
        pivot = ird.getPivot();
    }

    /**
     * Create the Description worksheet
     * 
     * @param integrationColumns
     * @param person
     * @param workbook
     * @param summaryStyle
     * @param tableList
     */
    private void createDescriptionSheet(List<IntegrationColumn> integrationColumns, TdarUser person, Workbook workbook,
            CellStyle summaryStyle,
            List<DataTable> tableList) {
        Sheet summarySheet = workbook.createSheet(MessageHelper.getMessage("dataIntegrationWorkbook.description_worksheet"));
        Row summaryRow = summarySheet.createRow(0);
        // FIXME: Should I have the ontology mappings too??
        excelService
                .createHeaderCell(
                        summaryStyle,
                        summaryRow,
                        0,
                        provider.getText("dataIntegrationWorkbook.description_header",
                                Arrays.asList(person.getProperName(), new SimpleDateFormat().format(new Date()))));
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
            descriptions.add(provider.getText("dataIntegrationWorkbook.description_description_column", Arrays.asList("    ")));
            if (integrationColumn.isIntegrationColumn()) {
                labels.add(provider.getText("dataIntegrationWorkbook.description_integration_column", Arrays.asList("    ")));
                mappings.add(provider.getText("dataIntegrationWorkbook.description_mapped_column", Arrays.asList("    ")));
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

    /**
     * Create the "pivot" table worksheet ("summmary")
     * 
     * @param workbook
     * @param tableList
     * @param columnNames
     * @param pivot
     */
    private void createSummarySheet() {
        int rowIndex;
        Sheet pivotSheet = workbook.createSheet(MessageHelper.getMessage("dataIntegrationWorkbook.summary_worksheet"));

        rowIndex = 2;
        List<String> rowHeaders = new ArrayList<>();
        for (IntegrationColumn col : context.getIntegrationColumns()) {
            if (col.isIntegrationColumn()) {
                rowHeaders.add(col.getName());
            }
        }
        for (DataTable table : context.getDataTables()) {
            rowHeaders.add(table.getName());
        }

        excelService.addHeaderRow(pivotSheet, ExcelService.FIRST_ROW, ExcelService.FIRST_COLUMN, rowHeaders);

        for (List<OntologyNode> key : pivot.keySet()) {
            logger.trace("key: {}", key);
            List<String> rowData = new ArrayList<String>();
            for (OntologyNode col : key) {
                if (col != null) {
                    rowData.add(col.getDisplayName());
                    // rowData.add(col.getIndex());
                }
            }
            // Map<List<OntologyNode>, HashMap<String, IntContainer>>
            Map<String, IntContainer> vals = pivot.get(key);
            for (DataTable table : context.getDataTables()) {
                IntContainer integer = vals.get(table.getName());
                if (integer == null) {
                    rowData.add("0");
                } else {
                    rowData.add(Integer.toString(integer.getVal()));
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

    public StringBuilder getDescription() {
        return description;
    }

    public void setDescription(StringBuilder description) {
        this.description = description;
    }

    public TdarUser getPerson() {
        return person;
    }

    public void setPerson(TdarUser person) {
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
        // MD5 is probably overkill, but we want a filename that is unique based on the included result sheets while avoiding any OS filename restrictions (e.g.
        // maxlength)
        String basename = StringUtils.join(names, "");
        String basenameMd5 = DigestUtils.md5Hex(basename);
        String fileName = provider.getText("dataIntegrationWorkbook.file_name", Arrays.asList(basenameMd5));
        return fileName;
    }

    /**
     * write to temp file
     * 
     * @return
     * @throws IOException
     */
    public File writeToTempFile() throws IOException {
        File resultFile = File.createTempFile(getFileName(), ".xls", TdarConfiguration.getInstance().getTempDirectory());
        logger.trace("writing temp file:{}", resultFile);
        resultFile.deleteOnExit();
        getWorkbook().write(new FileOutputStream(resultFile));
        return resultFile;
    }

    public void setResultSet(ResultSet arg0) {
        this.resultSet = arg0;
    }

    public IntegrationContext getContext() {
        return context;
    }

    public void setContext(IntegrationContext context) {
        this.context = context;
    }

}
