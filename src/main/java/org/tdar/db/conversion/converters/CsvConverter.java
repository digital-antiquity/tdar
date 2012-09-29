package org.tdar.db.conversion.converters;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;
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
public class CsvConverter extends DatasetConverter.Base {

    private CSVReader reader;
    private String[] headerLine;
    private String tableName = "";

    public CsvConverter() {
        setDatabasePrefix("csv");
    }

    public CsvConverter(InformationResourceFileVersion version, TargetDatabase targetDatabase) {
        setDatabasePrefix("csv");
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(version);
    }

    protected void openInputDatabase()
            throws IOException {
        if (informationResourceFileVersion == null) {
            logger.warn("Received null information resource file.");
            return;
        }
        File csvFile = informationResourceFileVersion.getFile();
        if (csvFile == null) {
            logger.error("InformationResourceFile's file was null, this should never happen.");
            return;
        }
        reader = new CSVReader(new FileReader(csvFile));
        // grab first line as header.
        tableName = FilenameUtils.getBaseName(csvFile.getName());
        headerLine = reader.readNext();
        setIrFileId(informationResourceFileVersion.getId());
    }

    /**
     * Do the job, to convert the db file and put the data into the
     * corresponding db.
     * 
     * @param targetDatabase
     */
    public void dumpData() throws Exception {

        DataTable dataTable = createDataTable(tableName);

        for (int i = 0; i < headerLine.length; i++) {
            createDataTableColumn(headerLine[i], DataTableColumnType.TEXT,
                    dataTable);
        }

        targetDatabase.createTable(dataTable);

        // initialize our most-desired-datatype statistics
        ConversionStatisticsManager statisticsManager = new ConversionStatisticsManager(
                dataTable.getDataTableColumns());

        // iterate through the rest of the CSVReader file.
        int numberOfLines = 0;
        while (true) {
            String[] line = reader.readNext();
            if (line == null) {
                // what to do? are we done?
                logger.debug("line was null after processing " + numberOfLines);
                break;
            }
            numberOfLines++;
            // 1-based count for PreparedStatement's weirdness.
            int count = 1;
            Map<DataTableColumn, String> columnToValueMap = new HashMap<DataTableColumn, String>();
            if (line.length > headerLine.length)
                throw new TdarRecoverableRuntimeException("row "+ numberOfLines + " has more columns "+ line.length +" than the header column");

            for (int i = 0; i < line.length; i++) {
                if (count <= headerLine.length) {
                    columnToValueMap.put(
                                dataTable.getDataTableColumns().get(i), line[i]);
                    statisticsManager.updateStatistics(dataTable
                                .getDataTableColumns().get(i), line[i]);
                } else {
                    logger.warn("Discarding degenerate data value at index "
                            + count + " : " + line[i]);
                }
            }
            targetDatabase.addTableRow(dataTable, columnToValueMap);

        }
        completePreparedStatements();
        alterTableColumnTypes(dataTable, statisticsManager.getStatistics());

    }

}
