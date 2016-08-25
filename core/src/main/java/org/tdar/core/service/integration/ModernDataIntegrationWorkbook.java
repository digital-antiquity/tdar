package org.tdar.core.service.integration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ExcelWorkbookWriter;
import org.tdar.core.service.excel.SheetProxy;
import org.tdar.filestore.personal.PersonalFileType;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * Proxy class to handle the generation of the Excel Workbook at the end of the DataIntegration
 * 
 * @author abrin
 * 
 */
public class ModernDataIntegrationWorkbook implements Serializable {

    private static final long serialVersionUID = -1913436731447584442L;
    private ExcelWorkbookWriter workbookWriter = new ExcelWorkbookWriter();
    private Workbook workbook;
    private IntegrationContext context;
    private StringBuilder description = new StringBuilder();
    private TdarUser person;
    private PersonalFilestoreTicket ticket;
    private TextProvider provider;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private ResultSet resultSet;
    private Map<List<OntologyNode>, HashMap<Long, IntContainer>> pivot;
    private ModernIntegrationDataResult result;
    private String rawIntegration;

    public ModernDataIntegrationWorkbook(TextProvider provider, ModernIntegrationDataResult result, String rawIntegration) {
        this.result = result;
        result.setWorkbook(this);
        this.context = result.getIntegrationContext();
        this.person = context.getCreator();
        this.provider = provider;
        this.rawIntegration = rawIntegration;
        setWorkbook(new XSSFWorkbook());
    }

    /**
     * Generate the Excel File
     */
    public void generate() {
        createDataSheet();

        // check that this works in excel on windows:
        // https://issues.apache.org/bugzilla/show_bug.cgi?id=50315
        // FIXME: in poi 3.7 turning this on causes a warning notice in Excel that the file is corrupted, disabling
        // sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columnIndex - 1));

        createPivotSheet();
        createDescriptionSheet();
        createOntologySheets();

    }

    private void createOntologySheets() {
        Set<Ontology> seenOntologies = new HashSet<>();
        for (IntegrationColumn col : context.getIntegrationColumns()) {
            if (!col.isIntegrationColumn() || seenOntologies.contains(col.getSharedOntology()) || PersistableUtils.isNullOrTransient(col.getSharedOntology())) {
                continue;
            }
            Ontology ontology = col.getSharedOntology();
            if (ontology == null) {
                ontology = col.getColumns().get(0).getDefaultCodingSheet().getDefaultOntology();
            }
            int rowIndex = 0;
            seenOntologies.add(ontology);
            String name = provider.getText("dataIntegrationWorkbook.ontology_worksheet", Arrays.asList(ontology.getTitle()));
            logger.debug("creating sheet with name: {}", name);
            if (workbook.getSheet(name) != null) {
                logger.error("sheet already exists: {}", name);
                continue;
            }
            Sheet ontologySheet = workbook.createSheet(name);
            workbookWriter.addHeaderRow(ontologySheet, 0, 0, Arrays.asList(ontology.getTitle()));
            addMergedRegion(0, 0, 0, 8, ontologySheet);
            rowIndex++;
            String termText = provider.getText("dataIntegrationWorkbook.ontology_term");
            String orderText = provider.getText("dataIntegrationWorkbook.ontology_order");
            workbookWriter.addHeaderRow(ontologySheet, rowIndex, 0, Arrays.asList(orderText, termText));
            rowIndex++;
            for (OntologyNode node : ontology.getSortedOntologyNodesByImportOrder()) {
                String order = Long.toString(node.getImportOrder());
                workbookWriter.addDataRow(ontologySheet, rowIndex, 0, Arrays.asList(order, node.getDisplayName()));
                rowIndex++;
            }
        }
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
     */
    public void createDataSheet() {
        // Create header
        // CellStyle dataTableNameStyle = CellFormat.build(Style.NORMAL).setColor(new HSSFColor.GREY_25_PERCENT()).createStyle(getWorkbook());

        List<String> headerLabels = result.getPreviewColumnLabels();

        // FIXME: support for cell style data table name (C1)
        SheetProxy sheetProxy = new SheetProxy(workbook, provider.getText("dataIntegrationWorkbook.data_worksheet"));

        sheetProxy.setHeaderLabels(headerLabels);
        sheetProxy.setFreezeRow(1);
        sheetProxy.setStartRow(0);
        sheetProxy.setAutosizeCols(true);
        Iterable<Object[]> iterator = ResultSetIterator.iterable(resultSet);
        IntegrationResultSetDecorator ird = new IntegrationResultSetDecorator(iterator.iterator(), getContext());
        sheetProxy.setData(ird);
        result.setPivotData(ird.getPivot());
        workbookWriter.addSheets(sheetProxy);
        List<Object[]> previewData = ird.getPreviewData();
        logger.debug("previewData: {}", previewData);
        Collections.sort(previewData, new Comparator<Object[]>() {
            // compare by database name, should sort together
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ObjectUtils.compare((String) o1[0], (String) o2[0]);
            }

        });
        result.setPreviewData(previewData);
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
    private void createDescriptionSheet() {
        CellStyle summaryStyle = workbookWriter.createSummaryStyle(getWorkbook());
        Sheet summarySheet = workbook.createSheet(provider.getText("dataIntegrationWorkbook.description_worksheet"));

        // initial header ... Integration run on ... by ...
        String title = provider.getText("dataIntegrationWorkbook.title",
                Arrays.asList(person.getProperName(), context.getTitle(), new SimpleDateFormat().format(new Date())));
        int currentRow = 0;
        workbookWriter.addHeaderRow(summarySheet, currentRow, 0, Arrays.asList(title));
        currentRow++;
        workbookWriter.addDataRow(summarySheet, currentRow, 0, Arrays.asList(context.getDescription()));
        currentRow++;

        // first and second sub-header:
        // dataset and data table list
        // along with info for each specific column of each dataset
        List<String> header = new ArrayList<>();
        header.add(provider.getText("dataIntegrationWorkbook.int_col_name"));
        header.add(provider.getText("dataIntegrationWorkbook.int_col_type"));
        String[] dataTableNameheader = new String[2000];
        String[] datasetNameheader = new String[2000];
        int increment = 5;
        int count = 0;
        int max = 2 + increment * count;
        for (DataTable table : context.getDataTables()) {
            max = 2 + increment * count;
            dataTableNameheader[max] = formatTableName(table);
            datasetNameheader[max] = provider.getText("dataIntegrationWorkbook.name_paren_id",
                    Arrays.asList(table.getDataset().getName(), table.getDataset().getId()));
            count++;
            header.addAll(Arrays.asList(provider.getText("dataIntegrationWorkbook.col_name"),
                    provider.getText("dataIntegrationWorkbook.col_name"), provider.getText("dataIntegrationWorkbook.col_description"),
                    provider.getText("dataIntegrationWorkbook.col_type"), provider.getText("dataIntegrationWorkbook.col_ontology"),
                    provider.getText("dataIntegrationWorkbook.col_coding_sheet")));
        }
        workbookWriter.addHeaderRow(summarySheet, currentRow, 0, Arrays.asList(ArrayUtils.subarray(datasetNameheader, 0, max + increment)));
        currentRow++;
        workbookWriter.addHeaderRow(summarySheet, currentRow, 0, Arrays.asList(ArrayUtils.subarray(dataTableNameheader, 0, max + increment)));
        currentRow++;
        workbookWriter.addRow(summarySheet, currentRow, 0, header, summaryStyle);
        currentRow++;
        // for each integration column, print out all of the column type info, and then add more if it's a true integration column
        for (IntegrationColumn col : context.getIntegrationColumns()) {
            currentRow++;
            String[] row = new String[header.size()];
            row[0] = col.getName();
            row[1] = "";
            if (col.getColumnType() != null) {
                row[1] = col.getColumnType().name();
            }

            int size = 2;
            for (DataTable table : context.getDataTables()) {
                DataTableColumn dtc = col.getColumnForTable(table);
                if (dtc != null) {
                    row[size] = dtc.getName();
                    row[size + 1] = dtc.getDescription();
                    row[size + 2] = dtc.getColumnDataType().getLabel();
                    Ontology defaultOntology = dtc.getMappedOntology();
                    if (defaultOntology != null) {
                        row[size + 3] = provider.getText("dataIntegrationWorkbook.name_paren_id",
                                Arrays.asList(defaultOntology.getName(), defaultOntology.getId()));
                    }
                    CodingSheet defaultCodingSheet = dtc.getDefaultCodingSheet();
                    if (defaultCodingSheet != null) {
                        row[size + 4] = provider.getText("dataIntegrationWorkbook.name_paren_id",
                                Arrays.asList(defaultCodingSheet.getName(), defaultCodingSheet.getId()));
                    }
                    size = size + 5;
                }
            }
            workbookWriter.addDataRow(summarySheet, currentRow, 0, Arrays.asList(row));
            // if real integration column, show the mapping info

            if (col.isIntegrationColumn()) {
                List<String> row2 = new ArrayList<>(Arrays.asList(row));
                row2.set(0, provider.getText("dataIntegrationWorkbook.col_mapped", Arrays.asList(row[0])));
                workbookWriter.addDataRow(summarySheet, currentRow++, 0, row2);
            }
            // show selected ontology nodes
            workbookWriter.addDataRow(summarySheet, currentRow++, 2, Arrays.asList(provider.getText("dataIntegrationWorkbook.selected_nodes")));
            for (OntologyNode node : col.getFilteredOntologyNodes()) {
                workbookWriter.addDataRow(summarySheet, currentRow++, 3, Arrays.asList(node.getDisplayName()));
            }
        }

        workbookWriter.addDataRow(summarySheet, currentRow + 4, 1,Arrays.asList("JSON:",rawIntegration));
        // auto-sizing columns
        for (int i = 0; i < max; i++) {
            summarySheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the "pivot" table worksheet ("summmary")
     */
    private void createPivotSheet() {
        int rowIndex;
        Sheet pivotSheet = workbook.createSheet(provider.getText("dataIntegrationWorkbook.summary_worksheet"));
        logger.debug("{} - {}", provider, person);
        String title = provider.getText("dataIntegrationWorkbook.title",
                Arrays.asList(person.getProperName(), context.getTitle(), new SimpleDateFormat().format(new Date())));
        workbookWriter.addHeaderRow(pivotSheet, 0, 0, Arrays.asList(title));
        addMergedRegion(0, 0, 0, 8, pivotSheet);
        if (context.hasCountColumn()) {
            workbookWriter.addHeaderRow(pivotSheet, 1, 0, Arrays.asList(provider.getText("dataIntegrationWorkbook.pivot_description")));
        } else {
            workbookWriter.addHeaderRow(pivotSheet, 1, 0, Arrays.asList(provider.getText("dataIntegrationWorkbook.pivot_description_count_warning")));
        }
        addMergedRegion(1, 1, 0, 8, pivotSheet);

        rowIndex = 4;
        List<String> rowHeaders = result.getPivotColumnLabels();

        workbookWriter.addHeaderRow(pivotSheet, ExcelWorkbookWriter.FIRST_ROW + 3, ExcelWorkbookWriter.FIRST_COLUMN, rowHeaders);

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
            Map<Long, IntContainer> vals = pivot.get(key);
            for (DataTable table : context.getDataTables()) {
                IntContainer integer = vals.get(table.getId());
                if (integer == null) {
                    rowData.add("0");
                } else {
                    rowData.add(Integer.toString(integer.getVal()));
                }
            }
            workbookWriter.addDataRow(pivotSheet, rowIndex++, 0, rowData);
        }
        workbookWriter.addDataRow(pivotSheet, rowIndex + 2, 0, Arrays.asList(provider.getText("dataIntegrationWorkbook.pivot_note")));
        addMergedRegion(rowIndex + 2, rowIndex + 3, 0, 8, pivotSheet);
    }

    private void addMergedRegion(int startRow, int endRow, int startCol, int endCol, Sheet pivotSheet) {
        pivotSheet.addMergedRegion(new CellRangeAddress(
                startRow, // first row (0-based)
                endRow, // last row (0-based)
                startCol, // first column (0-based)
                endCol // last column (0-based)
                ));
    }

    public static String formatTableName(DataTable table) {
        return String.format("%s - %s", table.getDataset().getTitle(), table.getDisplayName());
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
        String fileName = provider.getText("dataIntegrationWorkbook.file_name", Arrays.asList(context.getTitle()));
        return fileName;
    }

    /**
     * write to temp file
     * 
     * @return
     * @throws IOException
     */
    public File writeToTempFile() throws IOException {
        File resultFile = File.createTempFile(getFileName(), ".xlsx", TdarConfiguration.getInstance().getTempDirectory());
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
