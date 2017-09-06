package org.tdar.struts.action.api.lookup;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Institution;
import org.tdar.search.exception.SearchException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.SearchUtils;
import org.tdar.search.service.query.CreatorSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.utils.json.JsonLookupFilter;

/**
 * $Id$
 * <p>
 * 
 * @version $Rev$
 */
@Namespace("/api/lookup")
@ParentPackage("default")
@Component
@Scope("prototype")
public class InstitutionLookupAction extends AbstractLookupController<Institution> {

    private static final long serialVersionUID = -6332785137761675803L;

    private String institution;
    @Autowired
    private CreatorSearchService creatorSearchService;


    @Action(value = "institution", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
    })
    public String lookupInstitution() throws SolrServerException, IOException {
        setMode("institutionLookup");
        return findInstitution(getInstitution());
    }
    

    @SuppressWarnings("unchecked")
    public String findInstitution(String institution) throws SolrServerException, IOException {
        this.setLookupSource(LookupSource.INSTITUTION);
        if (SearchUtils.checkMinString(institution, getMinLookupLength())) {
            try {
                creatorSearchService.findInstitution(institution, this, this, getMinLookupLength());
            } catch (SearchException e) {
                addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
                return ERROR;
            }
        }
        jsonifyResult(JsonLookupFilter.class);
        return SUCCESS;
    }


    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

}
