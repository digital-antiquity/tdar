package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.SiteCodeExtractor;


public class KeywordDocumentConverter extends AbstractSolrDocumentConverter {

    /*
     * See solr/configsets/default/conf/keywords-schema.xml
     */

    public static SolrInputDocument convert(Keyword kwd) {
        
        SolrInputDocument doc = convertPersistable(kwd);
        List<String> names = new ArrayList<>();
        String label = kwd.getLabel();
        names.add(label);
        doc.setField(QueryFieldNames.GENERAL_TYPE, kwd.getKeywordType());
        if (kwd instanceof HierarchicalKeyword<?>) {
            HierarchicalKeyword<?> hk = (HierarchicalKeyword<?>)kwd;
            CollectionUtils.addAll(names, hk.getParentLabelList());
        }
        
        if ((kwd instanceof SiteNameKeyword || kwd instanceof OtherKeyword) && SiteCodeExtractor.matches(label)) {
            doc.setField(QueryFieldNames.SITE_CODE, SiteCodeExtractor.extractSiteCodeTokens(label,true));
        }
        
        doc.setField(QueryFieldNames.NAME, names);
        doc.setField(QueryFieldNames.NAME_SORT, label);
        return doc;
    }
}
