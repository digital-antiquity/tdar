/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.search;

import static org.tdar.core.service.external.auth.InternalTdarRights.SEARCH_FOR_DELETED_RECORDS;
import static org.tdar.core.service.external.auth.InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS;
import static org.tdar.core.service.external.auth.InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.hibernate.search.FullTextQuery;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.QueryDescriptionBuilder;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.QueryGroup;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.queryBuilder.QueryBuilder;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.search.query.SearchResultHandler;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractLookupController <I extends Indexable> extends AuthenticationAware.Base implements SearchResultHandler<I> {

    private static final long serialVersionUID = 2357805482356017885L;

    private String callback;
    private int minLookupLength = 3;
    private int recordsPerPage = 10;
    private int startRecord = DEFAULT_START;
    private List<I> results = Collections.emptyList();
    private int totalRecords;
    private SortOption sortField = SortOption.RELEVANCE;
    private SortOption secondarySortField;
    private boolean debug = false;
    public static final String ERROR_MINIMUM_LENGTH = "Search term shorter than minimum length";
    private List<ResourceType> resourceTypes = new ArrayList<ResourceType>();
    private List<Status> includedStatuses = new ArrayList<Status>();
    private String title;
    private Long id = null;
    private boolean useSubmitterContext = false;
    private String mode;

    // execute a query even if query is empty
    private boolean showAll = false;

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
        if (sortField == null) {
            sortField = SortOption.RELEVANCE;
        }
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
            FieldQueryPart fqp = new FieldQueryPart(field);
            fqp.setEscapeWildcardValue(value);
            q.append(fqp);
        }
    }

    protected void addEscapedField(QueryGroup q, String field, String value) {
        if (checkMinString(value)) {
            getLogger().trace(field + ":" + value);
            FieldQueryPart fqp = new FieldQueryPart(field);
            fqp.setEscapedValue(value);
            q.append(fqp);
        }
    }

    protected void addQuotedEscapedField(QueryGroup q, String field, String value) {
        if (checkMinString(value)) {
            getLogger().trace(field + ":" + value);
            FieldQueryPart fqp = new FieldQueryPart(field);
            fqp.setQuotedEscapeValue(value);
            q.append(fqp);
        }
    }

    protected void appendIf(boolean test, QueryGroup q, String field, String value) {
        if (test) {
            q.append(new FieldQueryPart(field, value));
        }
    }

    protected void addResourceTypeQueryPart(QueryGroup q, List<ResourceType> list) {
        if (!CollectionUtils.isEmpty(list)) {
            QueryPartGroup grp = new QueryPartGroup();
            grp.setOperator(Operator.OR);
            for (ResourceType resourceType : list) {
                if (resourceType != null) {
                    grp.append(new FieldQueryPart("resourceType", resourceType));
                }
            }
            if (!grp.isEmpty()) {
                q.append(grp);
            }
        }
    }

    protected void addTitlePart(QueryGroup q, String title, boolean autocomplete) {
        if (StringUtils.isEmpty(title))
            return;
        String titleFieldName = QueryFieldNames.TITLE;
        if (autocomplete) {
            titleFieldName = QueryFieldNames.TITLE_AUTO;
        }
        QueryPartGroup grp = new QueryPartGroup();
        grp.setOperator(Operator.OR);
        addEscapedWildcardField(grp, titleFieldName, title);
        FieldQueryPart titlePart = new FieldQueryPart(titleFieldName);
        titlePart.setQuotedEscapeValue(title);
        grp.append(titlePart.setBoost(6f));
        q.append(grp);
    }

    protected void appendStatusInformation(QueryBuilder q, QueryDescriptionBuilder queryDescriptionBuilder, List<Status> statuses, Person user) {
        logger.trace("initial status for {} : {}", user, statuses);
        CollectionUtils.filter(statuses, NotNullPredicate.INSTANCE);
        //selecting nothing is the same as selecting everything
        if (CollectionUtils.isEmpty(statuses)) {
            statuses.addAll(Arrays.asList(Status.values()));
        }

        // remove invalid, inappropriate selections
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(statuses, Status.DELETED, SEARCH_FOR_DELETED_RECORDS, user);
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(statuses, Status.FLAGGED, SEARCH_FOR_FLAGGED_RECORDS, user);
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(statuses, Status.DRAFT, SEARCH_FOR_DRAFT_RECORDS, user, isUseSubmitterContext());

        if (CollectionUtils.isEmpty(statuses)) {
            throw new TdarRecoverableRuntimeException("no valid statuses were specified");
        }

        if (queryDescriptionBuilder != null && !(statuses.size() == 1 && statuses.contains(Status.ACTIVE))) {
            queryDescriptionBuilder.append(QueryDescriptionBuilder.WITH_STATUSES, statuses);
        }

        logger.trace("ending status for {} : {}", user, statuses);
        appendStatusTypes(q, statuses);
    }

    protected void appendStatusTypes(QueryGroup q, List<Status> includedStatuses) {
        QueryPartGroup group = new QueryPartGroup();
        group.setOperator(Operator.OR);
        if (includedStatuses != null) {
            for (Status status : includedStatuses) {
                if (status != null) {
                    group.append(new FieldQueryPart(QueryFieldNames.STATUS, status));
                }
            }
        }
        if (!group.isEmpty()) {
            q.append(group);
        }
    }

    public int getNextPageStartRecord() {
        return startRecord + recordsPerPage;
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
     * @return the resourceTypes
     */
    public List<ResourceType> getResourceTypes() {
        return resourceTypes;
    }

    /**
     * @param resourceTypes
     *            the resourceTypes to set
     */
    public void setResourceTypes(List<ResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
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
     * @return the includedStatuses
     */
    public List<Status> getIncludedStatuses() {
        return includedStatuses;
    }

    /**
     * @param includedStatuses
     *            the includedStatuses to set
     */
    public void setIncludedStatuses(List<Status> includedStatuses) {
        //we need a list we can mutate
        this.includedStatuses = new ArrayList<Status>(includedStatuses);      
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
        this.useSubmitterContext = useSubmitterContext;
    }

    /**
     * @return the useSubmitterContext
     */
    public boolean isUseSubmitterContext() {
        return useSubmitterContext;
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
    public void setMode(String mode) {
        this.mode = mode;
    }
}
