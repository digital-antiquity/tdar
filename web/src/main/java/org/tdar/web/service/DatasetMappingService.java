package org.tdar.web.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTableColumn;

public interface DatasetMappingService {

    /*
     * convenience method, used for Asynchronous as opposed to the Synchronous version by the Controller
     */
    void remapColumnsAsync(List<DataTableColumn> columns, Project project);

    void remapColumns(List<DataTableColumn> columns, Project project);

}