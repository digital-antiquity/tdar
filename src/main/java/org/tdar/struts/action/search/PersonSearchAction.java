package org.tdar.struts.action.search;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.PersonQueryBuilder;
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
public class PersonSearchAction extends AbstractLookupController<Person> {

    private static final long serialVersionUID = -4399875145290579664L;

    private List<SortOption> sortOptions = SortOption.getOptionsForContext(Person.class);

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient SearchService searchService;

    private String query;

    @Action(value = "people", results = {
            @Result(name = SUCCESS, location = "people.ftl"),
            @Result(name = INPUT, location = "person.ftl") })
    public String searchPeople() throws TdarActionException {
        setSortOptions(SortOption.getOptionsForContext(Person.class));
        setMinLookupLength(0);
        setMode("PERSON");
        setLookupSource(LookupSource.PERSON);
        PersonQueryBuilder pqb = new PersonQueryBuilder();
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        Person person = Person.fromName(getQuery());
        group.append(new GeneralCreatorQueryPart(person));
        pqb.append(group);
        try {
            handleSearch(pqb);
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
