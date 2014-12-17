package org.tdar.struts.action.api.integration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.integration.OntologySearchFilter;
import org.tdar.core.dao.resource.integration.IntegrationOntologySearchResult;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.search.query.SimpleSearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class IntegrationOntologyLookupAction extends AbstractIntegrationAction implements Preparable, SimpleSearchResultHandler {

    private static final long serialVersionUID = -1440176848488485510L;
    private Integer startRecord = 0;
    private Integer recordsPerPage = 100;
    private OntologySearchFilter searchFilter = new OntologySearchFilter(recordsPerPage, startRecord);

    @Autowired
    private transient OntologyService ontologyService;
    @Autowired
    private transient SerializationService serializationService;

    @Override
    public void prepare() {
        searchFilter.setAuthorizedUser(getAuthenticatedUser());
        searchFilter.setMaxResults(getRecordsPerPage());
        searchFilter.setFirstResult(getStartRecord());
    }

    @Action(value = "find-ontologies")
    public String findOntologies() throws IOException {

        getLogger().debug("find-ontologies:: searchFilter: {}", searchFilter);
        IntegrationOntologySearchResult result = ontologyService.findOntologies(searchFilter);
        setJsonInputStream(new ByteArrayInputStream(serializationService.convertToFilteredJson(result, JsonIntegrationFilter.class).getBytes()));
        return SUCCESS;
    }
    
    public OntologySearchFilter getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(OntologySearchFilter searchFilter) {
        this.searchFilter = searchFilter;
    }

    @Override
    public void setRecordsPerPage(int recordsPerPage) {
        // TODO Auto-generated method stub

    }

    @Override
    public SortOption getSortField() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSortField(SortOption sortField) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getStartRecord() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setStartRecord(int startRecord) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getRecordsPerPage() {
        // TODO Auto-generated method stub
        return 0;
    }

}
