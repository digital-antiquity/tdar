package org.tdar.struts.action.api.lookup;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.search.bean.CollectionSearchQueryObject;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.SearchUtils;
import org.tdar.search.service.query.CollectionSearchService;
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
public class CollectionLookupAction extends AbstractLookupController<ResourceCollection> {

    private static final long serialVersionUID = -1785355137646480452L;

    @Autowired
    private transient CollectionSearchService collectionSearchService;

    private String term;
    private GeneralPermissions permission;

    @Action(value = "collection", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
    })
    public String lookupResourceCollection() throws SolrServerException, IOException {
        setMinLookupLength(0);
        setLookupSource(LookupSource.COLLECTION);
        getLogger().trace("looking up: '{}'", getTerm());
        setMode("collectionLookup");
        // only return results if query length has enough characters
        if (SearchUtils.checkMinString(getTerm(), getMinLookupLength())) {
            try {
                CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
                csqo.setPermission(getPermission());
                csqo.getTitles().add(getTerm());
                collectionSearchService.lookupCollection(getAuthenticatedUser(), csqo, this, this);
            } catch (ParseException e) {
                addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
                return ERROR;
            }
        }

        jsonifyResult(JsonLookupFilter.class);
        return SUCCESS;
    }

    public GeneralPermissions getPermission() {
        return permission;
    }

    public void setPermission(GeneralPermissions permission) {
        this.permission = permission;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

}
