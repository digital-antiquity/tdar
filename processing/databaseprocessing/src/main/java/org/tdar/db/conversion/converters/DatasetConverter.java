package org.tdar.db.conversion.converters;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.tdar.db.ImportDatabase;
import org.tdar.db.datatable.ImportTable;
import org.tdar.db.datatable.TDataTable;
import org.tdar.db.datatable.TDataTableRelationship;
import org.tdar.filestore.FileStoreFileProxy;

public interface DatasetConverter {

    /**
     * Get names of all tables for the converted data file.
     * 
     * @return
     */
    List<String> getTableNames();

    List<String> getMessages();

    void setIndexedContentsFile(File indexedContentsFile);

    File getIndexedContentsFile();

    void setTargetDatabase(ImportDatabase targetDatabase);

    /**
     * Imports the given sourceDatabase into the target database.
     * 
     * @param sourceFile
     * @param targetDatabase
     * @return
     */
    Set<TDataTable> execute();

    String getInternalTableName(String originalTableName);

    Set<TDataTable> getDataTables();

    Set<TDataTableRelationship> getKeys();

    ImportTable getDataTableByName(String name);

    ImportTable getDataTableByOriginalName(String name);

    Set<TDataTableRelationship> getRelationships();

    void setRelationships(Set<TDataTableRelationship> relationships);

    void setInformationResourceFileVersion(FileStoreFileProxy version);

    void setFilename(String filename);

    String getFilename();

    List<TDataTableRelationship> getRelationshipsWithTable(String tableName);

}