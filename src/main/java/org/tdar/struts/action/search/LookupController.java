package org.tdar.struts.action.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.KeywordQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceAnnotationKeyQueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.AutocompleteTitleQueryPart;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.struts.data.FacetGroup;

/**
 * $Id$
 * <p>
 * Handles ajax requests for people, institutions, and resources *
 * 
 * @author <a href='mailto:jim.devos@asu.edu'>Jim deVos</a>
 * @version $Rev$
 */
@Namespace("/lookup")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class LookupController extends AbstractLookupController<Indexable> {

    private static final long serialVersionUID = 176288602101059922L;

    private String firstName;
    private String lastName;
    private String institution;
    private String email;
    private String registered;
    private String url;
    private String projectId;
    private String collectionId;
    private String title;

    private String keywordType;
    private String term;

    private Long sortCategoryId;
    private List<String> projections = new ArrayList<String>();
    private boolean includeCompleteRecord = false;
    private GeneralPermissions permission = GeneralPermissions.VIEW_ALL;

    @Action(value = "person",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupPerson() {
        setMode("personLookup");
        return findPerson(firstName, term, lastName, institution, email, registered);
    }

    @Action(value = "institution",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupInstitution() {
        setMode("institutionLookup");
        return findInstitution(institution);
    }

    @Action(value = "resource",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupResource() {
        QueryBuilder q = new ResourceQueryBuilder();
        this.setLookupSource(LookupSource.RESOURCE);
        setMode("resourceLookup");
        // if we're doing a coding sheet lookup, make sure that we have access to all of the information here
        if (!isIncludeCompleteRecord() || getAuthenticatedUser() == null) {
            logger.info("using projection {}, {}", isIncludeCompleteRecord(), getAuthenticatedUser());
            projections.add("id");
        }

        QueryPartGroup valueGroup = new QueryPartGroup();
        if (StringUtils.isNotBlank(getTerm())) {
            valueGroup.append(new AutocompleteTitleQueryPart(getTerm()));
        }

        // assumption: if sortCategoryId is set, we assume we are serving a coding-sheet/ontology autocomplete
        // FIXME: instead of guessing this way it may be better to break codingsheet/ontology autocomplete lookups to another action.
        if (getSortCategoryId() != null && getSortCategoryId() > -1) {
            // SHOULD PREFER THINGS THAT HAVE THAT CATEGORY ID
            FieldQueryPart<String> q2 = new FieldQueryPart<String>(QueryFieldNames.CATEGORY_ID, getSortCategoryId().toString().trim());
            q2.setBoost(2f);
            valueGroup.append(q2);
            valueGroup.setOperator(Operator.OR);

            // if searching by category AND title, a relevancy sort makes more sense
            if (StringUtils.isNotBlank(term)) {
                setSortField(SortOption.RELEVANCE);
            }
        }
        q.append(valueGroup);

        if (StringUtils.isNotBlank(projectId) && StringUtils.isNumeric(projectId)) {
            QueryPartGroup group = new QueryPartGroup();
            group.setOperator(Operator.OR);
            group.append(new FieldQueryPart<String>(QueryFieldNames.PROJECT_ID, projectId));
            group.append(new FieldQueryPart<String>(QueryFieldNames.ID, projectId));
            q.append(group);
        }

        // FIXME: SHOULD I BE "SHARED" OR PUBLIC
        appendIf(StringUtils.isNotBlank(collectionId) && StringUtils.isNumeric(collectionId), q, QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, collectionId);

        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }

        q.append(processReservedTerms());
        try {
            handleSearch(q);
            if (CollectionUtils.isNotEmpty(getProjections())) {
                setResults(getGenericService().populateSparseObjectsById(getResults(), Resource.class));
            }
            logger.trace("jsonResults:" + getResults());
        } catch (ParseException e) {
            addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
            return ERROR;
        }

        return SUCCESS;
    }

    @Action(value = "keyword",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupKeyword() {
        // only return results if query length has enough characters
        if (!checkMinString(this.term) && !checkMinString(keywordType))
            return SUCCESS;

        QueryBuilder q = new KeywordQueryBuilder(Operator.AND);
        this.setLookupSource(LookupSource.KEYWORD);
        QueryPartGroup group = new QueryPartGroup();

        group.setOperator(Operator.AND);
        addQuotedEscapedField(group, "label_auto", term);

        // refine search to the correct keyword type
        group.append(new FieldQueryPart<String>("keywordType", keywordType));
        setMode("keywordLookup");

        q.append(group);
        q.append(new FieldQueryPart<Status>("status", Status.ACTIVE));
        try {
            handleSearch(q);
        } catch (ParseException e) {
            addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
            return ERROR;
        }

        return SUCCESS;
    }

    @Action(value = "annotationkey",
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupAnnotationKey() {
        QueryBuilder q = new ResourceAnnotationKeyQueryBuilder();
        setMinLookupLength(2);
        setMode("annotationLookup");

        this.setLookupSource(LookupSource.KEYWORD);
        logger.trace("looking up:'" + term + "'");

        // only return results if query length has enough characters
        if (checkMinString(term)) {
            addQuotedEscapedField(q, "annotationkey_auto", term);
            try {
                handleSearch(q);
            } catch (ParseException e) {
                addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                return ERROR;
            }
        }

        return SUCCESS;
    }

    @Action(value = "collection",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupResourceCollection() {
        QueryBuilder q = new ResourceCollectionQueryBuilder();
        setMinLookupLength(0);

        this.setLookupSource(LookupSource.COLLECTION);
        logger.trace("looking up:'" + term + "'");
        setMode("collectionLookup");

        // only return results if query length has enough characters
        if (checkMinString(term)) {
            q.append(new AutocompleteTitleQueryPart(getTerm()));
            QueryPartGroup rightsGroup = new QueryPartGroup(Operator.OR);
            q.append(new FieldQueryPart<CollectionType>(QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED));
            rightsGroup.append(new FieldQueryPart<Boolean>(QueryFieldNames.COLLECTION_VISIBLE, Boolean.TRUE));
            if (Persistable.Base.isNotNullOrTransient(getAuthenticatedUser())) {
                String field = QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW;
                switch (getPermission()) {
                    case MODIFY_RECORD:
                        field = QueryFieldNames.COLLECTION_USERS_WHO_CAN_MODIFY;
                        break;
                    case ADMINISTER_GROUP:
                        field = QueryFieldNames.COLLECTION_USERS_WHO_CAN_ADMINISTER;
                        break;
                    default:
                        break;
                }
                rightsGroup.append(new FieldQueryPart<Long>(field, getAuthenticatedUser().getId()));
            }
            q.append(rightsGroup);
            try {
                handleSearch(q);
            } catch (ParseException e) {
                addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                return ERROR;
            }
        }

        return SUCCESS;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = StringUtils.trim(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = StringUtils.trim(lastName);
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = StringUtils.trim(institution);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = StringUtils.trim(email);
    }

    public String getRegistered() {
        return registered;
    }

    public void setRegistered(String registered) {
        this.registered = registered;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId.toString();
    }

    /**
     * @param term
     *            the term to set
     */
    public void setTerm(String term) {
        this.term = StringUtils.trim(term);
    }

    /**
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * @param keywordType
     *            the keywordType to set
     */
    public void setKeywordType(String keywordType) {
        this.keywordType = keywordType;
    }

    /**
     * @return the keywordType
     */
    public String getKeywordType() {
        return keywordType;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public Long getSortCategoryId() {
        return sortCategoryId;
    }

    public void setSortCategoryId(Long sortCategoryId) {
        this.sortCategoryId = sortCategoryId;
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

        this.title = StringUtils.trim(title);
    }

    @Override
    public List<String> getProjections() {
        return projections;
    }

    public boolean isIncludeCompleteRecord() {
        return includeCompleteRecord;
    }

    public void setIncludeCompleteRecord(boolean includeCompleteRecord) {
        this.includeCompleteRecord = includeCompleteRecord;
    }

    public GeneralPermissions getPermission() {
        return permission;
    }

    public void setPermission(GeneralPermissions permission) {
        this.permission = permission;
    }

    @Override
    public List<FacetGroup<? extends Facetable>> getFacetFields() {
        return null;
    }
}
