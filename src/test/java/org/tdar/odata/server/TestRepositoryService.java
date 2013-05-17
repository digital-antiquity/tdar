package org.tdar.odata.server;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.GenericService;

public class TestRepositoryService {

    // TODO RR: remove.Used by tests only
    //void save(DataTable dataTable);
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private GenericService genericService;

    // Used by integration tests
    public void save(DataTable dataTable) {
        // TODO RR: Need to provide access control here.
        genericService.save(dataTable);
    }

}
