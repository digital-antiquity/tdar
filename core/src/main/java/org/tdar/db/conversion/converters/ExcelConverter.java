package org.tdar.db.conversion.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.excel.SheetEvaluator;
import org.tdar.datatable.DataTableColumnType;
import org.tdar.datatable.TDataTable;
import org.tdar.datatable.TDataTableColumn;
import org.tdar.db.conversion.ConversionStatisticsManager;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.MessageHelper;

/**
 * Uses Apache POI to parse and convert Excel workbooks into a unique table,
 * currently in the tdardata postgres database.
 * 
 * FIXME: error messages should be externalized
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class ExcelConverter extends AbstractDatabaseConverter {

    private static final String ERROR_CORRUPT_FILE_TRY_RESAVING = "excelConverter.error_corrupt_file_try_resaving";
    private static final String ERROR_POI_MISSING_ROWS = "excelConverter.poi_error_missing_rows";
    public static final String ERROR_WRONG_EXCEL_FORMAT = "excelConverter.error_wrong_excel_format";
    private static final String DEFAULT_SHEET_NAME = "Sheet1";
    private static final String DB_PREFIX = "e";
    private Workbook workbook;
    // private DataFormatter formatter = new HSSFDataFormatter();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private boolean xssfEnabled = false;

    @Override
    public String getDatabasePrefix() {
        return DB_PREFIX;
    }

    public ExcelConverter() {
    }

    // FIXME: what is the point of these constructors? Just for tests?
    public ExcelConverter(TargetDatabase targetDatabase, InformationResourceFileVersion... versions) {
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(versions[0]);
        setFilename(versions[0].getFilename());
    }

    @Override
    protected void openInputDatabase() throws IOException {
        if (informationResourceFileVersion == null) {
            logger.warn("Received null information resource file.");
            return;
        }
        File excelFile = informationResourceFileVersion.getTransientFile();
        if (excelFile == null) {
            logger.error("InformationResourceFile's file was null, this should never happen.");
            return;
        }
        try {

            if (xssfEnabled && StringUtils.endsWithIgnoreCase(excelFile.getName(), "xlsx")) {
                workbook = new SXSSFWorkbook(new XSSFWorkbook(excelFile), 100);
                logger.debug("# sheets {}", workbook.getNumberOfSheets());
            } else {
                // helper.processSheet(null, 25);
                workbook = WorkbookFactory.create(new FileInputStream(excelFile));
            }
        } catch (NullPointerException npe) {
            logger.error("{}", npe);
        } catch (InvalidFormatException exception) {
            logger.debug("cannot read excel file (invalid format)", exception);
            String errorMessage = "Couldn't create workbook from " + excelFile.getAbsolutePath();
            logger.error(errorMessage, exception);
            throw new TdarRecoverableRuntimeException(errorMessage, exception);
        } catch (IllegalArgumentException exception) {
            logger.error("Couldn't create workbook, likely due to invalid Excel file or Excel 2003 file.", exception);
            throw new TdarRecoverableRuntimeException(ERROR_WRONG_EXCEL_FORMAT, exception);
        } catch (RuntimeException rex) {
            // if this is a "missing rows" issue, the user might be able to work around it by resaving the excel spreadsheet;
            if (rex.getMessage().equals(MessageHelper.getMessage(ERROR_POI_MISSING_ROWS))) {
                throw new TdarRecoverableRuntimeException(ERROR_CORRUPT_FILE_TRY_RESAVING);
            } else {
                throw rex;
            }
        }
        setIrFileId(informationResourceFileVersion.getId());
    }

    /**
     * Do the job, to convert the db file and put the data into the
     * corresponding db.
     * 
     * @param targetDatabase
     */
    @Override
    public void dumpData() throws Exception {
        int numberOfSheets = workbook.getNumberOfSheets();

        List<Exception> exceptions = new ArrayList<Exception>();
        int numberOfActualSheets = 0;
        for (int sheetIndex = 0; sheetIndex < numberOfSheets; sheetIndex++) {
            // skip empty sheets
            Sheet currentSheet = workbook.getSheetAt(sheetIndex);
            String sheetName = workbook.getSheetName(sheetIndex);
            if (currentSheet.getPhysicalNumberOfRows() < 2) {
                logger.warn(String.format("Sheet # %d (%s) only had %d rows, skipping.", sheetIndex, sheetName, currentSheet.getPhysicalNumberOfRows()));
                continue;
            }
            numberOfActualSheets++;
            try {
                if ((numberOfActualSheets == 1) && sheetName.equals(DEFAULT_SHEET_NAME)) {
                    sheetName = FilenameUtils.getBaseName(informationResourceFileVersion.getTransientFile().getName());
                }
                processSheet(currentSheet, sheetName, numberOfActualSheets - 1);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        if (exceptions.size() > 0) {
            for (Exception e : exceptions) {
                logger.warn("Exception while processing excel workbook sheets", e);
            }
            // FIXME: why reraise?
            throw exceptions.get(0);
        }
    }

    /**
     * Converts a single excel sheet to a database table in the target database.
     * 
     * 1) Initially create a table with all TEXT(nullable) columns
     * 2) For each record in the sheet, store a row in our all-text table, but analyze the text being inserted into each cell, and maintain statistics to see if
     * we can ultimately convert the column of that cell to a more desirable dataype (for example, if every value in a column can safely convert
     * to an int, we deem it more desirable for that column to be BIGINT than TEXT)
     * 3) After all inserts have been made, check final statistics to see if we can safely convert the table columns to a
     * more specific datatype.
     */
    private void processSheet(Sheet currentSheet, String sheetName, int order) throws Exception {
        logger.info("processing Worksheet: {}", sheetName);
        // evaluate schema from the current sheet.
        SheetEvaluator sheetEvaluator = new SheetEvaluator(currentSheet);
        // sheetEvalator.evaluateBeginning(currentSheet, 25);
        if (!sheetEvaluator.hasTabularData()) {
            // FIXME: shouldn't use exceptions for flow control
            // throw new TdarRecoverableRuntimeException("excelConverter.no_tabular_data", Arrays.asList(sheetName));
            logger.warn("no tabular data found for sheet {}", sheetName);
            return;
        }
        // create the data table + columns based on the SheetEvaluator's reported headers.
        TDataTable dataTable = createDataTable(sheetName, order);
        int count = 0;
        for (String columnName : sheetEvaluator.getHeaderColumnNames()) {
            createDataTableColumn(columnName, DataTableColumnType.TEXT, dataTable, count);
            count++;
        }
        // FIXME: will this conditional ever happen?
        if ((dataTable.getDataTableColumns() == null) || dataTable.getDataTableColumns().isEmpty()) {
            logger.info("{} appears to be empty or have non-tabular data, skipping data table", sheetName);
            dataTables.remove(dataTable);
            // FIXME: old code used to continue in this situation if there was no tabular data
            return;
        }
        final int startColumnIndex = sheetEvaluator.getDataColumnStartIndex();
        final int endColumnIndex = sheetEvaluator.getDataColumnEndIndex();

        logger.debug("{}", dataTable.getDataTableColumns());
        targetDatabase.createTable(dataTable);

        // initialize our most-desired-datatype statistics
        ConversionStatisticsManager statisticsManager = new ConversionStatisticsManager(dataTable.getDataTableColumns());

        // insert data into the table.
        final int startRow = sheetEvaluator.getDataRowStartIndex();
        // the last row is the size of the column names list instead of
        // currentSheet.getLastRowNum()
        // we ignore data that doesn't have a column heading).
        final int endRow = currentSheet.getLastRowNum();
        // we also assume that no blanks exist between any consecutive
        // columns.

        for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
            Row currentRow = currentSheet.getRow(rowIndex);
            if (currentRow == null) {
                continue;
            }
            if (currentRow.getFirstCellNum() < 0) {
                continue;
            }
            sheetEvaluator.validate(currentRow);
            Map<TDataTableColumn, String> valueColumnMap = new HashMap<>();
            for (int columnIndex = startColumnIndex; columnIndex <= endColumnIndex; columnIndex++) {
                Cell currentCell = currentRow.getCell(columnIndex);
                if (currentCell != null) {
                    // XXX: adding all Excel types as the String representation
                    // of how they appear in the Excel sheet
                    // in order to avoid issues with parsing numeric fields.
                    // Otherwise currentCell.getNumericCellValue()
                    // will return a double like 3.0 instead of the number 3
                    String cellValue = sheetEvaluator.getCellValueAsString(currentCell);
                    if (StringUtils.isEmpty(cellValue)) {
                        cellValue = null;
                    }
                    // make sure to offset by the startColumnIndex. sheet data starts at startColumnIndex but DataTableColumns are zero-based
                    TDataTableColumn column = dataTable.getDataTableColumns().get(columnIndex - startColumnIndex);
                    valueColumnMap.put(column, cellValue);
                    statisticsManager.updateStatistics(column, cellValue, rowIndex);
                }
            }
            logger.trace("inserting {} into {}", valueColumnMap, dataTable.getName());
            targetDatabase.addTableRow(dataTable, valueColumnMap);
        }
        // now that we have our final statistics on how we we can convert each
        // table column, let's alter the table and update our column information
        completePreparedStatements();
        alterTableColumnTypes(dataTable, statisticsManager.getStatistics());
    }
}
