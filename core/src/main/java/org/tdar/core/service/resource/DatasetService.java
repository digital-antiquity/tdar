package org.tdar.core.service.resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.resource.dataset.ResultMetadataWrapper;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.FileAnalyzer;

import com.opensymphony.xwork2.TextProvider;

public interface DatasetService {

    /*
     * Convenience method for untranslate, then translate all columns in the dataset
     */
    void retranslate(Dataset dataset);

    InformationResourceFile createTranslatedFile(Dataset dataset);

    /**
     * Re-uploads the latest version of the data file for the given dataset.
     * FIXME: once message queue + message queue processor is in place we shouldn't need the noRollbackFor anymore
     * 
     * @param dataset
     */
    void reprocess(Dataset dataset);

    /*
     * Checks whether a @link Dataset can be mapped to an @link Ontology and thus, whether specific CodingValues can be mapped to that Ontology
     */
    boolean canLinkDataToOntology(Dataset dataset);

    /*
     * Log the DataTableColumn Information to XML to be stored in the ResourceRevisionLog
     */
    void logDataTableColumns(DataTable dataTable, String message, TdarUser authenticatedUser, Long start);

    /*
     * Takes a Coding Table within a larger data set and converts it to a tDAR CodingSheet
     */
    CodingSheet convertTableToCodingSheet(TdarUser user, TextProvider provider, DataTableColumn keyColumn,
            DataTableColumn valueColumn,
            DataTableColumn descriptionColumn);

    /*
     * Find all Rows within a @link DataTable with Pagination. Used to browse a Data Table
     */
    ResultMetadataWrapper selectAllFromDataTable(Dataset dataset, DataTable dataTable, int start, int page, boolean includeGenerated,
            boolean returnRowId,TdarUser authenticatedUser);

    /*
     * Extracts a specific Row of data from a tdardata database and returns a map object with it's contents pre-mapped to @link DataTableColumn entries
     */
    Map<DataTableColumn, String> selectRowFromDataTable(Dataset dataset, DataTable dataTable, Long rowId, boolean returnRowId,TdarUser authenticatedUser);

    /*
     * Finds a set of Database rows from the TdarMetadata database that are associated with the String specified, and wraps them in a @link
     * ResultsMetadataWrapper
     */
    ResultMetadataWrapper findRowsFromDataTable(Dataset dataset, DataTable dataTable, int start, int page, boolean includeGenerated, String query,TdarUser authenticatedUser);

    /*
     * Extracts out all @link DataTableRelationship entries for a @link DataTableColumn.
     */
//    List<DataTableRelationship> listRelationshipsForColumns(DataTableColumn column);

    /*
     * Based on a set of @link DataTableColumn entries, and a @link Project we can will clear out the existing mappings; and then identify mappings that need to
     * be made.
     */
    List<DataTableColumn> prepareAndFindMappings(Project project, Collection<DataTableColumn> columns);

    /*
     * Finds all Dataset Ids
     */
    List<Long> findAllIds();

    /*
     * A special feature of a @link Dataset is if it's associated with a @link Project, we can use data from a @link DataTable to associate additional data with
     * other resources in the project, e.g. a database of images. The mapping here is created using a field in the column that contains the filename of the file
     * to be mapped, and is associated with the filename associated with @InformationResourceFileVersion of any @link Resource in that @link Project.
     */
    void remapColumns(List<DataTableColumn> columns, Project project);

    void remapColumnsWithoutIndexing(List<DataTableColumn> columns, Project project);

    /*
     * Takes an existing @link Dataset and @link DataTable, and an incoming list of @link DataTableColumn entries, from the edit-column-metadata function in
     * tDAR, iterate through each incoming DataTableColumn and update the real entries in the database. Once updated, re-translate, map, and other changes as
     * necessary.
     */
    Boolean updateColumnMetadata(TextProvider provider, Dataset dataset, DataTable dataTable,
            List<DataTableColumn> dataTableColumns, TdarUser authenticatedUser, Long startTime);

    /*
     * Takes an existing @link Dataset and @link DataTable, and an incoming list of @link DataTableColumn entries, from the edit-column-metadata function in
     * tDAR, iterate through each incoming DataTableColumn and update the real entries in the database. Just handle resource-column-row mappings
     */
    List<DataTableColumn> updateColumnResourceMappingMetadata(TextProvider provider, Dataset dataset, DataTable dataTable,
            List<DataTableColumn> dataTableColumns, TdarUser authenticatedUser, Long start);

    /*
     * Exposes the @link DataTable as xml using the postgres xml format.
     * 
     * http://www.postgresql.org/docs/9.1/static/functions-xml.html
     */
    String selectTableAsXml(DataTable dataTable);

    /*
     * Setter for the tdardata postgres database which is not managed by hibernate
     */
    void setTdarDataImportDatabase(PostgresDatabase tdarDataImportDatabase);

    void remapAllColumnsAsync(Long datasetId, Long projectId);

    void remapAllColumns(Long datasetId, Long projectId);

    /**
     * We autowire the setter to help with autowiring issues
     * 
     * @param analyzer
     *            the analyzer to set
     */
    void setAnalyzer(FileAnalyzer analyzer);

    FileAnalyzer getAnalyzer();

    List<Dataset> findAll();

    Long count();

    List<Dataset> findAll(String string);

    Dataset find(Long id);

    Set<DataTableColumn> findSearchableColumns(Dataset ds, TdarUser user);

    List<String> findAutocompleteValues(Dataset dataset, DataTableColumn column, String value, TdarUser authenticatedUser);

}