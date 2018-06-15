package org.tdar.db.conversion.converters;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.db.model.abstracts.TargetDatabase;
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

    void setTargetDatabase(TargetDatabase targetDatabase);

    /**
     * Imports the given sourceDatabase into the target database.
     * 
     * @param sourceFile
     * @param targetDatabase
     * @return
     */
    Set<DataTable> execute();

    String getInternalTableName(String originalTableName);

    Set<DataTable> getDataTables();

    Set<DataTableRelationship> getKeys();

    DataTable getDataTableByName(String name);

    DataTable getDataTableByOriginalName(String name);

    Set<DataTableRelationship> getRelationships();

    void setRelationships(Set<DataTableRelationship> relationships);

    void setInformationResourceFileVersion(FileStoreFileProxy version);

    void setFilename(String filename);

    String getFilename();

    List<DataTableRelationship> getRelationshipsWithTable(String tableName);

}