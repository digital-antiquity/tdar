package org.tdar.struts.action.api.lookup;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Institution;
import org.tdar.struts.action.AbstractLookupController;

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

    @Action(value = "institution", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
    })
    public String lookupInstitution() throws SolrServerException, IOException {
        setMode("institutionLookup");
        return findInstitution(getInstitution());
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

}
