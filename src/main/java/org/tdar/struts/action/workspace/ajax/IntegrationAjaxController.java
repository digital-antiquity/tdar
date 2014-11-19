package org.tdar.struts.action.workspace.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.dao.integration.DatasetIntegrationSearchFilter;
import org.tdar.core.dao.integration.OntologyIntegrationSearchFilter;
import org.tdar.core.service.DataIntegrationService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.search.query.SimpleSearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionSupport;

import com.opensymphony.xwork2.Preparable;

/**
 * Created by jimdevos on 10/28/14.
 */
@ParentPackage("secured")
@Namespace("/workspace/ajax")
@Component
@Scope("prototype")
@Results(value = {
        @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.JSONRESULT, params = { "stream", "jsonInputStream" })
})
public class IntegrationAjaxController extends AuthenticationAware.Base implements Preparable, SimpleSearchResultHandler {

    private static final long serialVersionUID = 7550182111626753594L;

    private OntologyIntegrationSearchFilter ontologyFilter;
    private DatasetIntegrationSearchFilter datasetFilter;
    private InputStream jsonInputStream;
    private Integer startRecord = 0;
    private int recordsPerPage = 10;

    @Autowired
    private transient OntologyService ontologyService;
    @Autowired
    private transient DataIntegrationService integrationService;
    @Autowired
    private transient DataTableService dataTableService;
    @Autowired
    private transient XmlService xmlService;
    List<Map<String, Object>> results = new ArrayList<>();

    @Override
    public void prepare() {
        integrationService.hydrateFilter(ontologyFilter, getAuthenticatedUser());
        integrationService.hydrateFilter(datasetFilter, getAuthenticatedUser());
    }
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Action(value = "find-datasets")
    public String findDatasets() throws IOException {
        if (datasetFilter == null) {
            datasetFilter = new DatasetIntegrationSearchFilter();
        }
        
        List<DataTable> findDataTables = dataTableService.findDataTables(datasetFilter, startRecord, recordsPerPage);
        for (DataTable result : findDataTables) {
            HashMap<String, Object> map = new HashMap<>();

            Dataset dataset = result.getDataset();
            map.put("dataset_id", dataset.getId());
            map.put("data_table_id", result.getId());
            map.put("data_table_name", result.getName());
            map.put("dataset_name", dataset.getTitle());
            map.put("dataset_submitter", dataset.getSubmitter().getProperName());
            map.put("dataset_date_created", formatter.format(dataset.getDateCreated()));
            map.put("integratable", dataset.getIntegratableOptions().getBooleanValue());
            results.add(map);
        }
        setJsonInputStream(new ByteArrayInputStream(xmlService.convertToJson(results).getBytes()));
        return SUCCESS;
    }

    @Action(value = "find-ontologies")
    public String findOntologies() throws IOException {
        if (ontologyFilter == null) {
            ontologyFilter = new OntologyIntegrationSearchFilter();
        }

        List<Ontology> ontologies = ontologyService.findOntologies(ontologyFilter, startRecord, recordsPerPage);
        for (Ontology ontology : ontologies) {
            HashMap<String, Object> map = new HashMap<>();
            CategoryVariable category = ontology.getCategoryVariable();
            map.put("id", ontology.getId());
            map.put("title", ontology.getTitle());
            map.put("category_variable_id", category.getId());
            map.put("category_variable_name", category.getName());
            map.put("ontology_name", ontology.getTitle());
            map.put("ontology_submitter", ontology.getSubmitter().getProperName());
            map.put("ontology_date_created", formatter.format(ontology.getDateCreated()));
            results.add(map);
        }
        setJsonInputStream(new ByteArrayInputStream(xmlService.convertToJson(results).getBytes()));
        return SUCCESS;
    }

    @Action(value = "table-details", results = {
            @Result(name = SUCCESS, type = "stream",
                    params = {
                            "contentType", "application/json",
                            "inputName", "tableDetailsJsonInputStream"
                    })
    })
    public String dataTableDetails() {
        return SUCCESS;
    }

    @Action(value = "ontology-details", results = {
            @Result(name = SUCCESS, type = "stream",
                    params = {
                            "contentType", "application/json",
                            "inputName", "ontologyDetailsJsonInputStream"
                    })
    })
    public String ontologyDetails() {
        return SUCCESS;
    }

    // FIXME: replace placeholder data
    public InputStream getTableDetailsJsonInputStream() {
        return getClass().getClassLoader().getResourceAsStream("integration-ajax-samples/get-table-details.json");
    }

    // FIXME: replace placeholder data
    public InputStream getOntologyDetailsJsonInputStream() {
        return getClass().getClassLoader().getResourceAsStream("integration-ajax-samples/get-ontology-details.json");
    }


    @Override
    public SortOption getSortField() {
        return null;
    }

    @Override
    public void setSortField(SortOption sortField) {
    }

    @Override
    public int getStartRecord() {
        return startRecord;
    }

    @Override
    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    @Override
    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    @Override
    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;

    }

    public List<Map<String,Object>> getResults() {
        return results;
    }

    public OntologyIntegrationSearchFilter getOntologyFilter() {
        return ontologyFilter;
    }

    public void setOntologyFilter(OntologyIntegrationSearchFilter ontologyFilter) {
        this.ontologyFilter = ontologyFilter;
    }

    public DatasetIntegrationSearchFilter getDatasetFilter() {
        return datasetFilter;
    }

    public void setDatasetFilter(DatasetIntegrationSearchFilter datasetFilter) {
        this.datasetFilter = datasetFilter;
    }

    public InputStream getJsonInputStream() {
        return jsonInputStream;
    }

    public void setJsonInputStream(InputStream jsonInputStream) {
        this.jsonInputStream = jsonInputStream;
    }

}
