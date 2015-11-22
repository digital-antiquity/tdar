package org.tdar.search.service;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.InstitutionQueryBuilder;
import org.tdar.search.query.builder.PersonQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.GeneralCreatorQueryPart;
import org.tdar.search.query.part.InstitutionAutocompleteQueryPart;
import org.tdar.search.query.part.PersonQueryPart;
import org.tdar.search.query.part.QueryPartGroup;

import com.opensymphony.xwork2.TextProvider;

@Service
@Transactional
public class CreatorSearchService<I extends Indexable> {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SearchService<Person> searchService;

    /**
     * Generates a query for resources created by or releated to in some way to a @link Creator given a creator and a user
     *
     * @param creator
     * @param user
     * @return
     */
    public QueryBuilder generateQueryForRelatedResources(Creator creator, TdarUser user, TextProvider provider) {
        QueryBuilder queryBuilder = new ResourceQueryBuilder();
        queryBuilder.setOperator(Operator.AND);
        SearchParameters params = new SearchParameters(Operator.AND);
        params.setCreatorOwner(new ResourceCreatorProxy(creator, null));
        queryBuilder.append(params, provider);
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        searchService.initializeReservedSearchParameters(reservedSearchParameters, user);
        queryBuilder.append(reservedSearchParameters, provider);
        return queryBuilder;
    }

    public InstitutionQueryBuilder searchInstitution(String name) {
        InstitutionQueryBuilder iqb = new InstitutionQueryBuilder();
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        if (!isFindAll(name)) {
            group.append(new GeneralCreatorQueryPart(new Institution(name)));
            iqb.append(group);
        }
        return iqb;
    }

    protected boolean isFindAll(String query) {
        if (StringUtils.isBlank(query)) {
            return true;
        }
        return StringUtils.equals(StringUtils.trim(query), "*");
    }

    public PersonQueryBuilder findPerson(String name) {
        PersonQueryBuilder pqb = new PersonQueryBuilder();
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        if (!isFindAll(name)) {
            Person person = Person.fromName(name);
            group.append(new GeneralCreatorQueryPart(person));
            pqb.append(group);
        }
        return pqb;
    }

    public InstitutionQueryBuilder findInstitution(String institution) {
        InstitutionQueryBuilder q = new InstitutionQueryBuilder(Operator.AND);
        InstitutionAutocompleteQueryPart iqp = new InstitutionAutocompleteQueryPart();
        Institution testInstitution = new Institution(institution);
        if (StringUtils.isNotBlank(institution)) {
            iqp.add(testInstitution);
            q.append(iqp);
        }
        q.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
        return q;
    }

    public PersonQueryBuilder findPerson(Person person, String term, Boolean registered, int min) {
        PersonQueryBuilder q = new PersonQueryBuilder(Operator.AND);
        boolean valid = false;

        if (SearchUtils.checkMinString(person.getFirstName(), min)) {
            person.setFirstName(person.getFirstName());
            valid = true;
        }

        if (SearchUtils.checkMinString(person.getLastName(), min)) {
            person.setLastName(person.getLastName());
            valid = true;
        }

        if (StringUtils.isEmpty(person.getFirstName()) && StringUtils.isEmpty(person.getLastName()) && SearchUtils.checkMinString(term, min)) {
            person.setWildcardName(term);
            valid = true;
        }

        if (SearchUtils.checkMinString(person.getInstitutionName(), min)) {
            valid = true;
        }

        // ignore email field for unauthenticated users.
        if (SearchUtils.checkMinString(person.getEmail(), min)) {
            valid = true;
        }

        if (valid || min == 0) {
            if (valid) {
                PersonQueryPart pqp = new PersonQueryPart();
                pqp.add(person);
                q.append(pqp);
                logger.debug("{}", pqp.toString());
            }
            q.append(new FieldQueryPart<Status>("status", Status.ACTIVE));
        }
        return q;
    }

}