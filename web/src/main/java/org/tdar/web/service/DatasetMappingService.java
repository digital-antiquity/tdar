package org.tdar.web.service;

import java.util.List;

import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTableColumn;

public interface DatasetMappingService {

    /*
     * convenience method, used for Asynchronous as opposed to the Synchronous version by the Controller
     */
    void remapColumnsAsync(Dataset dataset, List<DataTableColumn> columns);

    void remapColumns(Dataset dataset, List<DataTableColumn> columns);

}