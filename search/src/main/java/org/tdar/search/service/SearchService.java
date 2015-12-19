 package org.tdar.search.service;

 import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.client.solrj.SolrServerException;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.DeHydratable;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.SearchPaginationException;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.dao.SearchDao;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.builder.InstitutionQueryBuilder;
import org.tdar.search.query.builder.PersonQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.AbstractHydrateableQueryPart;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.InstitutionAutocompleteQueryPart;
import org.tdar.search.query.part.PersonQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryGroup;
import org.tdar.search.query.part.QueryPart;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.range.DateRange;

import com.opensymphony.xwork2.TextProvider;

 @Service
 @Transactional
 public class SearchService<I extends Indexable> extends AbstractSearchService {

     private final GenericService genericService;

     private final SearchDao<I> searchDao;


     @Autowired
     public SearchService(SessionFactory sessionFactory, GenericService genericDao, 
             AuthenticationService authenticationService, AuthorizationService authorizationService, SearchDao<I> searchDao) {
         this.genericService = genericDao;
         this.searchDao = searchDao;
         this.authenticationService = authenticationService;
         this.authorizationService = authorizationService;
     }


     /**
      * Perform a search based on the @link QueryBuilder and @link SortOption array.
      *
      * @param queryBuilder
      * @param sortOptions
      * @return
      * @throws ParseException
     * @throws IOException 
     * @throws SolrServerException 
      */
     protected SolrSearchObject<I> constructSolrSearch(QueryBuilder queryBuilder, SearchResultHandler<I> handler, SortOption... sortOptions) throws ParseException, SolrServerException, IOException {
         return searchDao.search(new SolrSearchObject<I>(queryBuilder, sortOptions, handler), handler);
     } 

     /**
      * This method actually handles the Lucene search, passed in from the Query Builder and sets the results
      * on the results handler
      *
      * @param q
      * @param resultHandler
      * @throws ParseException
     * @throws IOException 
     * @throws SolrServerException 
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
     protected void handleSearch(QueryBuilder q, SearchResultHandler resultHandler, TextProvider textProvider) throws ParseException, SolrServerException, IOException {
         if (q.isEmpty()) {
             q.append(new FieldQueryPart<>("*","*"));;
         }
         long num = System.currentTimeMillis();
         hydrateQueryParts(q);
         SolrSearchObject<I> ftq = constructSolrSearch(q, resultHandler, resultHandler.getSortField(), resultHandler.getSecondarySortField());

         resultHandler.setTotalRecords(ftq.getTotalResults());
         long lucene = System.currentTimeMillis() - num;
         num = System.currentTimeMillis();
         logger.trace("begin adding facets");
         searchDao.processFacets(ftq, resultHandler);
         logger.trace("completed adding facets");
         logger.trace("completed hibernate hydration ");
         String queryText = ftq.getQueryString();
         logger.debug(queryText);
         Object searchMetadata[] = { resultHandler.getMode(), StringUtils.left(queryText, 100), resultHandler.getSortField(), resultHandler.getSecondarySortField(),
                 lucene, (System.currentTimeMillis() - num),
                 ftq.getTotalResults(),
                 resultHandler.getStartRecord() };
         logger.trace("query: {} ", queryText);
         logger.debug("{}: {} (SORT:{},{})\t LUCENE: {} | HYDRATION: {} | # RESULTS: {} | START #: {}", searchMetadata);
         resultHandler.setSearchTitle(q.getDescription(textProvider));
         if (resultHandler.getStartRecord() > ftq.getResultSize()) {
             throw new SearchPaginationException(MessageHelper.getMessage("searchService.start_record_too_high", Arrays.asList(resultHandler.getStartRecord(),
                     ftq.getResultSize())));
         }
     }



     /**
      * For all of the HydratableQueryParts iterate through them and do a find by id... keep the same order
      *
      * @param q
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private <T extends Persistable> void hydrateQueryParts(QueryGroup q) {
         List<AbstractHydrateableQueryPart> partList = findAllHydratableParts(q);
         Map<Class, Set<T>> lookupMap = new HashMap<>();
         // iterate through all of the values and get them into a map of <class -> Set<Item,..>
         for (int i = 0; i < partList.size(); i++) {
             Class<T> cls = partList.get(i).getActualClass();
             if (lookupMap.get(cls) == null) {
                 lookupMap.put(cls, new HashSet<T>());
             }
             for (T fieldValue : (List<T>) partList.get(i).getFieldValues()) {
                 // T cast = (T) fieldValue;
                 if (PersistableUtils.isNotTransient(fieldValue)) {
                     lookupMap.get(cls).add(fieldValue);
                 } else {
                     logger.trace("not adding {} ", fieldValue);
                 }
             }
         }
         // load sparse entities, and put them into a map to look them back up by
         Map<Class, Map<Long, Persistable>> idLookupMap = new HashMap<>();
         for (Class<T> cls : lookupMap.keySet()) {
             List<T> hydrated = new ArrayList<>();
             if (DeHydratable.class.isAssignableFrom(cls)) {
                 hydrated = genericService.populateSparseObjectsById(new ArrayList<>(lookupMap.get(cls)), cls);
             } else {
                 hydrated = genericService.loadFromSparseEntities(lookupMap.get(cls), cls);
             }
             logger.trace("toLookup: {} {} result: {}", cls, lookupMap.get(cls), hydrated);

             idLookupMap.put(cls, (Map<Long, Persistable>) PersistableUtils.createIdMap(hydrated));
         }

         // repopulate
         for (int i = 0; i < partList.size(); i++) {
             AbstractHydrateableQueryPart part = partList.get(i);
             Class<T> cls = part.getActualClass();
             for (int j = 0; j < part.getFieldValues().size(); j++) {
                 T fieldValue = (T) part.getFieldValues().get(j);
                 if (PersistableUtils.isNotTransient(fieldValue)) {
                     part.getFieldValues().set(j, idLookupMap.get(cls).get(fieldValue.getId()));
                 } else {
                     logger.trace("not adding: {} ", idLookupMap.get(cls), fieldValue);
                 }
             }
             part.update();
             logger.trace("final result: {}", part);
         }
     }

     /**
      * Troll through the @link QueryGroup and find all @link AbstractHydrateableQueryPart entries that may need hydration
      *
      * @param q
      * @return
      */
     @SuppressWarnings("rawtypes")
     private List<AbstractHydrateableQueryPart> findAllHydratableParts(QueryGroup q) {
         List<AbstractHydrateableQueryPart> partList = new ArrayList<>();
         for (QueryPart part : q.getParts()) {
             if (part instanceof QueryGroup) {
                 partList.addAll(findAllHydratableParts((QueryGroup) part));
             }
             if (part instanceof AbstractHydrateableQueryPart) {
                 partList.add((AbstractHydrateableQueryPart) part);
             }
         }
         return partList;
     }


     /**
      * remove unauthorized statuses from list. it's up to caller to handle implications of empty list
      *
      * @param statusList
      * @param user
      */
     public void filterStatusList(List<Status> statusList, TdarUser user) {
         authorizationService.removeIfNotAllowed(statusList, Status.DELETED, InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, user);
         authorizationService.removeIfNotAllowed(statusList, Status.FLAGGED, InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, user);
         authorizationService.removeIfNotAllowed(statusList, Status.DRAFT, InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS, user);
     }

     /**
      * Replace AND/OR with lowercase so that lucene does not interpret them as operaters.
      * It is not necessary sanitized quoted strings.
      *
      * @param unsafeQuery
      * @return
      */
     public String sanitize(String unsafeQuery) {
         Matcher m = luceneSantizeQueryPattern.matcher(unsafeQuery);
         return m.replaceAll("$1\\\\$2$3");
     }

     /**
      * Take any of the @link SearchParameter properties that can support skeleton resources and inflate them so we can display something in the search title /
      * description that isn't just creatorId=4
      *
      * @param searchParameters
      */
     public void inflateSearchParameters(SearchParameters searchParameters) {
         // FIXME: refactor to ue genericService.populateSparseObjectsById() which optimizes the qeries to the DB
         // Also, consider moving into genericService
         List<List<? extends Persistable>> lists = searchParameters.getSparseLists();
         for (List<? extends Persistable> list : lists) {
             logger.debug("inflating list of sparse objects: {}", list);
             // making unchecked cast so compiler accepts call to set()
             @SuppressWarnings("unchecked")
             ListIterator<Persistable> itor = (ListIterator<Persistable>) list.listIterator();
             while (itor.hasNext()) {
                 Persistable sparse = itor.next();
                 if (sparse != null) {
                     Persistable persistable = genericService.find(sparse.getClass(), sparse.getId());
                     logger.debug("\t inflating {}({}) -> {}", sparse.getClass().getSimpleName(), sparse.getId(), persistable);
                     itor.set(persistable);
                 }
             }
         }
     }

     /**
      * Takes a set of @link ResourceCreator entities from the @link SearchParameters and resolves them in tDAR before doing an ID search in Lucene
      *
      * @param group
      * @param maxCreatorsToResolve
      * @throws ParseException
     * @throws IOException 
     * @throws SolrServerException 
      */
     public void updateResourceCreators(SearchParameters group, Integer maxCreatorsToResolve) throws ParseException, SolrServerException, IOException {
         logger.trace("updating proxies");
         int maxToResolve = 1000;
         if ((maxCreatorsToResolve != null) && (maxCreatorsToResolve > 0)) {
             maxToResolve = maxCreatorsToResolve;
         }
         Map<ResourceCreatorProxy, List<ResourceCreatorProxy>> replacements = new HashMap<>();
         List<ResourceCreatorProxy> proxies = group.getResourceCreatorProxies();
         for (ResourceCreatorProxy proxy : proxies) {
             if (proxy == null) {
                 continue;
             }
             ResourceCreator rc = proxy.getResourceCreator();
             if ((rc != null) && proxy.isValid()) {
                 ArrayList<ResourceCreatorProxy> values = new ArrayList<>();
                 Creator creator = rc.getCreator();
                 if (PersistableUtils.isTransient(creator)) {
                     resolveCreator(maxToResolve, replacements, proxy, rc, values, creator);
                 } else {
                     Creator find = genericService.find(Creator.class, creator.getId());
                     rc.setCreator(find);
                 }
             } else {
                 replacements.put(proxy, null);
             }
             logger.debug("{} -- {}", rc.getCreator(), rc.getCreator().getSynonyms());
         }
         for (ResourceCreatorProxy toReplace : replacements.keySet()) {
             proxies.remove(toReplace);
             List<ResourceCreatorProxy> values = replacements.get(toReplace);
             if (CollectionUtils.isNotEmpty(values)) {
                 proxies.addAll(values);
             }
         }
         logger.trace("result: {} ", proxies);
     }

     /**
      * Takes a Creator without an id and attempts to resolve it with as many matching creators as specified. This sort of creator comes from a search without an
      * AJAX backing on the advanced search area.
      *
      * @param maxToResolve
      * @param replacements
      * @param proxy
      * @param rc
      * @param values
      * @param creator
      * @throws ParseException
     * @throws IOException 
     * @throws SolrServerException 
      */
     @SuppressWarnings("unchecked")
     private void resolveCreator(int maxToResolve, Map<ResourceCreatorProxy, List<ResourceCreatorProxy>> replacements, ResourceCreatorProxy proxy,
             ResourceCreator rc, ArrayList<ResourceCreatorProxy> values, Creator creator) throws ParseException, SolrServerException, IOException {
         QueryBuilder q;
         replacements.put(proxy, values);
         if (creator instanceof Institution) {
             q = new InstitutionQueryBuilder();
             InstitutionAutocompleteQueryPart iqp = new InstitutionAutocompleteQueryPart();
             iqp.setPhraseFormatters(PhraseFormatter.WILDCARD, PhraseFormatter.QUOTED);
             iqp.add((Institution) creator);
             q.append(iqp);
         } else {
             q = new PersonQueryBuilder();
             PersonQueryPart pqp = new PersonQueryPart();
             pqp.setPhraseFormatters(PhraseFormatter.WILDCARD, PhraseFormatter.QUOTED);
             pqp.add((Person) creator);
             q.append(pqp);
         }

         q.append(new FieldQueryPart<>("status", Status.ACTIVE));
         List<Creator> list = null;
         logger.trace(q.generateQueryString());
         SearchResultHandler<I> handler = new SearchResult<>();
        SolrSearchObject<I> search = searchDao.search(new SolrSearchObject<I>(q, ((SortOption[]) null),handler), handler );
         search.setMaxResults(maxToResolve);
         list = (List<Creator>)search.getResultList();
         if (CollectionUtils.isNotEmpty(list)) {
             for (Creator c : list) {
                 values.add(new ResourceCreatorProxy(c, rc.getRole()));
             }
         }
     }

     /**
      * Applies the @link ResourceType facet to a search
      *
      * @param qb
      * @param selectedResourceTypes
      * @param handler
      */
     public void addResourceTypeFacetToViewPage(ResourceQueryBuilder qb, List<ResourceType> selectedResourceTypes, SearchResultHandler<?> handler) {
         if (CollectionUtils.isNotEmpty(selectedResourceTypes)) {
             qb.append(new FieldQueryPart<>(QueryFieldNames.RESOURCE_TYPE, MessageHelper.getMessage("searchService.resourceType"), Operator.OR,
                     selectedResourceTypes));
             // If we sort by resource type, then change the primary sort field to the secondary as we're faceting by resource type
             if ((handler.getSortField() == SortOption.RESOURCE_TYPE) || (handler.getSortField() == SortOption.RESOURCE_TYPE_REVERSE)) {
                 handler.setSortField(handler.getSecondarySortField());
             }
         }
     }

     @SuppressWarnings("unchecked")
     public Collection<? extends Resource> findMostRecentResources(long l, TdarUser authenticatedUser, TextProvider provider) throws ParseException, SolrServerException, IOException {
         ReservedSearchParameters params = new ReservedSearchParameters();
         params.getStatuses().add(Status.ACTIVE);
         ResourceQueryBuilder qb = new ResourceQueryBuilder();
         qb.append(params.toQueryPartGroup(MessageHelper.getInstance()));
         SearchResult<Resource> result = new SearchResult<>();
         result.setAuthenticatedUser(authenticatedUser);
         result.setSortField(SortOption.ID_REVERSE);
         result.setSecondarySortField(SortOption.TITLE);
         result.setStartRecord(0);
         result.setRecordsPerPage(10);
         handleSearch(qb, result, provider);
         return (List<Resource>) ((List<?>) result.getResults());
     }

     @SuppressWarnings("unchecked")
     public Collection<? extends Resource> findRecentResourcesSince(Date d, TdarUser authenticatedUser, TextProvider provider) throws ParseException, SolrServerException, IOException {
         ReservedSearchParameters params = new ReservedSearchParameters();
         params.getStatuses().add(Status.ACTIVE);
         ResourceQueryBuilder qb = new ResourceQueryBuilder();
         params.getRegisteredDates().add(new DateRange(d, null));
         qb.append(params.toQueryPartGroup(MessageHelper.getInstance()));
         SearchResult<Resource> result = new SearchResult<>();
         result.setAuthenticatedUser(authenticatedUser);
         result.setSortField(SortOption.ID_REVERSE);
         result.setSecondarySortField(SortOption.TITLE);
         result.setStartRecord(0);
         result.setRecordsPerPage(1000);
         handleSearch(qb, result, provider);
         return (List<Resource>) ((List<?>) result.getResults());
     }


    public <C>  void facetBy(Class<C> c, Collection<C> vals, SearchResultHandler<Indexable> handler) {
        // TODO Auto-generated method stub
        
    }


 }
