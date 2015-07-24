package org.tdar.struts.action.lookup;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.action.search.FacetGroup;

/**
 * $Id$
 * <p>
 * 
 * @version $Rev$
 */
@Namespace("/lookup")
@ParentPackage("default")
@Component
@Scope("prototype")
public class InstitutionLookupAction extends AbstractLookupController<Institution> {

    private static final long serialVersionUID = -6332785137761675803L;

    private String institution;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Action(value = "institution", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
    })
    public String lookupInstitution() {
        setMode("institutionLookup");
        return findInstitution(getInstitution());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

}
