package org.tdar.db.conversion.converters;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.conversion.ConversionStatisticsManager;
import org.tdar.db.model.abstracts.TargetDatabase;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Converts text CSV files into a postgres database.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class CsvConverter extends SimpleConverter {

    protected static final String DB_PREFIX = "csv";
    protected CSVReader reader;
    protected String[] headerLine;
    protected String tableName = "";

    public String getDatabasePrefix() {
        return DB_PREFIX;
    }

    public CsvConverter() {
    };

    public CsvConverter(TargetDatabase targetDatabase,InformationResourceFileVersion ... versions) {
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(versions[0]);
    }

    protected void openInputDatabase()
            throws IOException {
        if (informationResourceFileVersion == null) {
            logger.warn("Received null information resource file.");
            return;
        }
        File csvFile = informationResourceFileVersion.getTransientFile();
        if (csvFile == null) {
            logger.error("InformationResourceFile's file was null, this should never happen.");
            return;
        }
        setReader(new CSVReader(new FileReader(csvFile)));
        // grab first line as header.
        setTableName(FilenameUtils.getBaseName(csvFile.getName()));
        setHeaderLine(getReader().readNext());
        setIrFileId(informationResourceFileVersion.getId());
    }

    /**
     * Do the job, to convert the db file and put the data into the
     * corresponding db.
     * 
     * @param targetDatabase
     */
    public void dumpData() throws Exception {

        DataTable dataTable = createDataTable(getTableName());

        for (int i = 0; i < getHeaderLine().length; i++) {
            createDataTableColumn(getHeaderLine()[i], DataTableColumnType.TEXT, dataTable);
        }

        targetDatabase.createTable(dataTable);

        // initialize our most-desired-datatype statistics
        ConversionStatisticsManager statisticsManager = new ConversionStatisticsManager(dataTable.getDataTableColumns());

        // iterate through the rest of the CSVReader file.
        int numberOfLines = 0;
        while (true) {
            String[] line = getReader().readNext();
            if (line == null) {
                // what to do? are we done?
                logger.debug("line was null after processing " + numberOfLines);
                break;
            }
            numberOfLines++;
            // 1-based count for PreparedStatement's weirdness.
            int count = 1;
            Map<DataTableColumn, String> columnToValueMap = new HashMap<DataTableColumn, String>();
            if (line.length > getHeaderLine().length)
                throw new TdarRecoverableRuntimeException("row " + numberOfLines + " has more columns " + line.length + " than the header column");

            for (int i = 0; i < line.length; i++) {
                if (count <= getHeaderLine().length) {
                    columnToValueMap.put(dataTable.getDataTableColumns().get(i), line[i]);
                    statisticsManager.updateStatistics(dataTable.getDataTableColumns().get(i), line[i]);
                } else {
                    logger.warn("Discarding degenerate data value at index " + count + " : " + line[i]);
                }
            }
            targetDatabase.addTableRow(dataTable, columnToValueMap);

        }
        completePreparedStatements();
        alterTableColumnTypes(dataTable, statisticsManager.getStatistics());

    }

    public CSVReader getReader() {
        return reader;
    }

    public void setReader(CSVReader reader) {
        this.reader = reader;
    }

    public String[] getHeaderLine() {
        return headerLine;
    }

    public void setHeaderLine(String[] headerLine) {
        this.headerLine = headerLine;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
