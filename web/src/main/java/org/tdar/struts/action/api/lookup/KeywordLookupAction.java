package org.tdar.struts.action.api.lookup;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.SearchUtils;
import org.tdar.search.service.query.KeywordSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.utils.json.JsonLookupFilter;

/**
 * $Id$
 * <p>
 * 
 * @version $Rev$
 */
@Namespace("/api/lookup")
@ParentPackage("default")
@Component
@Scope("prototype")
public class KeywordLookupAction extends AbstractLookupController<Keyword> {

    private static final long serialVersionUID = 1614951978147004418L;

    private String keywordType;
    private String term;
    
    @Autowired
    KeywordSearchService<Keyword> keywordSearchService;

    @Action(value = "keyword", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
    })
    public String lookupKeyword() throws SolrServerException, IOException {
        // only return results if query length has enough characters
        getLogger().trace("term: {} , minLength: {}", getTerm(), getMinLookupLength());
        setLookupSource(LookupSource.KEYWORD);

        if (StringUtils.isBlank(getKeywordType())) {
            addActionError(getText("lookupController.specify_keyword_type"));
            jsonifyResult(JsonLookupFilter.class);
            return ERROR;
        }

        if (!SearchUtils.checkMinString(getTerm(), getMinLookupLength())) {
            setResults(new ArrayList<Keyword>());
            jsonifyResult(JsonLookupFilter.class);
            getLogger().trace("returning ... too short?" + getTerm());
            return SUCCESS;
        }

        setMode("keywordLookup");
        try {
            keywordSearchService.findKeyword(getTerm(), getKeywordType(), this, this, getMinLookupLength());
        } catch (ParseException e) {
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        jsonifyResult(JsonLookupFilter.class);
        return SUCCESS;
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
