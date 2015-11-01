package org.tdar.struts.action.search;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.FacetGroup;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.InstitutionQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.GeneralCreatorQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpOnlyIfUnauthenticated
public class InstitutionSearchAction extends AbstractLookupController<Institution> {

    private static final long serialVersionUID = -2102002561399688184L;

    private List<SortOption> sortOptions = SortOption.getOptionsForContext(Institution.class);

    private String query;

    @Action(value = "institutions", results = {
            @Result(name = SUCCESS, location = "institutions.ftl"),
            @Result(name = INPUT, location = "institution.ftl") })
    public String searchInstitutions() throws TdarActionException {
        setSortOptions(SortOption.getOptionsForContext(Institution.class));
        setMinLookupLength(0);
        setLookupSource(LookupSource.INSTITUTION);
        setMode("INSTITUTION");
        InstitutionQueryBuilder iqb = new InstitutionQueryBuilder();
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        if (!isFindAll(getQuery())) {
            group.append(new GeneralCreatorQueryPart(new Institution(getQuery())));
            iqb.append(group);
        }
        try {
            handleSearch(iqb);
        } catch (TdarRecoverableRuntimeException | ParseException trex) {
            addActionError(trex.getMessage());
            return INPUT;
        }
        return SUCCESS;
    }


    public List<SortOption> getSortOptions() {
        sortOptions.remove(SortOption.RESOURCE_TYPE);
        sortOptions.remove(SortOption.RESOURCE_TYPE_REVERSE);
        return sortOptions;
    }

    public void setSortOptions(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
    }

    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
