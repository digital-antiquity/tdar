package org.tdar.search.service.query;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.bean.PersonSearchOption;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.InstitutionQueryBuilder;
import org.tdar.search.query.builder.PersonQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.query.part.entity.GeneralCreatorQueryPart;
import org.tdar.search.query.part.entity.InstitutionAutocompleteQueryPart;
import org.tdar.search.query.part.entity.PersonQueryPart;
import org.tdar.search.service.SearchUtils;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

@Service
@Transactional
public class CreatorSearchServiceImpl<I extends Creator<?>> extends AbstractSearchService implements CreatorSearchService<I> {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SearchService<Person> searchService;


    /* (non-Javadoc)
	 * @see org.tdar.search.service.query.CreatorSearchInterface#searchInstitution(java.lang.String, org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider)
	 */
    @Override
	public LuceneSearchResultHandler<I> searchInstitution(String name, LuceneSearchResultHandler<I> result, TextProvider provider) throws SearchException, IOException {
        InstitutionQueryBuilder iqb = new InstitutionQueryBuilder();
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        if (!isFindAll(name)) {
            group.append(new GeneralCreatorQueryPart(new Institution(name)));
            iqb.append(group);
        }
        searchService.handleSearch(iqb, result, provider);
        return result;
    }

    protected boolean isFindAll(String query) {
        if (StringUtils.isBlank(query)) {
            return true;
        }
        return StringUtils.equals(StringUtils.trim(query), "*");
    }

    /* (non-Javadoc)
	 * @see org.tdar.search.service.query.CreatorSearchInterface#findPerson(java.lang.String, org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider)
	 */
    @Override
	public LuceneSearchResultHandler<I> findPerson(String query, PersonSearchOption personSearchOption, LuceneSearchResultHandler<I> result, TextProvider provider) throws SearchException, IOException {
        PersonQueryBuilder pqb = new PersonQueryBuilder();
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        if (!isFindAll(query)) {

        	TdarUser person = new TdarUser();
        	//The person search option will only be populated if the search was submitted by an editor.
        	if(personSearchOption==null){
        		return findPerson(query, result, provider);
        	}

        	//If the person search option is not null, then search by one or more of the other fields.
        		PersonQueryPart personQueryPart = new PersonQueryPart();
        		
        		boolean searchAllFields = personSearchOption == PersonSearchOption.ALL_FIELDS;
        		
        		if(personSearchOption == PersonSearchOption.ALL_FIELDS){
        			Person p_ = Person.fromName(query);
        			person.setFirstName(p_.getFirstName()); 
        			person.setLastName(p_.getLastName()); 
        		}
        		
        		if(personSearchOption == PersonSearchOption.FIRST_NAME){
        			person.setFirstName(query); 
        		}
        		
        		if(personSearchOption == PersonSearchOption.LAST_NAME){
        			person.setLastName(query); 
        		}
        		
        		if(personSearchOption == PersonSearchOption.EMAIL || searchAllFields){
        			person.setEmail(query);
        			personQueryPart.setIncludeEmail(true);
        		}
        		
        		if(personSearchOption == PersonSearchOption.USERNAME || searchAllFields){
        			person.setUsername(query);
        		}
        		
        		if(personSearchOption == PersonSearchOption.INSTITUTION || searchAllFields){
        			person.setInstitution(new Institution(query));
        		}
        		
        	
        		personQueryPart.add(person);
        		personQueryPart.setOperator(Operator.OR);
        		group.append(personQueryPart);
        		pqb.append(group);        		
        }
        searchService.handleSearch(pqb, result, provider);
        return result;
    }
    
    public LuceneSearchResultHandler<I> findPerson(String name, LuceneSearchResultHandler<I> result, TextProvider provider) throws SearchException, IOException {
        PersonQueryBuilder pqb = new PersonQueryBuilder();
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        if (!isFindAll(name)) {
            Person person = Person.fromName(name);
            group.append(new GeneralCreatorQueryPart(person));
            pqb.append(group);
        }
        searchService.handleSearch(pqb, result, provider);
        return result;
    }

    /* (non-Javadoc)
	 * @see org.tdar.search.service.query.CreatorSearchInterface#findInstitution(java.lang.String, org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider, int)
	 */
    @Override
	public LuceneSearchResultHandler<I> findInstitution(String institution, LuceneSearchResultHandler<I> result, TextProvider provider, int min) throws SearchException, IOException {
        InstitutionQueryBuilder q = new InstitutionQueryBuilder(Operator.AND);
        InstitutionAutocompleteQueryPart iqp = new InstitutionAutocompleteQueryPart();
        Institution testInstitution = new Institution(institution);
        if (StringUtils.isNotBlank(institution)) {
            iqp.add(testInstitution);
            q.append(iqp);
        }
        if (min > 0 && q.size() == 0) {
            return result;
        }
        q.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
        searchService.handleSearch(q, result, provider);
        return result;
    }

    /* (non-Javadoc)
	 * @see org.tdar.search.service.query.CreatorSearchInterface#findPerson(org.tdar.core.bean.entity.Person, java.lang.String, java.lang.Boolean, org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider, int)
	 */
    @Override
	public LuceneSearchResultHandler<I> findPerson(Person person_, String term, Boolean registered, LuceneSearchResultHandler<I> result, TextProvider provider, int min) throws SearchException, IOException {
        Person person = person_;
        if (person == null) {
            person = new Person();
        }
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
                logger.trace("{}", pqp.toString());
                if (PersistableUtils.isNotNullOrTransient(result.getAuthenticatedUser())) {
                    pqp.setIncludeEmail(true);
                }
            }
            if (registered == Boolean.TRUE) {
                q.append(new FieldQueryPart<Boolean>(QueryFieldNames.REGISTERED,Boolean.TRUE));
            }
            q.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
            searchService.handleSearch(q, result, provider);
        } 
        return result;
    }

}