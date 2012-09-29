package org.tdar.struts.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.hibernate.search.FullTextQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.FullUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.FreetextQueryPart;
import org.tdar.search.query.QueryBuilder;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.ResourceQueryBuilder;
import org.tdar.search.query.ResourceTypeQueryPart;
import org.tdar.struts.action.AuthenticationAware.Base;

/**
 * 
 * $Id$
 * 
 * Provides an endpoint for <a href="http://www.datatables.net/">jQuery
 * datatables</a>.
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
@Namespace("/project")
@ParentPackage("secured")
@Component
@Scope("prototype")
@Results({ @Result(name = "success", type = "stream", params = { "contentType",
        "application/json", "inputName", "jsonStream" }) })
public class ProjectDatatableController extends Base {

    private static final long serialVersionUID = 6345765580908934870L;
    private transient InputStream jsonStream;
    private int iDisplayStart;
    private int iDisplayLength;
    private int sEcho;
    private String sSearch;
    private Long projectId;
    private List<String> types;
    private int iSortCol_0;
    private String callback;
    private String sSortDir_0;

    private static Map<Integer, String> sortMap;

    static {
        sortMap = new HashMap<Integer, String>();
        sortMap.put(0, "id");
        sortMap.put(1, "title_sort");
        sortMap.put(2, "resourceType");
    }

    @SuppressWarnings("unchecked")
    @Action("datatable")
    public String execute() {

        JSONObject json = new JSONObject();
        json.element("sEcho", sEcho); // supply back the verification string
        JSONArray rows = new JSONArray();
        QueryBuilder qb = new ResourceQueryBuilder();
        QueryPartGroup group = new QueryPartGroup();
        group.setOperator(Operator.OR);
        // if a projectId is specified
        if (projectId != null) {
            // query for project
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("projectId");
            fqp.setFieldValue(projectId.toString());
            group.addPart(fqp);
        } else {
            // otherwise if projects exist for user add all of those ids, plus anything the user owns
            for (Project p : getProjectService().findSparseTitleIdProjectListByPerson(getAuthenticatedUser())) {
                FieldQueryPart fqp = new FieldQueryPart();
                fqp.setFieldName("projectId");
                fqp.setFieldValue(p.getId().toString());
                group.addPart(fqp);
            }
            FieldQueryPart fqp = new FieldQueryPart();
            fqp.setFieldName("submitter.id");
            fqp.setFieldValue(getAuthenticatedUser().getId().toString());
            group.addPart(fqp);
        }

        qb.append(group);

        // query for free text -- "or" supplied terms
        if (StringUtils.isNotBlank(sSearch)) {
            FreetextQueryPart query = new FreetextQueryPart();
            for (String term : StringUtils.split(sSearch)) {
                query.setQueryString(term.trim().concat("*"));
            }
            qb.append(query);
        }

        if (types != null) {
            ResourceTypeQueryPart rtq = new ResourceTypeQueryPart();
            for (String type : types) {
                ResourceType rt = ResourceType.valueOf(type);
                rtq.addResourceTypeLimit(rt);
            }
            qb.append(rtq);
        }

        QueryPartGroup statusGroup = new QueryPartGroup();
        statusGroup.setOperator(Operator.OR);
        statusGroup.addPart(new FieldQueryPart("status", Status.ACTIVE.toString()));
        statusGroup.addPart(new FieldQueryPart("status", Status.DRAFT.toString()));
        qb.append(statusGroup);
        boolean revSort = false;
        if ("desc".equalsIgnoreCase(sSortDir_0)) {
            revSort = true;
        }

        String sortField = sortMap.get(iSortCol_0);

        List<InformationResource> results = new ArrayList<InformationResource>();

        try {
            FullTextQuery ftq = getSearchService().search(qb, sortField, revSort);

            logger.info("running query: " + ftq.getQueryString());

            ftq.setFirstResult(iDisplayStart);
            ftq.setMaxResults(iDisplayLength);

            int totalRecords = ftq.getResultSize();
            results = (List<InformationResource>) ftq.list();

            json.element("iTotalRecords", totalRecords).element(
                    "iTotalDisplayRecords", totalRecords);
        } catch (ParseException e1) {
            getLogger()
                    .warn("There was a problem parsing the lucene query", e1);
        }
        logger.debug("returing " + results.size() + " items for list");

        for (InformationResource res : results) {
            String titleLink = String.format("<a href=\"%s\">%s</a>",
                    getUrlService().relativeUrl(res), res.getTitle());
            logger.trace(titleLink);
            String projectText = "";
            if (projectId == null) {
                projectText = " (" + res.getProject().getTitle() + ")";
            }
            boolean canEdit = false;
            if (res.getSubmitter().equals(getSessionData().getPerson()) || isAdministrator()) {
                canEdit = true;
            } else {
                for (FullUser u : res.getFullUsers()) {
                    if (u.getPerson().equals(getSessionData().getPerson()))
                        canEdit = true;
                }
            }
            String menuText = "";
            if (canEdit) {
                menuText += String.format("<a href=\"%s/edit\">%s</a> | ",
                        getUrlService().relativeUrl(res), "edit")
                        + String.format(
                                "<a href=\"%s/delete\">%s</a> ",
                                getUrlService().relativeUrl(res), "delete");
            }

            String statusInfo = "";
            if (res.getStatus() == Status.DELETED)
                statusInfo = " [DELETED]";
            rows.add(new JSONArray()
                    .element(res.getId())
                    .element(
                            "<h5>" + titleLink + projectText + "</h5>"
                                    + "<p>" + res.getShortenedDescription()
                                    + statusInfo
                                    + "</p>" + menuText)
                    .element(res.getResourceType().getLabel()));
        }

        json.element("aaData", rows);
        StringBuilder content = new StringBuilder();
        if (!StringUtils.isEmpty(getCallback()))
            content.append(getCallback()).append("(");
        content.append(json.toString());
        if (!StringUtils.isEmpty(getCallback()))
            content.append(");");
        try {
            jsonStream = new ByteArrayInputStream(content.toString().getBytes(
                    "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            getLogger()
                    .warn("We live in an odd world where Java does not understand UTF-8",
                            e);
        }
        return SUCCESS;
    }

    public void setIDisplayStart(int iDisplayStart) {
        this.iDisplayStart = iDisplayStart;
    }

    public void setIDisplayLength(int iDisplayLength) {
        this.iDisplayLength = iDisplayLength;
    }

    public void setSEcho(int sEcho) {
        this.sEcho = sEcho;
    }

    public void setSSearch(String sSearch) {
        this.sSearch = sSearch;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public InputStream getJsonStream() {
        return jsonStream;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setISortCol_0(int iSortCol_0) {
        this.iSortCol_0 = iSortCol_0;
    }

    public void setSSortDir_0(String sSortDir_0) {
        this.sSortDir_0 = sSortDir_0;
    }

    /**
     * @param callback
     *            the callback to set
     */
    public void setCallback(String callback) {
        this.callback = callback;
    }

    /**
     * @return the callback
     */
    public String getCallback() {
        return callback;
    }

}
