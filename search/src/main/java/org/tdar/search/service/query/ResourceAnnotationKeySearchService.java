package org.tdar.search.service.query;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.builder.ResourceAnnotationKeyQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.service.SearchUtils;

import com.opensymphony.xwork2.TextProvider;

@Service
public class ResourceAnnotationKeySearchService extends AbstractSearchService {

    @Autowired
    private SearchService<ResourceAnnotationKey> searchService;

    public SearchResultHandler<ResourceAnnotationKey> buildAnnotationSearch(String term, LuceneSearchResultHandler<ResourceAnnotationKey> result, int min,
            TextProvider provider) throws ParseException, SolrServerException, IOException {
        ResourceAnnotationKeyQueryBuilder q = new ResourceAnnotationKeyQueryBuilder();

        // only return results if query length has enough characters
        if (SearchUtils.checkMinString(term, min)) {
            FieldQueryPart<String> fqp = new FieldQueryPart<>(QueryFieldNames.NAME_AUTOCOMPLETE, term);
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
            q.append(fqp);
        }

        searchService.handleSearch(q, result, provider);
        return result;

    }

}
