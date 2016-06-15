package org.tdar.search.service.query;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.index.analyzer.SiteCodeExtractor;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.KeywordQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.query.part.StringAutocompletePart;
import org.tdar.search.service.SearchUtils;

import com.opensymphony.xwork2.TextProvider;

@Service
@Transactional
public class KeywordSearchService<I extends Keyword> extends AbstractSearchService {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SearchService<I> searchService;

    public LuceneSearchResultHandler<I> findKeyword(String term, String keywordType, LuceneSearchResultHandler<I> result, TextProvider provider, int min)
            throws ParseException, SolrServerException, IOException {
        QueryPartGroup subgroup = new QueryPartGroup(Operator.OR);
        
        if (StringUtils.equalsIgnoreCase(SiteNameKeyword.class.getSimpleName(), keywordType)) {
            if (StringUtils.isNotBlank(term) && SiteCodeExtractor.matches(term)) {
                FieldQueryPart<String> siteCodePart = new FieldQueryPart<String>(QueryFieldNames.SITE_CODE, term);
                siteCodePart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
                siteCodePart.setDisplayName(provider.getText("searchParameters.site_code"));
                subgroup.append(siteCodePart.setBoost(5f));
            }

        }

        KeywordQueryBuilder q = new KeywordQueryBuilder(Operator.AND);
        QueryPartGroup group = new QueryPartGroup();

        group.setOperator(Operator.AND);
        if (SearchUtils.checkMinString(term, min)) {
            q.append(new StringAutocompletePart(QueryFieldNames.NAME_AUTOCOMPLETE, Arrays.asList(term)));
       }

        // refine search to the correct keyword type
        group.append(new FieldQueryPart<String>(QueryFieldNames.TYPE, keywordType));
        subgroup.append(group);
        q.append(subgroup);
        q.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
        searchService.handleSearch(q, result, provider);
        return result;
    }

}
