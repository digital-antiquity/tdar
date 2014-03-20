package org.tdar.db.conversion.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.db.model.abstracts.TargetDatabase;

/**
 * Converts text TAB file into a postgres database... uses the fact that the OpenCSV Reader can read tab delimited files
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class TabConverter extends SimpleConverter {

    private static final char DELIMITER = '\t';
    private static final String DB_PREFIX = "tab";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String getDatabasePrefix() {
        return DB_PREFIX;
    }

    public TabConverter() {
    };

    public TabConverter(TargetDatabase targetDatabase, InformationResourceFileVersion... versions) {
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(versions[0]);
    }

    @Override
    public Character getDelimeterChar() {
        return DELIMITER;
    }

}
