package org.tdar.db.conversion.converters;

import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnRelationshipType;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;
import org.tdar.core.bean.resource.dataTable.DataTableRelationship;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.model.abstracts.TargetDatabase;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.PropertyMap;
import com.healthmarketscience.jackcess.Relationship;
import com.healthmarketscience.jackcess.Table;

/**
 * The class reads an access db file, and converts it into other types of db
 * files.
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 * @latest $Date$
 */
public class AccessDatabaseConverter extends DatasetConverter.Base {
    private static final String DB_PREFIX = "d";
    private static final String ERROR_CORRUPT_DB = "tDAR was unable to read portions of this Access database. It is possible this issue may be resolved By using the \"Compact and Repair \" feature in Microsoft Access.";
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public String getDatabasePrefix() {
        return DB_PREFIX;
    }

    public AccessDatabaseConverter() {}
    
    public AccessDatabaseConverter(InformationResourceFileVersion version, TargetDatabase targetDatabase) {
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(version);
    }

    protected void openInputDatabase()
            throws IOException {
        File databaseFile = getInformationResourceFileVersion().getFile();
        setDatabase(Database.open(databaseFile));
        this.setIrFileId(getInformationResourceFileVersion().getId());
        this.setFilename(databaseFile.getName());
    }

    /**
     * Dumps the access database wrapped by this converter into the target
     * database (in our current case, PostgresDatabase).
     * 
     * @param targetDatabase
     */
    public void dumpData() throws Exception {
        // start dumping ...
        Map<String, DataTable> dataTableNameMap = new HashMap<String, DataTable>();
        for (String tableName : getDatabase().getTableNames()) {
            // generate and sanitize new table name
            DataTable dataTable = createDataTable(tableName);
            dataTableNameMap.put(tableName, dataTable);
            // drop the table if it has been there
            targetDatabase.dropTable(dataTable);

            Table currentTable = getDatabase().getTable(tableName);
            List<Column> columnList = currentTable.getColumns();
            for (Column currentColumn : columnList) {
                DataTableColumnType dataType = DataTableColumnType.VARCHAR;
                logger.info("Incoming column \t name:{}  type:{}", currentColumn.getName(), currentColumn.getType());
                // NOTE: switch passthrough is intentional here (e.g. big, long, int types should all convert to BIGINT)
                switch (currentColumn.getType()) {
                    case BOOLEAN:
                        dataType = DataTableColumnType.BOOLEAN;
                        break;
                    case DOUBLE:
                    case NUMERIC:
                    case FLOAT:
                        dataType = DataTableColumnType.DOUBLE;
                        break;
                    case BYTE:
                    case LONG:
                    case INT:
                        dataType = DataTableColumnType.BIGINT;
                        break;
                    case TEXT:
                    case BINARY:
                    case MEMO:
                        dataType = DataTableColumnType.TEXT;
                        break;
                    case MONEY:
                    case GUID:
                    case OLE:
                    case SHORT_DATE_TIME:
                        dataType = DataTableColumnType.DATETIME;
                        break;
                    case UNKNOWN_0D:
                    case UNKNOWN_11:
                    default:
                        dataType = DataTableColumnType.VARCHAR;
                }

                DataTableColumn dataTableColumn = createDataTableColumn(currentColumn.getName(), dataType, dataTable);
                currentColumn.getProperties();

                Object description_ = currentColumn.getProperties().getValue(PropertyMap.DESCRIPTION_PROP);
                if (description_ != null && !StringUtils.isEmpty(description_.toString())) {
                    dataTableColumn.setDescription(description_.toString());
                }
                logger.info("Converted column\t obj:{}\t description:{}\t length:{}", new Object[] { dataTableColumn, dataTableColumn.getDescription(),
                        dataTableColumn.getLength() });
                if (dataType == DataTableColumnType.VARCHAR) {
                    dataTableColumn.setLength(Short.valueOf(currentColumn.getLengthInUnits()).intValue());
                    logger.trace("currentColumn:{}\t length:{}\t length in units:{}", new Object[] { currentColumn, currentColumn.getLength(),
                            currentColumn.getLengthInUnits() });
                }
            }

            targetDatabase.createTable(dataTable);

            try {
                int rowCount = getDatabase().getTable(tableName).getRowCount();
                for (int i = 0; i < rowCount; ++i) {
                    HashMap<DataTableColumn, String> valueColumnMap = new HashMap<DataTableColumn, String>();
                    Map<String, Object> currentRow = currentTable.getNextRow();
                    int j = 0;
                    if (currentRow == null)
                        continue;
                    for (Object currentObject : currentRow.values()) {
                        if (currentObject == null) {
                            ++j;
                            continue;
                        }
                        String currentObjectAsString = currentObject.toString();
                        valueColumnMap.put(dataTable.getDataTableColumns().get(j), currentObjectAsString);
                        ++j;
                    }
                    targetDatabase.addTableRow(dataTable, valueColumnMap);
                }
            } catch(BufferUnderflowException bex) {
                throw new TdarRecoverableRuntimeException(ERROR_CORRUPT_DB);
            }
        }
        completePreparedStatements();

        Set<DataTableRelationship> relationships = new HashSet<DataTableRelationship>();
        for (String tableName1 : getDatabase().getTableNames()) {
            for (String tableName2 : getDatabase().getTableNames()) {
                if (tableName1.equals(tableName2))
                    continue;
                for (Relationship relationship : getDatabase().getRelationships(getDatabase().getTable(tableName1), getDatabase().getTable(tableName2))) {
                    if (!tableName1.equals(relationship.getFromTable().getName()))
                        continue;
                    logger.debug(relationship.getName());
                    DataTableRelationship relationshipToPersist = new DataTableRelationship();
                    relationshipToPersist.setLocalTable(dataTableNameMap.get(tableName1));
                    relationshipToPersist.setForeignTable(dataTableNameMap.get(tableName2));
                    Set<DataTableColumn> fromColumns = new HashSet<DataTableColumn>();
                    for (Column col : relationship.getFromColumns()) {
                        DataTableColumn fromCol = dataTableNameMap.get(tableName1).getColumnByDisplayName(col.getName());
                        fromColumns.add(fromCol);
                    }
                    Set<DataTableColumn> toColumns = new HashSet<DataTableColumn>();
                    for (Column col : relationship.getToColumns()) {
                        DataTableColumn toCol = dataTableNameMap.get(tableName2).getColumnByDisplayName(col.getName());
                        toColumns.add(toCol);
                    }
                    relationshipToPersist.setLocalColumns(fromColumns);
                    relationshipToPersist.setForeignColumns(toColumns);

                    logger.trace(relationship.isLeftOuterJoin() + " left outer join");
                    logger.trace(relationship.isRightOuterJoin() + " right outer join");
                    logger.trace(relationship.isOneToOne() + " one to one");
                    logger.trace(relationship.cascadeDeletes() + " cascade deletes");
                    logger.trace(relationship.cascadeUpdates() + " cascade updates");
                    logger.trace(relationship.getFlags() + " :flags");
                    logger.trace("++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    relationshipToPersist.setType(DataTableColumnRelationshipType.FOREIGN_KEY);
                    logger.info("{}", relationshipToPersist);
                    relationships.add(relationshipToPersist);
                }
                setRelationships(relationships);
            }
        }
    }
}
