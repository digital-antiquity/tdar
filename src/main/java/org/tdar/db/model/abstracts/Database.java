/**
 * 
 */
package org.tdar.db.model.abstracts;

import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;



/**
 * Marker interface for all database types.    
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
public interface Database
{
	enum DatabaseType {ACCESS, DB2, EXCEL, POSTGRES};
	
	public static final String NULL_EMPTY_INTEGRATION_VALUE = "This integration value was not specified in the uploaded dataset";

//	enum DATA_TYPE 
//	{
//		TINYINT, SMALLINT, INT, BIGINT, // 1 ... 4
//		SMALLMONEY, MONEY, 				// 5, 6
//		SMALLDATETIME, DATATIME,		// 7, 8
//		FLOAT, DOUBLE, 
//		DECIMAL, NUMBERIC,
//		BINARY, VARBINARY, 
//		CHAR, NCHAR, NVARCHAR, VARCHAR,
//		BIT, TIMESTAMP, UNIQUEIDENTIFIER,
//		TEXT, NTEXT, IMAGE
//	};

	
	public DatabaseType getDatabaseType();

	/**
	 * Attempt to change the datatype of the specified column in the specified table
	 * @param tableName table name
	 * @param columnName column name
	 * @param jdbcType type id as defined in {@link java.sql.Types}
	 */
    public void alterTableColumnType(String tableName, DataTableColumn column, DataTableColumnType type); //TODO: add throws TypeConversionException?

    /**
     * Attempt to change the datatype of the specified column in the specified table
     * @param tableName table name
     * @param column DataTableColumn
     * @param length length attribute of data type
     * @param jdbcType type id as defined in {@link java.sql.Types}
     */
    public void alterTableColumnType(String tableName, DataTableColumn column, DataTableColumnType type, int length); //TODO: add throws TypeConversionException?

	public void translateInPlace(final DataTableColumn column, final CodingSheet codingSheet);

	public void untranslate(DataTableColumn column);
	
}
