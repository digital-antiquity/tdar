package org.tdar.core.service;

import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.odata.server.AbstractDataRecord;

public interface RowOperations {

    void editRow(DataTable dataTable, Long rowId, Map<?, ?> data);

    Set<AbstractDataRecord> findAllRows(DataTable dataTable);

    void deleteRow(DataTable dataTable, Long rowId);

}
