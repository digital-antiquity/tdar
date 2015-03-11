package org.tdar.struts.action.lookup;

import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.part.AutocompleteTitleQueryPart;
import org.tdar.search.query.part.CollectionAccessQueryPart;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.data.FacetGroup;
import org.tdar.utils.json.JsonLookupFilter;

/**
 * $Id$
 * <p>
 * @version $Rev$
 */
@Namespace("/lookup")
@ParentPackage("default")
@Component
@Scope("prototype")
public class CollectionLookupAction extends AbstractLookupController<ResourceCollection> {


    private static final long serialVersionUID = -1785355137646480452L;

    @Autowired
    private transient AuthorizationService authorizationService;

    private String term;
    private GeneralPermissions permission;

    @Action(value = "collection", results = {
                    @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
            })
    public String lookupResourceCollection() {
        QueryBuilder q = new ResourceCollectionQueryBuilder();
        setMinLookupLength(0);
        setLookupSource(LookupSource.COLLECTION);
        getLogger().trace("looking up: '{}'", getTerm());
        setMode("collectionLookup");
        // only return results if query length has enough characters
        if (checkMinString(getTerm())) {
            q.append(new AutocompleteTitleQueryPart(getTerm()));
            boolean admin = false;
            if (authorizationService.can(InternalTdarRights.VIEW_ANYTHING, getAuthenticatedUser())) {
                admin = true;
            }
            CollectionAccessQueryPart queryPart = new CollectionAccessQueryPart(getAuthenticatedUser(), admin, getPermission());
            q.append(queryPart);
            try {
                handleSearch(q);
            } catch (ParseException e) {
                addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
                return ERROR;
            }
        }

        jsonifyResult(JsonLookupFilter.class);
        return SUCCESS;
    }    @SuppressWarnings("rawtypes")

    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
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
