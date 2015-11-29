package org.tdar.struts.action.lookup;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.query.FacetGroup;
import org.tdar.struts.action.AbstractLookupController;

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
public class ResourceAnnotationKeyLookupAction extends AbstractLookupController<ResourceAnnotationKey> {

    private static final long serialVersionUID = 6481390840934368705L;

    @Autowired
    private transient AuthorizationService authorizationService;

    private String term;

    @Action(value = "annotationkey", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
    })
    public String lookupAnnotationKey() {
//        QueryBuilder q = new ResourceAnnotationKeyQueryBuilder();
//        setMinLookupLength(2);
//        setMode("annotationLookup");
//
//        setLookupSource(LookupSource.KEYWORD);
//        getLogger().trace("looking up:'{}'", getTerm());
//
//        // only return results if query length has enough characters
//        if (checkMinString(getTerm())) {
//            addQuotedEscapedField(q, "annotationkey_auto", getTerm());
//            try {
//                handleSearch(q);
//            } catch (ParseException e) {
//                addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
//                return ERROR;
//            }
//        }
//
//        jsonifyResult(JsonLookupFilter.class);
        return SUCCESS;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

}
