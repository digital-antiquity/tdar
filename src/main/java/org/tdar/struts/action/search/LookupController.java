package org.tdar.struts.action.search;

import java.util.List;

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
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.KeywordQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceAnnotationKeyQueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.AutocompleteTitleQueryPart;
import org.tdar.search.query.part.CategoryTermQueryPart;
import org.tdar.search.query.part.CollectionAccessQueryPart;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.ProjectIdLookupQueryPart;
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
    private Long projectId;
    private Long collectionId;
    private String title;

    private String keywordType;
    private String term;

    private Long sortCategoryId;
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
        if (!isIncludeCompleteRecord() || (getAuthenticatedUser() == null)) {
            getLogger().info("using projection {}, {}", isIncludeCompleteRecord(), getAuthenticatedUser());
            setProjectionModel(ProjectionModel.RESOURCE_PROXY);
        }

        q.append(new CategoryTermQueryPart(getTerm(), getSortCategoryId()));

        if (Persistable.Base.isNotNullOrTransient(projectId)) {
            q.append(new ProjectIdLookupQueryPart(projectId));
        }

        appendIf(Persistable.Base.isNotNullOrTransient(collectionId), q, QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, collectionId);

        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }

        q.append(processReservedTerms(this));
        try {
            handleSearch(q);
//            if (getProjectionModel() != ProjectionModel.HIBERNATE_DEFAULT) {
//                setResults(getGenericService().populateSparseObjectsById(getResults(), Resource.class));
//            }
            getLogger().trace("jsonResults:" + getResults());
        } catch (ParseException e) {
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        jsonifyResult();
        return SUCCESS;
    }

    @Action(value = "keyword",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupKeyword() {
        // only return results if query length has enough characters
        if (!checkMinString(this.term) && !checkMinString(keywordType)) {
            return SUCCESS;
        }

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
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        jsonifyResult();
        return SUCCESS;
    }

    @Action(value = "annotationkey",
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupAnnotationKey() {
        QueryBuilder q = new ResourceAnnotationKeyQueryBuilder();
        setMinLookupLength(2);
        setMode("annotationLookup");

        this.setLookupSource(LookupSource.KEYWORD);
        getLogger().trace("looking up:'" + term + "'");

        // only return results if query length has enough characters
        if (checkMinString(term)) {
            addQuotedEscapedField(q, "annotationkey_auto", term);
            try {
                handleSearch(q);
            } catch (ParseException e) {
                addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
                return ERROR;
            }
        }

        jsonifyResult();
        return SUCCESS;
    }

    @Action(value = "collection",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupResourceCollection() {
        QueryBuilder q = new ResourceCollectionQueryBuilder();
        setMinLookupLength(0);

        this.setLookupSource(LookupSource.COLLECTION);
        getLogger().trace("looking up:'" + term + "'");
        setMode("collectionLookup");

        // only return results if query length has enough characters
        if (checkMinString(term)) {
            q.append(new AutocompleteTitleQueryPart(getTerm()));
            boolean admin = false;
            if (getAuthenticationAndAuthorizationService().can(InternalTdarRights.VIEW_ANYTHING, getAuthenticatedUser())) {
                admin = true;
            }
            CollectionAccessQueryPart queryPart = new CollectionAccessQueryPart(getAuthenticatedUser(), admin, getPermission());
            q.append(queryPart);
            try {
                handleSearch(q);
            } catch (ParseException e) {
                addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
                return ERROR;
            }
        }

        jsonifyResult();
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

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
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

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
    }
}
