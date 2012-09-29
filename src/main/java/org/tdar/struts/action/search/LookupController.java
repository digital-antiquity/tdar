package org.tdar.struts.action.search;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.queryBuilder.InstitutionQueryBuilder;
import org.tdar.search.query.queryBuilder.KeywordQueryBuilder;
import org.tdar.search.query.queryBuilder.PersonQueryBuilder;
import org.tdar.search.query.queryBuilder.QueryBuilder;
import org.tdar.search.query.queryBuilder.ResourceAnnotationKeyQueryBuilder;
import org.tdar.search.query.queryBuilder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.queryBuilder.ResourceQueryBuilder;

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

    private String keywordType;
    private String term;

    // this defines what we are looking up (people, institutions, etc)
    // FIXME: lookupSource really should be an enum or const.
    private String lookupSource;

    private Long sortCategoryId;

    @Action(value = "person",
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupPerson() {
        QueryBuilder q = new PersonQueryBuilder();

        this.lookupSource = "people";
        setMode("personLookup");
        addEscapedWildcardField(q, "firstName", firstName);
        addEscapedWildcardField(q, "lastName", lastName);
        addEscapedWildcardField(q, "email", email);

        if (checkMinString(institution) && StringUtils.isNotBlank(institution)) {
            FieldQueryPart fqp = new FieldQueryPart("institution.name_auto", institution);
            if (institution.contains(" ")) {
                fqp.setQuotedEscapeValue(institution);
            }
            q.append(fqp);
        }

        if (!q.isEmpty() || getMinLookupLength() == 0) {
            if (StringUtils.isNotBlank(registered)) {
                try {
                    Boolean.parseBoolean(registered);
                    q.append(new FieldQueryPart("registered", registered));
                } catch (Exception e) {
                    addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                    return ERROR;
                }
            }

            try {
                handleSearch(q);
            } catch (ParseException e) {
                addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                return ERROR;
            }
        }
        return SUCCESS;
    }

    @Action(value = "institution",
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupInstitution() {
        QueryBuilder q = new InstitutionQueryBuilder();
        setMode("institutionLookup");

        this.lookupSource = "institutions";

        // only return results if query length has enough characters
        QueryPartGroup group = new QueryPartGroup();
        if (checkMinString(this.institution)) {
            group.setOperator(Operator.OR);

            // even if we allow zero-length strings, we dont want to append them to the queryBuilder. Just gimme all the institutions
            if (StringUtils.isNotBlank(institution)) {
                addQuotedEscapedField(group, "name_auto", institution);
                addQuotedEscapedField(group, "acronym", institution);
                q.append(group);
            }

            try {
                handleSearch(q);
            } catch (ParseException e) {
                addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                return ERROR;
            }
        }

        return SUCCESS;
    }

    @Action(value = "resource",
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupResource() {
        QueryBuilder q = new ResourceQueryBuilder();
        this.lookupSource = "resources";
        setMode("resourceLookup");

        addResourceTypeQueryPart(q, getResourceTypes());

        appendStatusInformation(q, null, getIncludedStatuses(), getAuthenticatedUser());

        try {
            appendIf(isUseSubmitterContext(), q, QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, getAuthenticatedUser().getId().toString());
        } catch (IllegalStateException e) {
            addActionError("you must be logged in");
        }
        QueryPartGroup valueGroup = new QueryPartGroup();
        addTitlePart(valueGroup, getTerm(), false);

        if (getSortCategoryId() != null && getSortCategoryId() > -1) {
            // SHOULD PREFER THINGS THAT HAVE THAT CATEGORY ID
            FieldQueryPart q2 = new FieldQueryPart(QueryFieldNames.CATEGORY_ID, getSortCategoryId().toString());
            q2.setBoost(5f);
            valueGroup.append(q2);
            valueGroup.setOperator(Operator.OR);
        }
        q.append(valueGroup);

        if (StringUtils.isNotBlank(projectId) && StringUtils.isNumeric(projectId)) {
            QueryPartGroup group = new QueryPartGroup();
            group.setOperator(Operator.OR);
            group.append(new FieldQueryPart(QueryFieldNames.PROJECT_ID, projectId));
            group.append(new FieldQueryPart(QueryFieldNames.ID, projectId));
            q.append(group);
        }

        appendIf(StringUtils.isNotBlank(collectionId) && StringUtils.isNumeric(collectionId), q, QueryFieldNames.RESOURCE_COLLECTION_PUBLIC_IDS, collectionId);

        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }
        try {
            handleSearch(q);
            logger.trace("jsonResults:" + getResults());
        } catch (ParseException e) {
            addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
            return ERROR;
        }

        return SUCCESS;
    }

    @Action(value = "keyword",
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupKeyword() {
        // only return results if query length has enough characters
        if (!checkMinString(this.term) && !checkMinString(keywordType))
            return SUCCESS;

        QueryBuilder q = new KeywordQueryBuilder();
        this.lookupSource = "items";
        QueryPartGroup group = new QueryPartGroup();

        group.setOperator(Operator.AND);
        addQuotedEscapedField(group, "label_auto", term);

        // refine search to the correct keyword type
        group.append(new FieldQueryPart("keywordType", keywordType));
        setMode("keywordLookup");

        q.append(group);
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

        this.lookupSource = "items";
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
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupResourceCollection() {
        QueryBuilder q = new ResourceCollectionQueryBuilder();
        setMinLookupLength(2);

        this.lookupSource = "collections";
        logger.trace("looking up:'" + term + "'");
        setMode("collectionLookup");

        // only return results if query length has enough characters
        if (checkMinString(term)) {
            addQuotedEscapedField(q, QueryFieldNames.COLLECTION_NAME_AUTO, term);
            FieldQueryPart fqp = new FieldQueryPart(QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED);
            q.append(fqp);
            try {
                appendIf(isUseSubmitterContext(), q, QueryFieldNames.COLLECTION_USERS_WHO_CAN_MODIFY, getAuthenticatedUser().getId().toString());
            } catch (IllegalStateException e) {
                addActionError("you must be logged in");
            }

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
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    // lookupSource defines the type of records contained in the jsonResults collection. To be
    // used in a ftl template /give hints to client-side javascript
    public String getLookupSource() {
        return lookupSource;
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
        this.term = term;
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

}
