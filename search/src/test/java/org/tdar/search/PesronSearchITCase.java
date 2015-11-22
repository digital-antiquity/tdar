package org.tdar.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.builder.PersonQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.GeneralCreatorQueryPart;
import org.tdar.search.query.part.PersonQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.service.SearchService;
import org.tdar.utils.MessageHelper;

public class PesronSearchITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    SearchService searchService;
    
    @Test
    public void testPersonRelevancy() throws ParseException, SolrServerException, IOException {
        List<Person> people = new ArrayList<>();
        Person whelan = new Person("Mary", "Whelan", null);
        people.add(whelan);
        Person mmc = new Person("Mary", "McCready", null);
        Person mmc2 = new Person("McCready", "Mary", null);
        people.add(mmc);
        people.add(new Person("Doug", "Mary", null));
        people.add(mmc2);
        people.add(new Person("Mary", "Robbins-Wade", null));
        people.add(new Person("Robbins-Wade", "Mary", null));
        updateAndIndex(people);
        SearchResult result = buildQuery("Whelan Mary");
        assertTrue(result.getResults().get(0).equals(whelan));
        assertTrue(result.getResults().size() == 1);

        result = buildQuery("Mary McCready");
        logger.debug("Results: {}", "Mary McCready");
        assertTrue(result.getResults().contains(mmc));
        assertTrue(result.getResults().contains(mmc2));
        assertTrue(result.getResults().size() == 2);
    }

    private SearchResult buildQuery(String name) throws ParseException, SolrServerException, IOException {
        PersonQueryBuilder qb = new PersonQueryBuilder();
        PersonQueryPart pqp = new PersonQueryPart();
        qb.append(pqp);
        Person transnt = Person.fromName(name);
        pqp.add(transnt);
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Arrays.asList(Status.ACTIVE)));
        group.append(new GeneralCreatorQueryPart(transnt));
        qb.append(group);
        SearchResult result = new SearchResult();
        searchService.handleSearch(qb, result, MessageHelper.getInstance());
        return result;
    }

    private void updateAndIndex(Collection<? extends Indexable> docs) throws SolrServerException, IOException {
        genericService.saveOrUpdate(docs);
        searchIndexService.index(docs.toArray(new Indexable[0]));
    }
}
