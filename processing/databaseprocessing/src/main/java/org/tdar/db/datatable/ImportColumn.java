package org.tdar.db.datatable;

import java.util.Set;

public interface ImportColumn  {

    public static final String TDAR_ID_COLUMN = "id_row_tdar";

    String getName();

    void setName(String name);
    
    DataTableColumnType getColumnDataType();

    String getDescription();

    String getDisplayName();

    void setDisplayName(String displayName);
    
    Set<String> getValues();
    
    Integer getLength();

    Integer getImportOrder();

    Integer getSequenceNumber();

    int compareToBySequenceNumber(ImportColumn b);

    Set<Integer> getIntValues();

    Set<Double> getFloatValues();

}