package org.tdar.core.service.resource;

import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.DataTableColumnRelationship;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.datatable.ImportColumn;
import org.tdar.datatable.ImportTable;
import org.tdar.datatable.TDataTable;
import org.tdar.datatable.TDataTableColumn;
import org.tdar.datatable.TDataTableColumnRelationship;
import org.tdar.datatable.TDataTableRelationship;

public class DatasetImportUtils {


    public static void copyColumnsFromIncomingTDataTable(TDataTable incomingtable, DataTable tableToPersist) {
        for (TDataTableColumn incomingColumn : incomingtable.getDataTableColumns()) {
            createDataTableColumn(incomingColumn, tableToPersist);
        }
        
    }

    public static DataTableColumn createDataTableColumn(ImportColumn incomingColumn, DataTable tableToPersist) {
        DataTableColumn col = new DataTableColumn();
        col.setName(incomingColumn.getName());
        col.setDisplayName(incomingColumn.getDisplayName());
        col.setDescription(incomingColumn.getDescription());
        col.setColumnDataType(incomingColumn.getColumnDataType());
        col.setImportOrder(incomingColumn.getImportOrder());
        col.setDataTable(tableToPersist);
        col.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        return col;
    }

    public static void copyValuesFromIncomingTDataTable(ImportTable incomingtable, DataTable tableToPersist) {
        tableToPersist.setDescription(incomingtable.getDescription());
        tableToPersist.setDisplayName(incomingtable.getDisplayName());
        tableToPersist.setName(incomingtable.getName());
        tableToPersist.setImportOrder(incomingtable.getImportOrder());
    }

    public static DataTableRelationship convertToRelationship(Dataset dataset, TDataTableRelationship rel_) {
        DataTableRelationship rel = new DataTableRelationship();
        rel.setType(rel_.getType());
        DataTable foreignTable =  dataset.getDataTableByName(rel_.getForeignTable().getName());
        DataTable localTable =  dataset.getDataTableByName(rel_.getLocalTable().getName());
        for (TDataTableColumnRelationship colRel_ : rel_.getColumnRelationships()) {
            DataTableColumnRelationship colRel = new DataTableColumnRelationship();
            colRel.setForeignColumn(foreignTable.getColumnByName(colRel_.getForeignColumn().getName()));
            colRel.setLocalColumn(foreignTable.getColumnByName(colRel_.getLocalColumn().getName()));
            rel.getColumnRelationships().add(colRel);
        }
        
        return rel;
    }

}
