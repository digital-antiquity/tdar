package org.tdar.core.bean.resource;

import java.util.Set;

import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;

public interface HasTables {

    public Set<DataTable> getDataTables();

    public Set<DataTableRelationship> getRelationships();
}
