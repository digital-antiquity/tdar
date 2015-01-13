package org.tdar.struts.action.lookup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.index.analyzer.SiteCodeTokenizingAnalyzer;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.KeywordQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.data.FacetGroup;
import org.tdar.utils.json.JsonLookupFilter;

/**
 * $Id$
 * <p>
 * @version $Rev$
 */
@Namespace("/lookup")
@ParentPackage("default")
@Component
@Scope("prototype")
public class KeywordLookupAction extends AbstractLookupController<Keyword> {

    private static final long serialVersionUID = 1614951978147004418L;


    @Autowired
    private transient AuthorizationService authorizationService;


    private String keywordType;
    private String term;

    @Action(value = "keyword",results = {
                    @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
            })
    public String lookupKeyword() {
        // only return results if query length has enough characters
        getLogger().trace("term: {} , minLength: {}", getTerm(), getMinLookupLength());
        setLookupSource(LookupSource.KEYWORD);

        if (StringUtils.isBlank(getKeywordType())) {
            addActionError(getText("lookupController.specify_keyword_type"));
            jsonifyResult(JsonLookupFilter.class);
            return ERROR;
        }

        if (!checkMinString(getTerm())) {
            setResults(new ArrayList<Keyword>());
            jsonifyResult(JsonLookupFilter.class);
            getLogger().debug("returning ... too short?" + getTerm());
            return SUCCESS;
        }
        
        
        QueryPartGroup subgroup = new QueryPartGroup(Operator.OR);
        if (StringUtils.equalsIgnoreCase(SiteNameKeyword.class.getSimpleName(), keywordType)) {
            if (StringUtils.isNotBlank(getTerm()) && SiteCodeTokenizingAnalyzer.pattern.matcher(getTerm()).matches()) {
                FieldQueryPart<String> siteCodePart = new FieldQueryPart<String>(QueryFieldNames.SITE_CODE, getTerm());
                siteCodePart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
                siteCodePart.setDisplayName(getText("searchParameters.site_code"));
                subgroup.append(siteCodePart.setBoost(5f));
            }

        }

        QueryBuilder q = new KeywordQueryBuilder(Operator.AND);
        QueryPartGroup group = new QueryPartGroup();

        group.setOperator(Operator.AND);
        addQuotedEscapedField(group, "label_auto", getTerm());

        // refine search to the correct keyword type
        group.append(new FieldQueryPart<String>("keywordType", getKeywordType()));
        setMode("keywordLookup");
        subgroup.append(group);
        q.append(subgroup);
        q.append(new FieldQueryPart<Status>("status", Status.ACTIVE));
        try {
            handleSearch(q);
        } catch (ParseException e) {
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        jsonifyResult(JsonLookupFilter.class);
        return SUCCESS;
    }


    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
    }


    public String getKeywordType() {
        return keywordType;
    }


    public void setKeywordType(String keywordType) {
        this.keywordType = keywordType;
    }


    public String getTerm() {
        return term;
    }


    public void setTerm(String term) {
        this.term = term;
    }


}
