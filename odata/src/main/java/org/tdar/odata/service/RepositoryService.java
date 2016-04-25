package org.tdar.odata.service;

import java.util.Collection;
import java.util.List;

import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.db.model.abstracts.AbstractDataRecord;

public interface RepositoryService {

    List<Dataset> findAllOwnedDatasets();

    Collection<DataTable> findAllOwnedDataTables();

    DataTable findOwnedDataTableByName(String entitySetName);

    List<AbstractDataRecord> findAllOwnedDataRecords();

    List<AbstractDataRecord> findAllDataRecordsForDataTable(DataTable dataTable);

    void updateRecord(AbstractDataRecord dataRecord);

}
