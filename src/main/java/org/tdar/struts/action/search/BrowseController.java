package org.tdar.struts.action.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.QueryBuilder;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.ResourceQueryBuilder;
import org.tdar.search.query.SortOption;

/**
 * $Id$
 * 
 * <p>
 * Action for the root namespace.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Namespace("/browse")
@ParentPackage("default")
@Component
@Scope("prototype")
@Results({ @Result(name = "authenticated", type = "redirect", location = "/") })
public class BrowseController extends AbstractLookupController {

    private static final long serialVersionUID = -128651515783098910L;
    private Creator creator;
    private List<ResourceCollection> collections = new ArrayList<ResourceCollection>();

    // private Keyword keyword;

    @Override
    @Action(results = {
            @Result(name = "success", location = "about.ftl")
    })
    public String execute() {
        return SUCCESS;
    }

    // FIXME: if we had real facets, this would not be needed
    // @Action(value = "places", results = { @Result(location = "results.ftl") })
    // public String browsePlaces() {
    // setResults(getResourceService().findResourceLinkedValues(GeographicKeyword.class));
    // return SUCCESS;
    // }
    //
    // @Action(value = "cultures", results = { @Result(location = "results.ftl") })
    // public String browseCultures() {
    // setResults(getResourceService().findResourceLinkedValues(CultureKeyword.class));
    // return SUCCESS;
    // }

    
    @Action("collections")
    public String browseCollections() throws ParseException {
            setCollections(getResourceCollectionService().findAllTopLevelCollections());
        return SUCCESS;
    }

    @Action(value = "creators", results = { @Result(location = "results.ftl") })
    public String browseCreators() throws ParseException {
        if (getId() == null || getId() == -1) {
            // setResults(getResourceService().findResourceLinkedValues(Creator.class));
        } else {
            creator = getGenericService().find(Creator.class, getId());
            QueryBuilder qb = new ResourceQueryBuilder();
            qb.append(new FieldQueryPart(QueryFieldNames.RESOURCE_CREATORS_CREATOR_ID, getId().toString()));
            qb.append(new FieldQueryPart(QueryFieldNames.STATUS, Status.ACTIVE.toString()));
            setMode("browseCreators");
            setSortField(SortOption.RESOURCE_TYPE);
            handleSearch(qb);
        }
        // setResults(getResourceService().findResourceLinkedValues(Creator.class));
        return SUCCESS;
    }

    // @Action(value = "materials", results = { @Result(location = "results.ftl") })
    // public String browseMaterialTypes() {
    // setResults(getResourceService().findResourceLinkedValues(MaterialKeyword.class));
    // return SUCCESS;
    // }
    //
    // @Action(value = "places", results = { @Result(location = "results.ftl") })
    // public String browseInvestigationTypes() {
    // setResults(getResourceService().findResourceLinkedValues(InvestigationType.class));
    // return SUCCESS;
    // }


    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }
    //
    // public Keyword getKeyword() {
    // return keyword;
    // }
    //
    // public void setKeyword(Keyword keyword) {
    // this.keyword = keyword;
    // }

    /**
     * @return the collections
     */
    public List<ResourceCollection> getCollections() {
        return collections;
    }

    /**
     * @param collections the collections to set
     */
    public void setCollections(List<ResourceCollection> collections) {
        this.collections = collections;
    }

}
