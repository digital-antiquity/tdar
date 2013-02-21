package org.tdar.odata.server;

import java.util.List;

import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;

public interface RepositoryService {

    List<Dataset> findAllOwnedDatasets();

    List<DataTable> findAllOwnedDataTables();

    DataTable findOwnedDataTableByName(String entitySetName);

    List<AbstractDataRecord> findAllOwnedDataRecords();

    List<AbstractDataRecord> findAllDataRecordsForDataTable(DataTable dataTable);

    void updateRecord(AbstractDataRecord dataRecord);

}
