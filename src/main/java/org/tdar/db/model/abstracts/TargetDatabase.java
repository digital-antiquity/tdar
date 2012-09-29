package org.tdar.db.model.abstracts;

import java.util.Collection;
import java.util.Map;

import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;

/**
 * A base class for target Databases that can be written to via a
 * DatabaseConverter.
 * 
 * FIXME (alllee): I don't think we need all this flexibility, and might be
 * better served just implementing a PostgresDatabase directly or rethinking the
 * design of these interfaces.
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
public interface TargetDatabase extends Database {

    /**
     * Returns a table name consistent with this target database's allowable
     * table names.
     */
    
    public static final String TDAR_ID_COLUMN = "id_row_tdar";

    public String normalizeTableOrColumnNames(String input);

    public void closePreparedStatements(Collection<DataTable> dataTables) throws Exception;

    public String getFullyQualifiedTableName(String tableName);

    public void dropTable(String tableName);

    public void dropTable(DataTable dataTable);

    public void createTable(DataTable dataTable) throws Exception;

    public void addTableRow(DataTable dataTable,
            Map<DataTableColumn, String> valueColumnMap) throws Exception;

    /**
     * @param dataType
     * @return
     */
    public String toImplementedTypeDeclaration(DataTableColumnType dataType,
            int precision);

}
