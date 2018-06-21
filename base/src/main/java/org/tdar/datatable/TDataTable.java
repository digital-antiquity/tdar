package org.tdar.datatable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;

/**
 * $Id$
 * <p>
 * A DataTable belonging to a Dataset and carrying a list of ordered TDataTableColumns and descriptive metadata.
 * </p>
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */

public class TDataTable implements Serializable, ImportTable {

    private static final long serialVersionUID = -3007267836652683035L;

    private String name;
    private String displayName;
    private String description;
    private List<TDataTableColumn> dataTableColumns = new ArrayList<>();
    private Integer importOrder;

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportTable#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportTable#getDataTableColumns()
     */
    @Override
    public List<TDataTableColumn> getDataTableColumns() {
        return dataTableColumns;
    }

    public void setDataTableColumns(List<TDataTableColumn> dataTableColumns) {
        this.dataTableColumns = dataTableColumns;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        return builder.toString();
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportTable#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportTable#getImportOrder()
     */
    @Override
    public Integer getImportOrder() {
        return importOrder;
    }

    public void setImportOrder(Integer importOrder) {
        this.importOrder = importOrder;
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportTable#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TDataTableColumn getColumnByName(String name) {
        for (TDataTableColumn col : getDataTableColumns()) {
            if (StringUtils.equals(name, col.getName())) {
                return col;
            }
        }
        return null;
    }

    public TDataTableColumn getColumnByDisplayName(String name) {
        for (TDataTableColumn col : getDataTableColumns()) {
            if (StringUtils.equals(name, col.getDisplayName())) {
                return col;
            }
        }
        return null;
    }
}
