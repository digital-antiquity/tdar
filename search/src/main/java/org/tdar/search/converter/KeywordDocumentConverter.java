package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.search.query.QueryFieldNames;


public class KeywordDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(Keyword kwd) {
        
        SolrInputDocument doc = convertPersistable(kwd);
        List<String> names = new ArrayList<>();
        names.add(kwd.getLabel());
        doc.setField(QueryFieldNames.NAME_AUTOCOMPLETE, kwd.getLabel());
        doc.setField(QueryFieldNames.TYPE, kwd.getKeywordType());
        if (kwd instanceof HierarchicalKeyword<?>) {
            HierarchicalKeyword<?> hk = (HierarchicalKeyword<?>)kwd;
            CollectionUtils.addAll(names, hk.getParentLabelList());
        }
        doc.setField(QueryFieldNames.NAME, names);
        doc.setField(QueryFieldNames.NAME_SORT, kwd.getLabel());
        return doc;
    }
}
