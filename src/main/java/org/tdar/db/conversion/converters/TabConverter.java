package org.tdar.db.conversion.converters;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.db.model.abstracts.TargetDatabase;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Converts text TAB file into a postgres database... uses the fact that the OpenCSV Reader can read tab delimited files
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class TabConverter extends SimpleConverter {

    private static final String DB_PREFIX = "tab";

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
    protected void openInputDatabase()
            throws IOException {
        logger.info("TAB CONVERTER");
        if (informationResourceFileVersion == null) {
            logger.warn("Received null information resource file.");
            return;
        }
        File csvFile = informationResourceFileVersion.getTransientFile();
        if (csvFile == null) {
            logger.error("InformationResourceFile's file was null, this should never happen.");
            return;
        }
        setReader(new CSVReader(new FileReader(csvFile), '\t'));
        // grab first line as header.
        setTableName(FilenameUtils.getBaseName(csvFile.getName()));
        setHeaderLine(getReader().readNext());
        setIrFileId(informationResourceFileVersion.getId());
    }
}
