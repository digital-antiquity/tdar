package org.tdar.db.datatable;

public interface ImportColumn  {

    String getName();

    void setName(String name);
    
    DataTableColumnType getColumnDataType();

    String getDescription();

    String getDisplayName();

    void setDisplayName(String displayName);
    
    Integer getLength();

    Integer getImportOrder();

    Integer getSequenceNumber();

    int compareToBySequenceNumber(ImportColumn b);

}