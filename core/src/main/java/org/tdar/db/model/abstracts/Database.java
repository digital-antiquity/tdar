package org.tdar.db.model.abstracts;

import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;

/**
 * Marker interface for all database types.
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
public interface Database
{
    enum DatabaseType {
        ACCESS, DB2, EXCEL, POSTGRES
    };

    DatabaseType getDatabaseType();
    public static final String NO_CODING_SHEET_VALUE = "No coding sheet value for code:";

    int getMaxTableLength();

    /**
     * Attempt to change the datatype of the specified column in the specified table
     * 
     * @param tableName
     *            table name
     * @param columnName
     *            column name
     * @param jdbcType
     *            type id as defined in {@link java.sql.Types}
     */
    // TODO: add throws TypeConversionException?
    void alterTableColumnType(String tableName, DataTableColumn column, DataTableColumnType type);

    /**
     * Attempt to change the datatype of the specified column in the specified table
     * 
     * @param tableName
     *            table name
     * @param column
     *            DataTableColumn
     * @param length
     *            length attribute of data type
     * @param jdbcType
     *            type id as defined in {@link java.sql.Types}
     */
    // TODO: add throws TypeConversionException?
    void alterTableColumnType(String tableName, DataTableColumn column, DataTableColumnType type, int length);

    void translateInPlace(final DataTableColumn column, final CodingSheet codingSheet);

    void untranslate(DataTableColumn column);

}
