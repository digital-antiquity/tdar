package org.tdar.db.conversion.converters;

import org.tdar.db.ImportDatabase;
import org.tdar.filestore.FileStoreFile;

/**
 * Converts text CSV files into a postgres database.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class CsvConverter extends SimpleConverter {

    protected static final String DB_PREFIX = "csv";

    @Override
    public String getDatabasePrefix() {
        return DB_PREFIX;
    }

    public CsvConverter() {
    };

    public CsvConverter(ImportDatabase targetDatabase, FileStoreFile... versions) {
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(versions[0]);
    }

}
