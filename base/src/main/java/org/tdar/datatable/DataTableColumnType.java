/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.datatable;

import java.sql.Types;

import org.tdar.locale.HasLabel;
import org.tdar.locale.Localizable;
import org.tdar.utils.MessageHelper;

/**
 * Enum to manage the type of column between tDAR internal types, and database and other types
 * 
 * @author Adam Brin
 * 
 */
public enum DataTableColumnType implements HasLabel, Localizable {

    // See: http://msdn.microsoft.com/en-us/library/bb896344.aspx for EdmSimpleTypes

    BOOLEAN(Types.BOOLEAN),
    VARCHAR(Types.VARCHAR),
    BIGINT(Types.BIGINT),
    DOUBLE(Types.DOUBLE),
    TEXT(Types.CLOB),
    DATE(Types.DATE),
    DATETIME(
            Types.TIMESTAMP),
    BLOB(Types.BLOB);

    private final int sqlType;

    private DataTableColumnType(int sqlType) {
        this.sqlType = sqlType;
    }

    /**
     * @return the sqlType
     */
    public int getSqlType() {
        return sqlType;
    }

    public static DataTableColumnType fromString(String typeToCheck) {
        if (typeToCheck.toLowerCase().contains("char")) {
            return VARCHAR;
        }
        if (typeToCheck.toLowerCase().contains("text")) {
            return TEXT;
        }
        if (typeToCheck.toLowerCase().contains("blob")) {
            return BLOB;
        }
        if (typeToCheck.toLowerCase().contains("double")
                || typeToCheck.contains("float")) {
            return DOUBLE;
        }
        if (typeToCheck.toLowerCase().contains("bool")) {
            return BOOLEAN;
        }
        if (typeToCheck.toLowerCase().contains("int")) {
            return BIGINT;
        }
        if (typeToCheck.toLowerCase().contains("time")) {
            return DATETIME;
        }
        if (typeToCheck.toLowerCase().equals("date")) {
            return DATE;
        }
        return TEXT;
    }

    public static DataTableColumnType fromJDBCType(int type) {
        switch (type) {
            case Types.VARCHAR:
                return VARCHAR;
            case Types.BIGINT:
                return BIGINT;
            case Types.DOUBLE:
                return DOUBLE;
            case Types.CLOB:
                return TEXT;
            case Types.BLOB:
                return BLOB;
            case Types.BOOLEAN:
                return BOOLEAN;
            case Types.DATE:
                return DATE;
            case Types.TIME:
            case Types.TIMESTAMP:
                return DATETIME;
        }
        return TEXT;

    }

    /**
     * @return
     */
    // public DataTableColumnEncodingType getDefaultEncodingType() {
    // switch (this) {
    // case VARCHAR:
    // return DataTableColumnEncodingType.TEXT;
    // case BIGINT:
    // return DataTableColumnEncodingType.NUMERIC;
    // case DOUBLE:
    // return DataTableColumnEncodingType.NUMERIC;
    // case TEXT:
    // return DataTableColumnEncodingType.TEXT;
    // case BOOLEAN:
    // return DataTableColumnEncodingType.TEXT;
    // case DATE:
    // return DataTableColumnEncodingType.TEXT;
    // case DATETIME:
    // return DataTableColumnEncodingType.TEXT;
    // }
    // return DataTableColumnEncodingType.TEXT;
    // }

    public static int[] getAllSQLTypes() {
        int types[] = { Types.ARRAY, Types.BIGINT, Types.BINARY, Types.BIT, Types.BLOB, Types.BOOLEAN, Types.CHAR, Types.CLOB, Types.DATALINK, Types.DATE,
                Types.DECIMAL, Types.DISTINCT, Types.DOUBLE, Types.FLOAT, Types.INTEGER, Types.JAVA_OBJECT, Types.LONGNVARCHAR, Types.LONGVARBINARY,
                Types.LONGVARCHAR, Types.NCHAR, Types.NCLOB, Types.NULL, Types.NVARCHAR, Types.OTHER, Types.REAL, Types.REF, Types.ROWID, Types.SMALLINT,
                Types.SQLXML, Types.STRUCT, Types.TIME, Types.TIMESTAMP, Types.TINYINT, Types.VARBINARY, Types.VARCHAR };
        return types;
    }

    /**
     * @return
     */
    public boolean isNumeric() {
        if ((this == DOUBLE) || (this == BIGINT)) {
            return true;
        }

        return false;
    }

    @Override
    public String getLabel() {
        return name();
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }
}
