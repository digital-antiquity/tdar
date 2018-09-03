package org.tdar.core.service.resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.HasTables;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.DataTableColumnRelationship;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.db.datatable.ImportColumn;
import org.tdar.db.datatable.ImportTable;
import org.tdar.db.datatable.TDataTable;
import org.tdar.db.datatable.TDataTableColumn;
import org.tdar.db.datatable.TDataTableColumnRelationship;
import org.tdar.db.datatable.TDataTableRelationship;

public class DatasetImportUtils {

    public static final Logger logger = LoggerFactory.getLogger(DatasetImportUtils.class);


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
        col.setValues(incomingColumn.getValues());
        col.setIntValues(incomingColumn.getIntValues());
        col.setFloatValues(incomingColumn.getFloatValues());
        col.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        tableToPersist.getDataTableColumns().add(col);
        return col;
    }

    public static void copyValuesFromIncomingTDataTable(ImportTable incomingtable, DataTable tableToPersist) {
        tableToPersist.setDescription(incomingtable.getDescription());
        tableToPersist.setDisplayName(incomingtable.getDisplayName());
        tableToPersist.setName(incomingtable.getName());
        tableToPersist.setImportOrder(incomingtable.getImportOrder());
    }

    public static DataTableRelationship convertToRelationship(HasTables dataset, TDataTableRelationship rel_) {
        DataTableRelationship rel = new DataTableRelationship();
        rel.setType(rel_.getType());
        String foreignTableName = rel_.getForeignTable().getName();
        DataTable foreignTable =  getDataTableByName(dataset, foreignTableName);
        String localTableName = rel_.getLocalTable().getName();
        DataTable localTable =  getDataTableByName(dataset, localTableName);
        for (TDataTableColumnRelationship colRel_ : rel_.getColumnRelationships()) {
            String foreignName = colRel_.getForeignColumn().getName();
            String localName = colRel_.getLocalColumn().getName();

            DataTableColumnRelationship colRel = new DataTableColumnRelationship();
            colRel.setForeignColumn(foreignTable.getColumnByName(foreignName));
            colRel.setLocalColumn(localTable.getColumnByName(localName));
            if (colRel.getForeignColumn() == null) {
                if (localTable.getColumnByName(foreignName) != null && null != foreignTable.getColumnByName(localName)) {
                    logger.warn("reversed??? {}({}), {} || {}({}), {}", foreignTableName, foreignName, foreignTable.getColumnNames(), localTableName, localName, localTable.getColumnNames());
                    colRel.setForeignColumn(localTable.getColumnByName(foreignName));
                    colRel.setLocalColumn(foreignTable.getColumnByName(localName));
                } else {
                    logger.error("foreign column not found:{}({}), {}", foreignTableName, foreignName, foreignTable.getColumnNames());
                }
            }
            if (colRel.getLocalColumn() == null) {
                logger.error("local column not found:{}({}), {}", localTableName, localName, localTable.getColumnNames());
            }
            rel.getColumnRelationships().add(colRel);
        }
        
        return rel;
    }

    private static DataTable getDataTableByName(HasTables dataset, String localTableName) {
        for (DataTable dt : dataset.getDataTables()) {
            if (StringUtils.equals(dt.getName(), localTableName)) {
                return dt;
            }
        }
        return null;
    }

}
