package org.tdar.struts.action.admin;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.InstitutionQueryBuilder;
import org.tdar.search.query.builder.KeywordQueryBuilder;
import org.tdar.search.query.builder.PersonQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.struts.action.search.AbstractLookupController;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin/search")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class AdminSearchController extends AbstractLookupController<Indexable> {

    private static final long serialVersionUID = 7413576291256790727L;

    private String rawQuery;

    public enum QueryBuilders {
        RESOURCE,
        INSTITUTION,
        PERSON,
        KEYWORD,
        COLLECTION
    }

    private QueryBuilders queryBuilder;

    @Action(value = "search")
    @Override
    public String execute() {
        return SUCCESS;
    }

    @Action(value = "lookup", results = { @Result(name = "success", location = "../../lookup/lookup.ftl", type = "freemarker", params = { "contentType",
            "application/json" }) })
    public String lookup() {
        QueryBuilder q = null;
        switch (queryBuilder) {
            case COLLECTION:
                q = new ResourceCollectionQueryBuilder();
                break;
            case INSTITUTION:
                q = new InstitutionQueryBuilder();
                break;
            case KEYWORD:
                q = new KeywordQueryBuilder();
                break;
            case PERSON:
                q = new PersonQueryBuilder();
                break;
            case RESOURCE:
                q = new ResourceQueryBuilder();
                break;
        }
        setMode("admin lookup");
        if (q == null) {
            throw new TdarRecoverableRuntimeException("cannot determine QueryBuilder");
        }
        q.setRawQuery(rawQuery);
        try {
            handleSearch(q);
            logger.trace("jsonResults:" + getResults());
        } catch (ParseException e) {
            addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
            return ERROR;
        }

        return SUCCESS;
    }

    public List<QueryBuilders> getAllQueryBuilders() {
        return Arrays.asList(QueryBuilders.values());
    }

    @Override
    public ProjectionModel getProjectionModel() {
        return ProjectionModel.HIBERNATE_DEFAULT;
    };
    
    public QueryBuilders getQueryBuilder() {
        return queryBuilder;
    }

    public void setQueryBuilder(QueryBuilders queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public List<SortOption> getSortOptions() {
        return Arrays.asList(SortOption.values());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Facetable>> getFacetFields() {
        return null;
    }
}
