package org.tdar.struts.action;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Query;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.search.FullTextQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.JsonModel;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.InstitutionQueryBuilder;
import org.tdar.search.query.KeywordQueryBuilder;
import org.tdar.search.query.PersonQueryBuilder;
import org.tdar.search.query.QueryBuilder;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.ResourceAnnotationKeyQueryBuilder;
import org.tdar.search.query.ResourceQueryBuilder;

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
public class LookupController extends TdarActionSupport {

    private static final long serialVersionUID = 176288602101059922L;

    private String firstName;
    private String lastName;
    private String institution;
    private String email;
    private String callback;
    private String registered;
    private int minLookupLength = 3;
    private int recordsPerPage = 10;
    private int startRecord = 0;
    private String sortField;
    private String url;

    private Long projectId;
    private String title;
    private String resourceType;

    private String keywordType;
    private String term;

    // this defines what we are looking up (people, institutions, etc)
    // FIXME: lookupSource really should be an enum or const.
    private String lookupSource;

    private List<JsonModel> jsonResults = Collections.emptyList();
    private int totalRecords;

    private String escape(String str) {
        // trim and escape input
        String escaped = str.trim();
        escaped = QueryParser.escape(escaped);
        return escaped;
    }

    private String escapeWild(String str) {
        return escape(str) + "*";
    }

    @Action(value = "person",
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupPerson() {
        QueryBuilder q = new PersonQueryBuilder();
        boolean any = false;
        // int totalRecords = 0;

        this.lookupSource = "people";

        if (checkMinString(firstName)) {
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("firstName");

            fqp.setFieldValue(escapeWild(firstName));
            q.append(fqp);
            any = true;
        }

        if (checkMinString(lastName)) {
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("lastName");
            getLogger().info("lastName:" + lastName);
            fqp.setFieldValue(escapeWild(lastName));
            q.append(fqp);
            any = true;
        }

        if (checkMinString(email)) {
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("email");
            fqp.setFieldValue(escapeWild(email));
            q.append(fqp);
            any = true;
        }

        if (checkMinString(institution)) {
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("institution.name_auto");
            if (institution.contains(" ")) {
                fqp.setFieldValue("\"" + escape(institution) +"\"");
            } else {
                fqp.setFieldValue(escape(institution));
            }
            q.append(fqp);
            any = true;
        }

        if (StringUtils.isNotEmpty(registered)) {
            FieldQueryPart fqp = new FieldQueryPart();
            try {
                Boolean.parseBoolean(registered);
                fqp.setFieldName("registered");
                fqp.setFieldValue(registered);
                q.append(fqp);
            } catch (Exception e) {
                addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                return ERROR;
            }
        }
        logger.info("{}",q);

        if (any) {
            try {
                jsonResults = handleSearch(q);
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

        this.lookupSource = "institutions";

        // only return results if query length has enough characters
        this.institution = escape(institution);
        QueryPartGroup group = new QueryPartGroup();
        FieldQueryPart fqp = new FieldQueryPart();
        if (checkMinString(this.institution)) {
            group.setOperator(Operator.OR);
            fqp.setFieldName("name_auto");
            fqp.setFieldValue("\"" + this.institution + "\"");
            group.addPart(fqp);

            fqp = new FieldQueryPart();
            fqp.setFieldName("acronym");
            fqp.setFieldValue(this.institution);
            group.addPart(fqp);

            q.append(group);
            try {
                setSortField("name");
                jsonResults = handleSearch(q);
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
        boolean any = false;
        this.lookupSource = "resource";

        if (checkMinString(resourceType)) {
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("resourceType");
            fqp.setFieldValue(escape(resourceType));
            q.append(fqp);
            any = true;
        }

        if (checkMinString(title)) {
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("title");
            fqp.setFieldValue(escapeWild(title));
            q.append(fqp);
            any = true;
        }

        if (projectId != null) {
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("projectId");
            fqp.setFieldValue(projectId.toString());
            q.append(fqp);
            any = true;
        }

        if (any) {
            try {
                jsonResults = handleSearch(q);
                logger.debug("jsonResults:" + jsonResults);
            } catch (ParseException e) {
                addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                return ERROR;
            }
        }

        return SUCCESS;
    }

    @Action(value = "keyword",
            results = { @Result(name = "success", location = "lookup.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String lookupKeyword() {
        // only return results if query length has enough characters
        if(!checkMinString(this.term) && !checkMinString(keywordType)) return SUCCESS;
        
        QueryBuilder q = new KeywordQueryBuilder();
        this.lookupSource = "items";
        QueryPartGroup group = new QueryPartGroup();
        
        group.setOperator(Operator.AND);
        FieldQueryPart fqp = new FieldQueryPart("label_auto", "\"" + escape(this.term) + "\"");
        group.addPart(fqp);

        //refine search to the correct keyword type
        fqp = new FieldQueryPart("keywordType", keywordType);
        group.addPart(fqp);

        q.append(group);
        try {
            setSortField("label");
            jsonResults = handleSearch(q);
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
        minLookupLength = 2;

        this.lookupSource = "items";
        logger.debug("looking up:'" + term + "'");

        // only return results if query length has enough characters
        if (checkMinString(term)) {
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("annotationkey_auto");
            fqp.setFieldValue("\"" + escape(term) + "\"");
            q.append(fqp);
            try {
                jsonResults = handleSearch(q);
            } catch (ParseException e) {
                addActionErrorWithException("Invalid query syntax, please try using simpler terms without special characters.", e);
                return ERROR;
            }
        }

        return SUCCESS;
    }

    @SuppressWarnings("unchecked")
    public List<JsonModel> handleSearch(QueryBuilder q) throws ParseException {
        Query query = q.buildQuery();
        Class<?>[] classes = q.getClasses();
        FullTextQuery ftq = getSearchService().search(query, sortField, classes);
        logger.info("running query: " + ftq.getQueryString());
        totalRecords = ftq.getResultSize();
        ftq.setFirstResult(startRecord);
        ftq.setMaxResults(recordsPerPage);
        logger.info("query results count:" + totalRecords);
        return ftq.list();
    }

    public boolean checkMinString(String look) {
        return (!StringUtils.isEmpty(look) && look.length() >= minLookupLength);
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

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getRegistered() {
        return registered;
    }

    public void setRegistered(String registered) {
        this.registered = registered;
    }

    public int getMinLookupLength() {
        return minLookupLength;
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

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
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

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public List<JsonModel> getJsonResults() {
        return jsonResults;
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

}
