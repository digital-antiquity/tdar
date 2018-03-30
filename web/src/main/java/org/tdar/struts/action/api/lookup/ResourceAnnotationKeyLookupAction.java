package org.tdar.struts.action.api.lookup;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.search.exception.SearchException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.query.ResourceAnnotationKeySearchService;
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
public class ResourceAnnotationKeyLookupAction extends AbstractLookupController<ResourceAnnotationKey> {

    private static final long serialVersionUID = 6481390840934368705L;

    @Autowired
    private transient ResourceAnnotationKeySearchService keySearchService;

    private String term;

    @Action(value = "annotationkey", results = {
            @Result(name = SUCCESS, type = JSONRESULT)
    })
    public String lookupAnnotationKey() {
        setMinLookupLength(2);
        setMode("annotationLookup");
        setLookupSource(LookupSource.KEYWORD);
        getLogger().trace("looking up:'{}'", getTerm());

        try {
            keySearchService.buildAnnotationSearch(term, this, getMinLookupLength(), this);
        } catch (SearchException | IOException e) {
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        jsonifyResult(JsonLookupFilter.class);
        return SUCCESS;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

}
