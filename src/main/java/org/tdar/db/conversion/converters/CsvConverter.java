package org.tdar.db.conversion.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.db.model.abstracts.TargetDatabase;

/**
 * Converts text CSV files into a postgres database.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class CsvConverter extends SimpleConverter {

    protected static final String DB_PREFIX = "csv";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String getDatabasePrefix() {
        return DB_PREFIX;
    }

    public CsvConverter() {
    };

    public CsvConverter(TargetDatabase targetDatabase, InformationResourceFileVersion... versions) {
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(versions[0]);
    }

}
