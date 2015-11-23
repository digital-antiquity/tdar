package org.tdar.search.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.index.analyzer.SiteCodeTokenizingAnalyzer;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.KeywordQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryPartGroup;

import com.opensymphony.xwork2.TextProvider;

@Service
@Transactional
public class KeywordSearchService<I extends Indexable> {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SearchService searchService;


    public KeywordQueryBuilder findKeyword(String term, String keywordType, TextProvider provider, int min) {
        QueryPartGroup subgroup = new QueryPartGroup(Operator.OR);
        if (StringUtils.equalsIgnoreCase(SiteNameKeyword.class.getSimpleName(), keywordType)) {
            if (StringUtils.isNotBlank(term) && SiteCodeTokenizingAnalyzer.pattern.matcher(term).matches()) {
                FieldQueryPart<String> siteCodePart = new FieldQueryPart<String>(QueryFieldNames.SITE_CODE, term);
                siteCodePart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
                siteCodePart.setDisplayName(provider.getText("searchParameters.site_code"));
                subgroup.append(siteCodePart.setBoost(5f));
            }

        }

        KeywordQueryBuilder q = new KeywordQueryBuilder(Operator.AND);
        QueryPartGroup group = new QueryPartGroup();

        group.setOperator(Operator.AND);
        if (SearchUtils.checkMinString(term,min)) {
            FieldQueryPart<String> fqp = new FieldQueryPart<String>(QueryFieldNames.NAME_AUTOCOMPLETE, StringUtils.trim(term));
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            q.append(fqp);
        }

        // refine search to the correct keyword type
        group.append(new FieldQueryPart<String>(QueryFieldNames.TYPE, keywordType));
        subgroup.append(group);
        q.append(subgroup);
        q.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
        return q;
    }

}
