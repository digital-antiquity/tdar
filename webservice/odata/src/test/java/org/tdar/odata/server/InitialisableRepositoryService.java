package org.tdar.odata.server;

import java.util.Collection;
import java.util.List;

import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.odata.service.RepositoryService;

// RR: Used by tests.

public interface InitialisableRepositoryService extends RepositoryService {

    public void save(DataTable dataTable);

    void saveValueByTableNameAndPropertyName(String dataTableName, String propertyName, Object propertyValue);

    void saveOwnedDatasetByName(String dataSetName, Dataset dataset);

    void saveOwnedDatasets(List<Dataset> ownedDataSets);

    public void saveOwnedDataTables(Collection<DataTable> arrayList);

}
