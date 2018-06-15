package org.tdar.core.service.resource;

import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.datatable.ImportColumn;
import org.tdar.datatable.ImportTable;
import org.tdar.datatable.TDataTable;
import org.tdar.datatable.TDataTableColumn;

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

}
