package org.tdar.struts.action.search;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.queryBuilder.QueryBuilder;
import org.tdar.search.query.queryBuilder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.queryBuilder.ResourceQueryBuilder;

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
        QueryBuilder qb = new ResourceCollectionQueryBuilder();
        qb.append(new FieldQueryPart(QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED));
        qb.append(new FieldQueryPart(QueryFieldNames.COLLECTION_VISIBLE, "true"));
        qb.append(new FieldQueryPart(QueryFieldNames.TOP_LEVEL, "true"));
        setMode("browseCollections");
        handleSearch(qb);

        return SUCCESS;
    }

    @Action(value = "creators", results = { @Result(location = "results.ftl") })
    public String browseCreators() throws ParseException {
        if (getId() == null || getId() == -1) {
            // setResults(getResourceService().findResourceLinkedValues(Creator.class));
        } else {
            creator = getGenericService().find(Creator.class, getId());
            QueryBuilder queryBuilder = new ResourceQueryBuilder();
            queryBuilder.setOperator(Operator.AND);

            QueryPartGroup queryPartGroup = new QueryPartGroup();
            queryPartGroup.setOperator(Operator.OR);
            if (creator instanceof Institution) {
                // institution: return all active resources that list this institution as a creator or resource provider
                queryPartGroup.append(new FieldQueryPart(QueryFieldNames.RESOURCE_PROVIDER_ID, getId().toString()));
                queryPartGroup.append(new FieldQueryPart(QueryFieldNames.RESOURCE_CREATORS_CREATOR_ID, getId().toString()));
            } else {
                // person: return all resources that list this person as a creator
                queryPartGroup.append(new FieldQueryPart(QueryFieldNames.RESOURCE_CREATORS_CREATOR_ID, getId().toString()));
                queryPartGroup.append(new FieldQueryPart(QueryFieldNames.RESOURCE_OWNER, getId().toString()));
            }
            queryBuilder.append(queryPartGroup);
            queryBuilder.append(new FieldQueryPart(QueryFieldNames.STATUS, Status.ACTIVE.toString()));

            setMode("browseCreators");
            setSortField(SortOption.RESOURCE_TYPE);
            String descr = String.format("All Resources from %s", creator.getProperName());
            setRecordsPerPage(100);
            handleSearch(queryBuilder);
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

}
