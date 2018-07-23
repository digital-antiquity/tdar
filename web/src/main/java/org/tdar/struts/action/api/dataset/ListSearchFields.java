package org.tdar.struts.action.api.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.utils.json.JacksonView;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/dataset")
public class ListSearchFields extends AbstractJsonApiAction {

    private static final long serialVersionUID = -7518119815679985919L;

        private Set<DataTableColumn> columns = new HashSet<>();
        private Dataset dataset;
        private Long id;

        @Autowired
        private DatasetService datasetService;

        @Override
        public void prepare() throws Exception {
            dataset = getGenericService().find(Dataset.class, id);
            super.prepare();
        }

        @Action(value = "listSearchFields")
        public String execute() throws IOException {
            columns = datasetService.findSearchableColumns(dataset);
            setResultObject(columns);
            return SUCCESS;
        }

//        @Override
//        public Class<? extends JacksonView> getJsonView() {
//            return JacksonView;
//        }
        
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

}
