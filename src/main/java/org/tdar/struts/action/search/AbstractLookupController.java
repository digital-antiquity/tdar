/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.hibernate.search.FullTextQuery;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset.IntegratableOptions;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.InstitutionQueryBuilder;
import org.tdar.search.query.builder.PersonQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.InstitutionQueryPart;
import org.tdar.search.query.part.PersonQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryGroup;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.utils.PaginationHelper;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractLookupController<I extends Indexable> extends AuthenticationAware.Base implements SearchResultHandler<I> {

    private static final long serialVersionUID = 2357805482356017885L;

    private String callback;
    private int minLookupLength = 3;
    private int recordsPerPage = 10;
    private int startRecord = DEFAULT_START;
    private List<I> results = Collections.emptyList();
    private int totalRecords;
    private SortOption sortField = SortOption.RELEVANCE;
    private SortOption secondarySortField = SortOption.TITLE;
    private boolean debug = false;
    private ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
    public static final String ERROR_MINIMUM_LENGTH = "Search term shorter than minimum length";
    private Long id = null;
    private String mode;
    private String searchTitle;
    private String searchDescription;
    // execute a query even if query is empty
    private boolean showAll = false;

    private LookupSource lookupSource;

    private PaginationHelper paginationHelper;

    protected void handleSearch(QueryBuilder q) throws ParseException {
        getSearchService().handleSearch(q, this);
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public int getMinLookupLength() {
        return minLookupLength;
    }

    public void addFacets(FullTextQuery ftq) {
        // empty method, overriden if needed
    }

    public void setMinLookupLength(int minLookupLength) {
        this.minLookupLength = minLookupLength;
    }

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public int getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    public SortOption getSortField() {
        // if (sortField == null) {
        // sortField = SortOption.RELEVANCE;
        // }
        return sortField;
    }

    public void setSortField(SortOption sortField) {
        this.sortField = sortField;
    }

    /**
     * @param totalRecords
     *            the totalRecords to set
     */
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    /**
     * @return the totalRecords
     */
    public int getTotalRecords() {
        return totalRecords;
    }

    /**
     * Return true if the specified string meets the minimum length requirement (or if there is no minimum length requirement). Not to be confused w/
     * checking if the specified string is blank.
     */
    public boolean checkMinString(String look) {
        if (getMinLookupLength() == 0)
            return true;
        return (!StringUtils.isEmpty(look) && look.trim().length() >= getMinLookupLength());
    }

    // return true if ALL of the specified strings meet the minimum length. Otherwise false;
    public boolean checkMinString(String... candidates) {
        for (String candidate : candidates) {
            if (!checkMinString(candidate))
                return false;
        }
        return true;
    }

    protected void addEscapedWildcardField(QueryGroup q, String field, String value) {
        if (checkMinString(value) && StringUtils.isNotBlank(value)) {
            getLogger().trace(field + ":" + value);
            FieldQueryPart<String> fqp = new FieldQueryPart<String>(field, value);
            fqp.setPhraseFormatters(PhraseFormatter.WILDCARD);
            q.append(fqp);
        }
    }

    protected void addQuotedEscapedField(QueryGroup q, String field, String value) {
        if (checkMinString(value)) {
            getLogger().trace(field + ":" + value);
            FieldQueryPart<String> fqp = new FieldQueryPart<String>(field, value);
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            q.append(fqp);
        }
    }

    protected void appendIf(boolean test, QueryGroup q, String field, String value) {
        if (test) {
            q.append(new FieldQueryPart<String>(field, value));
        }
    }

    protected void addResourceTypeQueryPart(QueryGroup q, List<ResourceType> list) {
        if (!CollectionUtils.isEmpty(list)) {
            FieldQueryPart<ResourceType> fqp = new FieldQueryPart<ResourceType>("resourceType", list.toArray(new ResourceType[0]));
            fqp.setOperator(Operator.OR);
            q.append(fqp);
        }
    }

    // deal with the terms that correspond w/ the "narrow your search" section
    // and from facets
    protected QueryPartGroup processReservedTerms() {
        getAuthenticationAndAuthorizationService().initializeReservedSearchParameters(getReservedSearchParameters(), getAuthenticatedUser());
        return getReservedSearchParameters().toQueryPartGroup();
    }

    public int getNextPageStartRecord() {
        return startRecord + recordsPerPage;
    }

    public int getPrevPageStartRecord() {
        return startRecord - recordsPerPage;
    }

    /**
     * @param results
     *            the results to set
     */
    public void setResults(List<I> results) {
        this.results = results;
    }

    /**
     * @return the results
     */
    public List<I> getResults() {
        return results;
    }

    /*
     * 
     */
    protected List<Creator> getCreatorResults() {
        return (List<Creator>) results;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug
     *            the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean ignoringEmptyQuery) {
        this.showAll = ignoringEmptyQuery;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the secondarySortField
     */
    public SortOption getSecondarySortField() {
        return secondarySortField;
    }

    /**
     * @param secondarySortField
     *            the secondarySortField to set
     */
    public void setSecondarySortField(SortOption secondarySortField) {
        this.secondarySortField = secondarySortField;
    }

    /**
     * @param useSubmitterContext
     *            the useSubmitterContext to set
     */
    public void setUseSubmitterContext(boolean useSubmitterContext) {
        getReservedSearchParameters().setUseSubmitterContext(useSubmitterContext);
    }

    /**
     * @return the useSubmitterContext
     */
    public boolean isUseSubmitterContext() {
        return getReservedSearchParameters().isUseSubmitterContext();
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode
     *            the mode to set
     */
    // TODO: method needs better name... this is just metadata used to describe the caller of handleSearch()
    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSearchDescription() {
        return searchDescription;
    }

    public void setSearchDescription(String searchDescription) {
        this.searchDescription = searchDescription;
    }

    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    public ReservedSearchParameters getReservedSearchParameters() {
        return reservedSearchParameters;
    }

    public void setReservedSearchParameters(ReservedSearchParameters reservedSearchParameters) {
        this.reservedSearchParameters = reservedSearchParameters;
    }

    public List<Status> getAllStatuses() {
        return new ArrayList<Status>(Arrays.asList(Status.values()));
    }

    public List<Status> getIncludedStatuses() {
        return getReservedSearchParameters().getStatuses();
    }

    public void setIncludedStatuses(List<Status> statuses) {
        getReservedSearchParameters().setStatuses(statuses);
    }

    public List<ResourceType> getResourceTypes() {
        return getReservedSearchParameters().getResourceTypes();
    }

    public List<ResourceType> getAllResourceTypes() {
        ArrayList<ResourceType> arrayList = new ArrayList<ResourceType>(Arrays.asList(ResourceType.values()));
        arrayList.remove(ResourceType.VIDEO);
        return arrayList;
    }

    public List<IntegratableOptions> getIntegratableOptions() {
        return getReservedSearchParameters().getIntegratableOptions();
    }

    public void setIntegratableOptions(List<IntegratableOptions> integratableOptions) {
        getReservedSearchParameters().setIntegratableOptions(integratableOptions);
    }

    // REQUIRED IF YOU WANT FACETING TO ACTUALLY WORK
    public void setResourceTypes(List<ResourceType> resourceTypes) {
        getReservedSearchParameters().setResourceTypes(resourceTypes);
    }

    public String findPerson(String firstName, String term, String lastName, String institution, String email, String registered) {
        this.setLookupSource(LookupSource.PERSON);
        QueryBuilder q = new PersonQueryBuilder(Operator.AND);
        boolean valid = false;
        Person incomingPerson = new Person();
        if (checkMinString(firstName)) {
            incomingPerson.setFirstName(firstName);
            valid = true;
        }

        if (checkMinString(lastName)) {
            incomingPerson.setLastName(lastName);
            valid = true;
        }

        if (StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName) && checkMinString(term)) {
            incomingPerson.setWildcardName(term);
            valid = true;
        }

        if (checkMinString(institution)) {
            valid = true;
            Institution incomingInstitution = new Institution(institution);
            incomingPerson.setInstitution(incomingInstitution);
            getGenericService().detachFromSession(incomingInstitution);
        }

        // ignore email field for unauthenticated users.
        if (isAuthenticated() && checkMinString(email)) {
            incomingPerson.setEmail(email);
            valid = true;
        }
        getGenericService().detachFromSession(incomingPerson);

        PersonQueryPart pqp = new PersonQueryPart();
        pqp.add(incomingPerson);
        q.append(pqp);
        q.append(new FieldQueryPart<Status>("status", Status.ACTIVE));
        if (valid || getMinLookupLength() == 0) {
            if (StringUtils.isNotBlank(registered)) {
                try {
                    pqp.setRegistered(Boolean.parseBoolean(registered));
                } catch (Exception e) {
                    addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                    return ERROR;
                }
            }

            try {
                handleSearch(q);
                // sanitize results if the user is not logged in
            } catch (ParseException e) {
                addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                return ERROR;
            }
        }
        return SUCCESS;
    }

    public String findInstitution(String institution) {
        this.setLookupSource(LookupSource.INSTITUTION);
        QueryBuilder q = new InstitutionQueryBuilder(Operator.AND);
        if (checkMinString(institution)) {
            InstitutionQueryPart iqp = new InstitutionQueryPart();
            Institution testInstitution = new Institution(institution);
            if (StringUtils.isNotBlank(institution)) {
                iqp.add(testInstitution);
                q.append(iqp);
            }
            q.append(new FieldQueryPart<Status>("status", Status.ACTIVE));
            try {
                handleSearch(q);
            } catch (ParseException e) {
                addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                return ERROR;
            }
        }
        return SUCCESS;
    }

    public LookupSource getLookupSource() {
        return lookupSource;
    }

    public void setLookupSource(LookupSource lookupSource) {
        this.lookupSource = lookupSource;
    }

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null)
            paginationHelper = PaginationHelper.withSearchResults(this);
        return paginationHelper;
    }

    /**
     * indicates whether view layer should hide facet + sort controls
     * 
     * @return
     */
    public boolean isHideFacetsAndSort() {
        return true;
    }

}
