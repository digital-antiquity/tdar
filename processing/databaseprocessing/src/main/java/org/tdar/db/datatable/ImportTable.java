package org.tdar.db.datatable;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

public interface ImportTable<C extends ImportColumn> {

    String getName();

    List<C> getDataTableColumns();

    String getDisplayName();

    Integer getImportOrder();

    public default String getInternalName() {
        return getName().replaceAll("^(([A-z]+)_)(\\d+_?)", "");
    }

    String getDescription();


    @Transient
    public default List<String> getColumnNames() {
        List<String> columns = new ArrayList<String>();
        for (ImportColumn column : getDataTableColumns()) {
            columns.add(column.getName());
        }
        return columns;
    }

    C getColumnByName(String string);

    C getColumnByDisplayName(String string);

    /**
     * Get the data table columns sorted in the ascending order of column names.
     * 
     * @return
     */
    @XmlTransient
    public default List<C> getSortedDataTableColumns() {
        return getSortedDataTableColumns(new Comparator<C>() {
            @Override
            public int compare(ImportColumn a, ImportColumn b) {
                int comparison = a.compareToBySequenceNumber(b);
                if (comparison == 0) {
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return comparison;
            }
        });
    }

    public default List<C> getSortedDataTableColumns(Comparator<C> comparator) {
        ArrayList<C> sortedDataTableColumns = new ArrayList<>(getDataTableColumns());
        Collections.sort(sortedDataTableColumns, comparator);
        return sortedDataTableColumns;
    }

}