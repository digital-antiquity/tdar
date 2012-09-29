package org.tdar.db.conversion.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.conversion.ConversionStatisticsManager;
import org.tdar.db.model.abstracts.TargetDatabase;

/**
 * Uses Apache POI to parse and convert Excel workbooks into a unique table,
 * currently in the tdardata postgres database.
 * 
 * FIXME: error messages should be externalized
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class ExcelConverter extends DatasetConverter.Base {

    private static final String ERROR_CORRUPT_FILE_TRY_RESAVING = "tDAR cannot read some rows in this workbook, possibly due to a corrupt file. This issue can often be resolved by simply opening the file using in Microsoft Excel and then re-saving the document.";
    private static final String ERROR_WRONG_EXCEL_FORMAT = "We could not open the Excel file you supplied.  Please try re-saving it in Excel as an Excel 97-2003 Workbook or Excel 2007 Workbook and upload it again.  If this problem persists, please contact us with a bug report.";
    private static final String POI_ERROR_MISSING_ROWS = "Unexpected missing row when some rows already present";
    private static final String DB_PREFIX = "e";
    private Workbook workbook;
    private DataFormatter formatter = new HSSFDataFormatter();

    public String getDatabasePrefix() {
        return DB_PREFIX;
    }


    public ExcelConverter() {}

    public ExcelConverter(InformationResourceFileVersion version, TargetDatabase targetDatabase) {
        setTargetDatabase(targetDatabase);
        this.setInformationResourceFileVersion(version);

    }

    protected void openInputDatabase()
            throws IOException {
        if (informationResourceFileVersion == null) {
            logger.warn("Received null information resource file.");
            return;
        }
        File excelFile = informationResourceFileVersion.getFile();
        if (excelFile == null) {
            logger.error("InformationResourceFile's file was null, this should never happen.");
            return;
        }
        try {
            workbook = WorkbookFactory.create(new FileInputStream(excelFile));
        } catch (InvalidFormatException exception) {
            exception.printStackTrace();
            String errorMessage = "Couldn't create workbook from "
                    + excelFile.getAbsolutePath();
            logger.error(errorMessage, exception);
            throw new TdarRecoverableRuntimeException(errorMessage, exception);
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();
            logger.error("Couldn't create workbook, likely due to invalid Excel file or Excel 2003 file.");
            throw new TdarRecoverableRuntimeException(
                    ERROR_WRONG_EXCEL_FORMAT,
                    exception);
        } catch (RuntimeException rex) {
           //if this is a "missing rows" issue, the user might be able to work around it by resaving the excel spreadsheet;
            if(rex.getMessage().equals(POI_ERROR_MISSING_ROWS)) {
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
    public void dumpData() throws Exception {
        int numberOfSheets = workbook.getNumberOfSheets();

        List<Exception> exceptions = new ArrayList<Exception>();
        for (int sheetIndex = 0; sheetIndex < numberOfSheets; ++sheetIndex) {
            // skip empty sheets
            Sheet currentSheet = workbook.getSheetAt(sheetIndex);
            String sheetName = workbook.getSheetName(sheetIndex);
            if (currentSheet.getPhysicalNumberOfRows() < 2) {
                logger.warn(String.format(
                        "Sheet # %d (%s) only had %d rows, skipping.",
                        sheetIndex, sheetName,
                        currentSheet.getPhysicalNumberOfRows()));
                continue;
            }
            try {
                processSheet(currentSheet, sheetName);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        if (exceptions.size() > 0) {
            throw exceptions.get(0);
        }
    }

    private void generateDataTableColumns(Row columnNamesRow, DataTable dataTable) {
        // FIXME: this is this column only, it is NOT the max # of columns for the workbook
        // right now, if this differs, we throw an exception
        int endColumnIndex = columnNamesRow.getLastCellNum();
        // create the table and insert the tuples
        for (int i = 0; i < endColumnIndex; i++) {
            String columnName = "Column #" + i + 1;
            try {
                FormulaEvaluator evaluator = columnNamesRow.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                String cellValue = formatter.formatCellValue(columnNamesRow.getCell(i), evaluator);
                if (StringUtils.isNotBlank(cellValue)) {
                    columnName = cellValue;
                }
            } catch (NullPointerException npe) {
                logger.trace("assigning arbitrary column name to blank column");
            }
            createDataTableColumn(columnName, DataTableColumnType.TEXT, dataTable);
        }
    }

    // convert a single excel sheet to a database table in the target database
    private void processSheet(Sheet currentSheet, String sheetName) throws Exception {
        /**
         * Our conversion approach will go like this: 1) Initially we create a
         * table with all TEXT(nullable) columns 2) For each record in the
         * sheet, store a row in our all-text table, but analyze the text being
         * inserted into each cell, and maintain statistics to see if we can
         * ultimately convert the column of that cell to a more desirable
         * dataype (for example, if every value in a column can safely convert
         * to an int, we deem it more desirable for thaty column to be BIGINT
         * than TEXT) 3) After all inserts have been made, we check our final
         * statistics to see if we can safely convert the table columns to a
         * more specific datatype.
         */

        DataTable dataTable = createDataTable(sheetName);
        logger.info("processing Worksheet:" + sheetName);
        // extract schema from the current sheet.
        // assume first row contains column names
        Row columnNamesRow = currentSheet.getRow(currentSheet.getFirstRowNum());
        generateDataTableColumns(columnNamesRow, dataTable);
        if (dataTable.getDataTableColumns() == null || dataTable.getDataTableColumns().isEmpty()) {
            try {
                columnNamesRow = currentSheet.getRow(currentSheet.getFirstRowNum() + 1);
                generateDataTableColumns(columnNamesRow, dataTable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (dataTable.getDataTableColumns() == null || dataTable.getDataTableColumns().isEmpty()) {
            logger.info(sheetName + " appears to be empty or have non-tabular data, skipping data table");
            dataTables.remove(dataTable);
        }

        int startColumnIndex = columnNamesRow.getFirstCellNum();
        int endColumnIndex = columnNamesRow.getLastCellNum();

        logger.debug("{}", dataTable.getDataTableColumns());
        targetDatabase.createTable(dataTable);

        // initialize our most-desired-datatype statistics
        ConversionStatisticsManager statisticsManager = new ConversionStatisticsManager(dataTable.getDataTableColumns());

        // insert data into the table.

        int startRow = currentSheet.getFirstRowNum() + 1;
        // the last row is the size of the column names list instead of
        // currentSheet.getLastRowNum()
        // we ignore data that doesn't have a column heading).
        int endRow = currentSheet.getLastRowNum();
        // we also assume that no blanks exist between any consecutive
        // columns.
        FormulaEvaluator evaluator = currentSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

        for (int rowIndex = startRow; rowIndex <= endRow; ++rowIndex) {
            Row currentRow = currentSheet.getRow(rowIndex);
            if (currentRow == null)
                continue;

            if (columnNamesRow.getLastCellNum() < currentRow.getLastCellNum()) {
                for (int i=columnNamesRow.getLastCellNum(); i<= currentRow.getLastCellNum();i++) {
                    String value = formatter.formatCellValue(currentRow.getCell(i), evaluator);
                    if (!StringUtils.isEmpty(value)) {
                        throw new TdarRecoverableRuntimeException("row #" + rowIndex + " has more columns  (" + currentRow.getLastCellNum()
                                + ") than this sheet has column names (" + columnNamesRow.getLastCellNum()
                                + ") - " + currentSheet.getSheetName());
                    }
                }

            }
            Map<DataTableColumn, String> valueColumnMap = new HashMap<DataTableColumn, String>();
            for (int columnIndex = startColumnIndex; columnIndex < endColumnIndex; ++columnIndex) {
                Cell currentCell = currentRow.getCell(columnIndex);
                if (currentCell != null) {
                    // XXX: adding all Excel types as the String representation
                    // of how they appear in the Excel sheet
                    // in order to avoid issues with parsing numeric fields.
                    // Otherwise currentCell.getNumericCellValue()
                    // will return a double like 3.0 instead of the number 3
                    String cellValue = formatter.formatCellValue(currentCell, evaluator);
                    if (StringUtils.isEmpty(cellValue)) {
                        cellValue = null;
                    }
                    valueColumnMap.put(
                            dataTable.getDataTableColumns().get(columnIndex),
                            cellValue);
                    statisticsManager.updateStatistics(dataTable.getDataTableColumns().get(columnIndex), cellValue);
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
