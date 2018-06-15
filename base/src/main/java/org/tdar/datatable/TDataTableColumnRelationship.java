package org.tdar.datatable;

import java.io.Serializable;

/**
 * Represents a relationship between two data-tables via columns
 * 
 * @author abrin
 * 
 */
public class TDataTableColumnRelationship implements Serializable {

    private static final long serialVersionUID = -5771507101151758202L;

    private TDataTableColumn localColumn;
    private TDataTableColumn foreignColumn;
    private TDataTable localTable;
    private TDataTable foreignTable;

    public ImportColumn getLocalColumn() {
        return localColumn;
    }
    public void setLocalColumn(TDataTableColumn localColumn) {
        this.localColumn = localColumn;
    }

    public ImportColumn getForeignColumn() {
        return foreignColumn;
    }

    public void setForeignColumn(TDataTableColumn foreignColumn) {
        this.foreignColumn = foreignColumn;
    }
    public TDataTable getForeignTable() {
        return foreignTable;
    }
    public void setForeignTable(TDataTable foreignTable) {
        this.foreignTable = foreignTable;
    }
    public TDataTable getLocalTable() {
        return localTable;
    }
    public void setLocalTable(TDataTable localTable) {
        this.localTable = localTable;
    }
}
