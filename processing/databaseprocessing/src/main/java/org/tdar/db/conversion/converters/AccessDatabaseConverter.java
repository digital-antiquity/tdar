package org.tdar.db.conversion.converters;

import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.db.ImportDatabase;
import org.tdar.db.datatable.DataTableColumnRelationshipType;
import org.tdar.db.datatable.DataTableColumnType;
import org.tdar.db.datatable.TDataTable;
import org.tdar.db.datatable.TDataTableColumn;
import org.tdar.db.datatable.TDataTableColumnRelationship;
import org.tdar.db.datatable.TDataTableRelationship;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.FileStoreFileProxy;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.PropertyMap;
import com.healthmarketscience.jackcess.Relationship;
import com.healthmarketscience.jackcess.Table;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBReader;

/**
 * The class reads an access db file, and converts it into other types of db
 * files.
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 * @latest $Date$
 */
public class AccessDatabaseConverter extends AbstractDatabaseConverter {
    private static final String DB_PREFIX = "d";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String getDatabasePrefix() {
        return DB_PREFIX;
    }

    public AccessDatabaseConverter() {
    }

    public AccessDatabaseConverter(ImportDatabase targetDatabase, FileStoreFileProxy... versions) {
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(versions[0]);
    }

    @Override
    protected void openInputDatabase() throws IOException {
        File databaseFile = getInformationResourceFileVersion().getTransientFile();
        // if we use ReadOnly Mode here we have the ability to open older files... http://jira.pentaho.com/browse/PDI-5111
        DatabaseBuilder builder = new DatabaseBuilder();
        builder.setReadOnly(true);
        builder.setFile(databaseFile);
        setDatabase(builder.open());
        this.setIrFileId(getInformationResourceFileVersion().getId());
        this.setFilename(databaseFile.getName());
    }

    /**
     * Dumps the access database wrapped by this converter into the target
     * database (in our current case, PostgresDatabase).
     *
     */
    @Override
    public void dumpData() throws Exception {
        // start dumping ...
        Map<String, TDataTable> dataTableNameMap = new HashMap<>();
        Iterator<Table> iterator = getDatabase().newIterable().setIncludeLinkedTables(false).iterator();
        Set<String> notLinked = new HashSet<>();
        Set<String> linked = new HashSet<>();

        while (iterator.hasNext()) {
            Table table = iterator.next();
            notLinked.add(table.getName());
        }

        int tableOrder = 0;
        for (String tableName : getDatabase().getTableNames()) {

            if (!notLinked.contains(tableName)) {
                logger.warn("LinkedTable: {}", tableName);
                linked.add(tableName);
                continue;
            }
            // generate and sanitize new table name
            TDataTable dataTable = createDataTable(tableName, tableOrder);
            tableOrder++;
            if (dataTableNameMap.containsKey(tableName)) {
                logger.error("duplicate table name: {} -- {} / {}", tableName, dataTableNameMap.get(tableName), dataTable);
            }
            dataTableNameMap.put(tableName, dataTable);
            // drop the table if it has been there
            targetDatabase.dropTable(dataTable);

            Table currentTable = getDatabase().getTable(tableName);

            List<? extends Column> columnList = currentTable.getColumns();
            int count = 0;
            for (Column currentColumn : columnList) {
                count = createColumn(dataTable, count, currentColumn);
            }

            targetDatabase.createTable(dataTable);
            int rowNumber = 0;
            try {
                rowNumber = loadRow(tableName, dataTable, currentTable);
            } catch (BufferUnderflowException | IllegalStateException bex) {
                throw new TdarRecoverableRuntimeException("accessDatabaseConverter.error_corrupt");
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException("accessDatabaseConverter.cannot_read_Row", e, Arrays.asList(rowNumber, tableName));
            } finally {
                completePreparedStatements();
            }
        }

        if (CollectionUtils.isNotEmpty(linked)) {
            getMessages().add(String.format("Database had the following linked tables that were NOT imported: %s", linked));
        }

        setRelationships(extractRelationships(dataTableNameMap, linked));
    }

    private int loadRow(String tableName, TDataTable dataTable, Table currentTable) throws IOException, Exception {
        int rowNumber;
        int rowCount = getDatabase().getTable(tableName).getRowCount();
        for (rowNumber = 0; rowNumber < rowCount; rowNumber++) {
            HashMap<TDataTableColumn, String> valueColumnMap = new HashMap<>();
            Map<String, Object> currentRow = currentTable.getNextRow();
            int j = 0;
            if (currentRow == null) {
                continue;
            }
            for (Object currentObject : currentRow.values()) {
                TDataTableColumn currentColumn = dataTable.getDataTableColumns().get(j);
                if (currentObject == null) {
                    j++;
                    continue;
                }
                String currentObjectAsString = currentObject.toString();
                if (currentColumn.getColumnDataType() == DataTableColumnType.BLOB) {

                    // logger.info(currentObject.getClass().getCanonicalName());
                    byte[] data = (byte[]) currentObject;
                    // InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(data));
                    // byte[] uncompressed = IOUtils.toByteArray(iis);
                    // logger.info("{}", Hex.encodeHexString(data));
                    // logger.info("{}", uncompressed);
                    // DATA here is paired with the data in the GDBGeomColumns table to describe the feature type, etc
                    GeometryFactory factory = new GeometryFactory();
                    // factory.
                    // WKBReader reader = new WKBReader(factory);

                    // http://sourceforge.net/mailarchive/message.php?msg_id=30646557
                    // http://sourceforge.net/mailarchive/message.php?msg_id=29982387
                    // https://github.com/geotools/geotools/blob/master/modules/unsupported/ogr/ogr-jni/pom.xml
                    // http://www.giser.net/wp-content/uploads/2011/01/extended-shapefile-format.pdf
                    // this does not work, see ogrpgeogeometry.cpp in ( extended_shapefile_format.pdf)
                    // and http://stackoverflow.com/questions/11483189/transact-sql-function-for-convert-from-esri-personal-geodatabase-shape-column-to
                    @SuppressWarnings("unused")
                    Geometry g = null;
                    try {
                        String encoded = new String(Hex.encodeHex(data));
                        g = new WKBReader(factory).read(encoded.getBytes());
                    } catch (Exception e) {
                        // logger.error("{}", e);
                    }
                    // logger.info("data: {} ", data);
                }
                valueColumnMap.put(currentColumn, currentObjectAsString);
                j++;
            }
            targetDatabase.addTableRow(dataTable, valueColumnMap);
        }
        return rowNumber;
    }

    private int createColumn(TDataTable dataTable, int count, Column currentColumn) throws IOException {
        DataTableColumnType dataType = DataTableColumnType.VARCHAR;
        logger.info("INCOMING COLUMN: '{}'  ({})", currentColumn.getName(), currentColumn.getType());
        // NOTE: switch passthrough is intentional here (e.g. big, long, int types should all convert to BIGINT)
        switch (currentColumn.getType()) {
            case BOOLEAN:
                dataType = DataTableColumnType.BOOLEAN;
                break;
            case DOUBLE:
            case NUMERIC:
            case FLOAT:
            case MONEY:
                dataType = DataTableColumnType.DOUBLE;
                break;
            case BYTE:
            case LONG:
            case INT:
                dataType = DataTableColumnType.BIGINT;
                break;
            case TEXT:
            case MEMO:
            case GUID:
                dataType = DataTableColumnType.TEXT;
                break;
            case SHORT_DATE_TIME:
                dataType = DataTableColumnType.DATETIME;
                break;
            case BINARY:
            case UNKNOWN_11:
            case UNKNOWN_0D:
            case OLE:
                dataType = DataTableColumnType.BLOB;
                break;
            default:
                dataType = DataTableColumnType.VARCHAR;
        }

        TDataTableColumn dataTableColumn = createDataTableColumn(currentColumn.getName(), dataType, dataTable, count);
        count++;
        currentColumn.getProperties();

        Object description_ = currentColumn.getProperties().getValue(PropertyMap.DESCRIPTION_PROP);
        if ((description_ != null) && !StringUtils.isEmpty(description_.toString())) {
            dataTableColumn.setDescription(description_.toString());
        }
        if (dataType == DataTableColumnType.VARCHAR) {
            dataTableColumn.setLength(Short.valueOf(currentColumn.getLengthInUnits()).intValue());
            logger.trace("currentColumn:{}\t length:{}\t length in units:{}", new Object[] { currentColumn, currentColumn.getLength(),
                    currentColumn.getLengthInUnits() });
        }
        logger.info("  \t create column {} {} ({}) -- {}", dataTableColumn.getName(), dataTableColumn.getColumnDataType(), dataTableColumn.getLength(),
                dataTableColumn.getDescription());
        return count;
    }

    private Set<TDataTableRelationship> extractRelationships(Map<String, TDataTable> dataTableNameMap, Set<String> linked) throws IOException {
        Set<TDataTableRelationship> relationships = new HashSet<>();
        for (String fromTableName : getDatabase().getTableNames()) {
            for (String toTableName : getDatabase().getTableNames()) {
                if (fromTableName.equals(toTableName)) {
                    continue;
                }

                if (linked.contains(fromTableName) || linked.contains(toTableName)) {
                    continue;
                }

                for (Relationship relationship : getDatabase().getRelationships(getDatabase().getTable(fromTableName), getDatabase().getTable(toTableName))) {
                    if (!fromTableName.equals(relationship.getFromTable().getName())) {
                        continue;
                    }
                    logger.trace(relationship.getName());
                    TDataTableRelationship relationshipToPersist = new TDataTableRelationship();
                    // iterate over the two lists of columns (from- and to-) and pair them up
                    Iterator<Column> fromColumns = relationship.getFromColumns().iterator();
                    Iterator<Column> toColumns = relationship.getToColumns().iterator();
                    while (fromColumns.hasNext() && toColumns.hasNext()) {
                        Column fromColumn = fromColumns.next();
                        Column toColumn = toColumns.next();
                        TDataTable fromTable = dataTableNameMap.get(fromTableName);
                        TDataTableColumn fromDataTableColumn = fromTable.getColumnByDisplayName(fromColumn.getName());
                        TDataTable toTable = dataTableNameMap.get(toTableName);
                        TDataTableColumn toDataTableColumn = toTable.getColumnByDisplayName(toColumn.getName());
                        TDataTableColumnRelationship columnRelationship = new TDataTableColumnRelationship();
                        columnRelationship.setLocalColumn(fromDataTableColumn);
                        columnRelationship.setForeignColumn(toDataTableColumn);
                        columnRelationship.setForeignTable(fromTable);
                        columnRelationship.setLocalTable(toTable);
                        relationshipToPersist.getColumnRelationships().add(columnRelationship);
                    }

                    // determine the type of relationship: one-to-one, one-to-many, or many-to-one
                    if (relationship.isOneToOne()) {
                        relationshipToPersist.setType(DataTableColumnRelationshipType.ONE_TO_ONE);
                    } else {
                        // The relationship is a one-to-many or many-to-one, but which?
                        // The "one" side of the relationship is the side whose key columns are a superset of the columns of a unique index on that table.
                        List<Column> possiblyUniqueKeyColumns = relationship.getFromColumns();
                        if (isUniqueKey(possiblyUniqueKeyColumns)) {
                            relationshipToPersist.setType(DataTableColumnRelationshipType.ONE_TO_MANY);
                        } else {
                            relationshipToPersist.setType(DataTableColumnRelationshipType.MANY_TO_ONE);
                        }
                    }

                    logger.trace(relationship.isLeftOuterJoin() + " left outer join");
                    logger.trace(relationship.isRightOuterJoin() + " right outer join");
                    logger.trace(relationship.isOneToOne() + " one to one");
                    logger.trace(relationship.cascadeDeletes() + " cascade deletes");
                    logger.trace(relationship.cascadeUpdates() + " cascade updates");
                    // logger.trace(relationship.getFlags() + " :flags");
                    logger.trace("++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    logger.info("{}", relationshipToPersist);
                    relationships.add(relationshipToPersist);
                }
            }
        }
        return relationships;
    }

    /**
     * Determine whether the set of columns in this Access database table would consistute a unique key,
     * by looking for corresponding unique key indexes in the table.
     * 
     * @param possiblyUniqueKeyColumns
     * @return
     */
    private boolean isUniqueKey(List<Column> possiblyUniqueKeyColumns) {
        // an empty list of columns is bogus
        if (possiblyUniqueKeyColumns.isEmpty()) {
            return false;
        }

        // search through the table's indexes...
        for (Index index : possiblyUniqueKeyColumns.get(0).getTable().getIndexes()) {
            // if the index is unique then it may provide proof that the relationship's key is also unique
            if (index.isUnique()) {
                // assemble a list of the columns
                List<Column> uniqueKeyColumns = new ArrayList<Column>();
                for (com.healthmarketscience.jackcess.Index.Column descriptor : index.getColumns()) {
                    uniqueKeyColumns.add(descriptor.getColumn());
                }
                // check if the relationship's columns include all the unique key's columns
                if (possiblyUniqueKeyColumns.containsAll(uniqueKeyColumns)) {
                    return true;
                }
            }
        }

        // our set of columns did not match any unique indexes
        return false;
    }
}
