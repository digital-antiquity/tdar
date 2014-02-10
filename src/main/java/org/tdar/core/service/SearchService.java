package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.Version;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.facet.Facet;
import org.hibernate.search.query.facet.FacetSortOrder;
import org.hibernate.search.query.facet.FacetingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.DeHydratable;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.SearchPaginationException;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SearchResultHandler.ProjectionModel;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.DynamicQueryComponent;
import org.tdar.search.query.builder.DynamicQueryComponentHelper;
import org.tdar.search.query.builder.InstitutionQueryBuilder;
import org.tdar.search.query.builder.PersonQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.AbstractHydrateableQueryPart;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.InstitutionQueryPart;
import org.tdar.search.query.part.PersonQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryGroup;
import org.tdar.search.query.part.QueryPart;
import org.tdar.struts.action.search.ReservedSearchParameters;
import org.tdar.struts.action.search.SearchParameters;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.data.ResourceCreatorProxy;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.TextProvider;

@Service
@Transactional
public class SearchService {
    private static final String ID_FIELD = "id";

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    ObfuscationService obfuscationService;

    @Autowired
    private GenericService genericService;

    @Autowired
    private DatasetDao datasetDao;
    
    protected static final transient Logger logger = LoggerFactory.getLogger(SearchService.class);
    private static final String[] LUCENE_RESERVED_WORDS = new String[] { "AND", "OR", "NOT" };
    private static final Pattern luceneSantizeQueryPattern = Pattern.compile("(^|\\W)(" + StringUtils.join(LUCENE_RESERVED_WORDS, "|") + ")(\\W|$)");

    private transient ConcurrentMap<Class<?>, Pair<String[], PerFieldAnalyzerWrapper>> parserCacheMap = new ConcurrentHashMap<>();

    public static int MAX_FTQ_RESULTS = 50_000;

    /**
     * Log the parser map (For debugging)
     */
    public void logParserMap() {
        for (Map.Entry<Class<?>, Pair<String[], PerFieldAnalyzerWrapper>> entry : parserCacheMap.entrySet()) {
            Class<?> type = entry.getKey();
            Pair<String[], PerFieldAnalyzerWrapper> pair = entry.getValue();
            logger.info("map key\t class: {}", type.getCanonicalName());
            logger.info("map value\t first:arr[{}]\t second:{}", pair.getFirst().length, pair.getSecond());
            for (int i = 0; i < pair.getFirst().length; i++) {
                logger.info("\t\t i:{}\t v:{}", i, pair.getFirst()[i]);
            }
        }
    }

    /**
     * Expose the session factory for HibernateSearch
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * get Hibernate's version of a query builder if needed
     * 
     * @param obj
     * @return
     */
    public org.hibernate.search.query.dsl.QueryBuilder getQueryBuilder(Class<?> obj) {
        return Search.getFullTextSession(sessionFactory.getCurrentSession()).getSearchFactory().buildQueryBuilder().forEntity(obj).get();
    }

    /**
     * Perform a search based on the @link QueryBuilder and @link SortOption array.
     * @param queryBuilder
     * @param sortOptions
     * @return
     * @throws ParseException
     */
    public FullTextQuery search(QueryBuilder queryBuilder, SortOption... sortOptions) throws ParseException {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        fullTextSession.setDefaultReadOnly(true);
        setupQueryParser(queryBuilder);
        Query query = new MatchAllDocsQuery();
        if (!queryBuilder.isEmpty()) {
            query = queryBuilder.buildQuery();
        }
        FullTextQuery ftq = fullTextSession.createFullTextQuery(query, queryBuilder.getClasses());

        if (sortOptions == null || sortOptions.length == 0) {
            // if no sort specified we sort by descending score
            ftq.setSort(new Sort(new SortField(SortOption.getDefaultSortOption().getSortField(), SortOption.getDefaultSortOption().getLuceneSortType())));
        }
        else {
            List<SortField> sortFields = new ArrayList<>();
            for (SortOption sortOption : sortOptions) {
                if (sortOption != null) {
                    sortFields.add(new SortField(sortOption.getSortField(), sortOption.getLuceneSortType(), sortOption.isReversed()));
                }
            }
            ftq.setSort(new Sort(sortFields.toArray(new SortField[0])));
        }
        logger.trace("completed fulltextquery setup");
        logger.trace(ftq.getQueryString());
        return ftq;
    }


    /**
     * This method actually handles the Lucene search, passed in from the Query Builder and sets the results
     * on the results handler
     * 
     * @param q
     * @param resultHandler
     * @throws ParseException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void handleSearch(QueryBuilder q, SearchResultHandler resultHandler) throws ParseException {
        if (q.isEmpty() && !resultHandler.isShowAll()) {
            logger.trace("empty query or show all");
            resultHandler.setResults(Collections.EMPTY_LIST);
        }
        long num = System.currentTimeMillis();
        hydrateQueryParts(q);
        FullTextQuery ftq = search(q, resultHandler.getSortField(), resultHandler.getSecondarySortField());

        resultHandler.setTotalRecords(ftq.getResultSize());
        ftq.setFirstResult(resultHandler.getStartRecord());
        ftq.setMaxResults(resultHandler.getRecordsPerPage());
        long lucene = System.currentTimeMillis() - num;
        num = System.currentTimeMillis();
        logger.trace("begin adding facets");
        processFacets(ftq, resultHandler);
        logger.trace("completed adding facets");
        List<String> projections = setupProjectionsForSearch(resultHandler, ftq);
        List list = ftq.list();
        logger.trace("completed hibernate hydration ");

        // user may be null (e.g. user not logged in)
        Person user = resultHandler.getAuthenticatedUser();

        List<Indexable> toReturn = convertProjectedResultIntoObjects(resultHandler, projections, list, user);
        Object searchMetadata[] = { resultHandler.getMode(), q.getQuery(), resultHandler.getSortField(), resultHandler.getSecondarySortField(),
                lucene, (System.currentTimeMillis() - num),
                ftq.getResultSize(),
                resultHandler.getStartRecord() };
        logger.debug("{}: {} (SORT:{},{})\t LUCENE: {} | HYDRATION: {} | # RESULTS: {} | START #: {}", searchMetadata);

        if (resultHandler.getStartRecord() > ftq.getResultSize()) {
            throw new SearchPaginationException(MessageHelper.getMessage("searchService.start_record_too_high", resultHandler.getStartRecord(),
                    ftq.getResultSize()));
        }

        logger.trace("returning: {}", toReturn);
        resultHandler.setResults(toReturn);
    }

    /**
     * Generate Facet Requests based on those specified on the @link SearchResultHandler
     * @param ftq
     * @param resultHandler
     */
    @SuppressWarnings("rawtypes")
    private <F extends Facetable> void processFacets(FullTextQuery ftq, SearchResultHandler<?> resultHandler) {
        if (resultHandler.getFacetFields() == null)
            return;

        for (FacetGroup<? extends Facetable> facet : resultHandler.getFacetFields()) {
            FacetingRequest facetRequest = getQueryBuilder(Resource.class).facet().name(facet.getFacetField())
                    .onField(facet.getFacetField()).discrete().orderedBy(FacetSortOrder.COUNT_DESC)
                    .includeZeroCounts(false).createFacetingRequest();
            ftq.getFacetManager().enableFaceting(facetRequest);
        }
        for (FacetGroup<? extends Facetable> facet : resultHandler.getFacetFields()) {
            for (Facet facetResult : ftq.getFacetManager().getFacets(facet.getFacetField())) {
                facet.add(facetResult.getValue(), facetResult.getCount());
            }
        }
    }

    /**
     * Taking the projected List<Object[]> and converting them back into something we can use; if using projection, we hydrate those field
     * we use "projection" to add the score and possibly the explanation in... but we also use it to get back simpler results
     * so we can control things like the JSON lookups to make them superfast because we just need "certain" fields, most of these
     * will just have an "ID" that we hydrate later on
     * 
     * @param resultHandler
     * @param projections
     * @param list
     * @param user
     * @return
     */
    private List<Indexable> convertProjectedResultIntoObjects(SearchResultHandler<?> resultHandler, List<String> projections, List<Object[]> list, Person user) {
        List<Indexable> toReturn = new ArrayList<>();
        LinkedHashMap<Long,Object[]> ids = new LinkedHashMap<>();
        ProjectionModel projectionModel = resultHandler.getProjectionModel();
        if (projectionModel == null) {
            projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
        }
        for (Object[] obj : list) {
            Indexable p = null;
            int idIndex = projections.indexOf(ID_FIELD);
            if (idIndex != -1) {
                ids.put((Long) obj[idIndex], obj);
            }
            if (projectionModel == ProjectionModel.HIBERNATE_DEFAULT) {
                p = (Indexable) obj[0];
                if (Persistable.Base.isNullOrTransient(p)) {
                    logger.error("persistable is null? {} " , obj);
                } else {
                    ids.put(p.getId(), obj);
                }
            } 
            toReturn.add(p);
        }
       // iterate over all of the objects and create an objectMap if needed
       if (CollectionUtils.isNotEmpty(ids.keySet())) {
            switch (projectionModel) {
                case LUCENE:
                    toReturn.clear();
                    createSpareObjectFromProjection(resultHandler, projections, ids);
                    break;
                case RESOURCE_PROXY:
                    toReturn.clear();
                    toReturn.addAll(datasetDao.findSkeletonsForSearch(ids.keySet().toArray(new Long[0])));
                    break;
                default:
                    break;
            }
            
            for (Indexable p : toReturn) {
                if (Persistable.Base.isNullOrTransient(p)) {
                    continue;
                }
                Object[] obj = ids.get(p.getId());
                Float score = (Float) obj[projections.indexOf(FullTextQuery.SCORE)];
                if (resultHandler.isDebug()) {
                    Explanation ex = (Explanation) obj[projections.indexOf(FullTextQuery.EXPLANATION)];
                    p.setExplanation(ex);
                }
                if (TdarConfiguration.getInstance().obfuscationInterceptorDisabled() && Persistable.Base.isNullOrTransient(user)) {
                    obfuscationService.obfuscate((Obfuscatable) p, user);
                }
                getAuthenticationAndAuthorizationService().applyTransientViewableFlag(p, user);
                p.setScore(score);
            }
        }
        return toReturn;
    }

    /**
     * Takes the projected object and list of projections and turns them back into an object we can use, often just with Id
     * 
     * @param resultHandler
     * @param projections
     * @param ids
     * @return
     */
    private List<Indexable> createSpareObjectFromProjection(SearchResultHandler<?> resultHandler, List<String> projections, LinkedHashMap<Long, Object[]> ids) {
        List<Indexable> toReturn = new ArrayList<>();
        for (Entry<Long,Object[]> entry : ids.entrySet()) {
            Object[] obj = entry.getValue();
            @SuppressWarnings("unchecked")
            Class<? extends Indexable> cast = (Class<? extends Indexable>) obj[projections.indexOf(FullTextQuery.OBJECT_CLASS)];

            try {
                Indexable p = cast.newInstance();
                ProjectionModel projectionModel = resultHandler.getProjectionModel();
                if (projectionModel == null) {
                    projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
                }

                Collection<String> fields = projectionModel.getProjections();
                for (String field : fields) {
                    BeanUtils.setProperty(p, field, obj[projections.indexOf(field)]);
                }
                toReturn.add(p);
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException(e);
            }
            
        }
                
        return toReturn;
    }

    /**
     * This method takes the projects from the search handler (if there are any) and adds them to a projection list that we're managing
     * 
     * @param resultHandler
     * @param ftq
     * @return
     */
    private List<String> setupProjectionsForSearch(SearchResultHandler<?> resultHandler, FullTextQuery ftq) {
        List<String> projections = new ArrayList<>();
        projections.add(FullTextQuery.THIS); // Hibernate Object
        projections.add(FullTextQuery.OBJECT_CLASS); // class to project

        ProjectionModel projectionModel = resultHandler.getProjectionModel();
        if (projectionModel == null) {
            projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
        }

        if (projectionModel != ProjectionModel.HIBERNATE_DEFAULT) { // OVERRIDE CASE, PROJECTIONS SET IN RESULTS HANDLER
            projections.remove(FullTextQuery.THIS);
            projections.addAll(resultHandler.getProjectionModel().getProjections());
        }

        projections.add(FullTextQuery.SCORE);
        if (resultHandler.isDebug()) {
            logger.debug("debug mode on for results handling");
            projections.add(FullTextQuery.EXPLANATION);
        }
        ftq.setProjection(projections.toArray(new String[0]));
        return projections;
    }

    /**
     * For all of the HydratableQueryParts iterate through them and do a find by id... keep the same order
     * 
     * @param q
     */
    @SuppressWarnings({ "rawtypes","unchecked" })
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
                if (Persistable.Base.isNotTransient(fieldValue)) {
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

            idLookupMap.put(cls, (Map<Long, Persistable>) Persistable.Base.createIdMap(hydrated));
        }

        // repopulate
        for (int i = 0; i < partList.size(); i++) {
            AbstractHydrateableQueryPart part = partList.get(i);
            Class<T> cls = part.getActualClass();
            for (int j = 0; j < part.getFieldValues().size(); j++) {
                T fieldValue = (T) part.getFieldValues().get(j);
                if (Persistable.Base.isNotTransient(fieldValue)) {
                    part.getFieldValues().set(j, idLookupMap.get(cls).get(fieldValue.getId()));
                } else {
                    logger.info("not adding: {} ", idLookupMap.get(cls), fieldValue);
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
     * Shared logic to find all direct children of container resource (ResourceCollections and Projects)
     * 
     * @param fieldName
     * @param indexable
     * @param user
     * @return
     */
    public <P extends Persistable> ResourceQueryBuilder buildResourceContainedInSearch(String fieldName, P indexable, Person user, TextProvider provider) {
        ResourceQueryBuilder qb = new ResourceQueryBuilder();
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        getAuthenticationAndAuthorizationService().initializeReservedSearchParameters(reservedSearchParameters, user);
        qb.append(reservedSearchParameters,provider);
        qb.setOperator(Operator.AND);
        qb.append(new FieldQueryPart<>(fieldName, indexable.getId()));

        return qb;
    }

    /**
     * Constructs a new MultiFieldQueryParser and sets it on the QueryBuilder parameter.
     * 
     * Currently caches the QueryBuilder's class with a Pair<String[] field names, PerFieldAnalyzerWrapper> used to construct the
     * MultiFieldQueryParser.
     * 
     * The MultiFieldQueryParser cannot be cached as it is not thread-safe. PerFieldAnalyzerWrapper is not thread-safe either (has an internal HashMap) but
     * appears to be safely usable by multiple threads as long as we don't add more analyzers to it (TODO: need to verify this).
     * 
     * @param qb
     */
    private void setupQueryParser(QueryBuilder qb) {
        Pair<String[], PerFieldAnalyzerWrapper> pair = parserCacheMap.get(qb.getClass());
        if (pair == null) {
            List<String> fields = new ArrayList<>();
            Set<DynamicQueryComponent> cmpnts = new HashSet<>();
            PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new LowercaseWhiteSpaceStandardAnalyzer());

            // add all DynamicQueryComponents for specified classes
            for (Class<?> cls : qb.getClasses()) {
                cmpnts.addAll(DynamicQueryComponentHelper.createFields(cls, ""));
            }

            applyAnalyzers(qb, fields, cmpnts, analyzer);
            // XXX: do not cache the actual MultiFieldQueryParser, it's not thread-safe
            // MultiFieldQueryParser qp = new MultiFieldQueryParser(Version.LUCENE_31, fields.toArray(new String[0]), analyzer);
            Pair<String[], PerFieldAnalyzerWrapper> newPair = new Pair<>(fields.toArray(new String[fields.size()]), analyzer);
            pair = parserCacheMap.putIfAbsent(qb.getClass(), newPair);
            if (pair == null) {
                pair = newPair;
            }
        }
        QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_35, pair.getFirst(), pair.getSecond());
        qb.setQueryParser(parser);
    }


    /**
     * The <b>fields</b> list specifies all of the generic fields that do not use the default analyzer.
     * 
     * @param qb
     * @param fields
     * @param cmpnts
     * @param analyzer
     */
    @SuppressWarnings("deprecation")
    private void applyAnalyzers(QueryBuilder qb, List<String> fields, Set<DynamicQueryComponent> cmpnts, PerFieldAnalyzerWrapper analyzer) {
        List<DynamicQueryComponent> toRemove = new ArrayList<>();
        // add all overrides and replace existing settings
        if (qb.getOverrides() != null) {
            for (DynamicQueryComponent over : qb.getOverrides()) {
                for (DynamicQueryComponent cmp : cmpnts) {
                    if (over.getLabel().equals(cmp.getLabel())) {
                        toRemove.add(cmp);
                    }
                }
            }
            cmpnts.removeAll(toRemove);
            cmpnts.addAll(qb.getOverrides());
        }
        for (DynamicQueryComponent cmp : cmpnts) {
            String partialLabel = qb.stringContainedInLabel(cmp.getLabel());
            if (partialLabel != null) {
                Class<? extends org.apache.lucene.analysis.Analyzer> overrideAnalyzerClass = qb.getPartialLabelOverrides().get(partialLabel);
                if (overrideAnalyzerClass != null) {
                    cmp.setAnalyzer(overrideAnalyzerClass);
                } else {
                    continue;
                }
            }

            fields.add(cmp.getLabel());
            if (cmp.getAnalyzer() != null) {
                try {
                    analyzer.addAnalyzer(cmp.getLabel(), (org.apache.lucene.analysis.Analyzer) cmp.getAnalyzer().newInstance());
                } catch (Exception e) {
                    logger.debug("cannot add analyzer:", e);
                }
                logger.trace(cmp.getLabel() + " : " + cmp.getAnalyzer().getCanonicalName());
            }
        }
    }

    /**
     * remove unauthorized statuses from list. it's up to caller to handle implications of empty list
     * 
     * @param statusList
     * @param user
     */
    public void filterStatusList(List<Status> statusList, Person user) {
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(statusList, Status.DELETED, InternalTdarRights.SEARCH_FOR_DELETED_RECORDS, user);
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(statusList, Status.FLAGGED, InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS, user);
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(statusList, Status.DRAFT, InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS, user);
    }

    private AuthenticationAndAuthorizationService getAuthenticationAndAuthorizationService() {
        return obfuscationService.getAuthenticationAndAuthorizationService();
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
            // making unchecked cast so compiler accepts call to set()
            @SuppressWarnings("unchecked")
            ListIterator<Persistable> itor = (ListIterator<Persistable>) list.listIterator();
            while (itor.hasNext()) {
                Persistable sparse = itor.next();
                if (sparse != null) {
                    Persistable persistable = genericService.find(sparse.getClass(), sparse.getId());
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
     */
    public void updateResourceCreators(SearchParameters group, Integer maxCreatorsToResolve) throws ParseException {
        logger.trace("updating proxies");
        int maxToResolve = 1000;
        if (maxCreatorsToResolve != null && maxCreatorsToResolve > 0) {
            maxToResolve = maxCreatorsToResolve;
        }
        Map<ResourceCreatorProxy, List<ResourceCreatorProxy>> replacements = new HashMap<>();
        List<ResourceCreatorProxy> proxies = group.getResourceCreatorProxies();
        for (ResourceCreatorProxy proxy : proxies) {
            if (proxy == null)
                continue;
            ResourceCreator rc = proxy.getResourceCreator();
            if (rc != null && proxy.isValid()) {
                ArrayList<ResourceCreatorProxy> values = new ArrayList<>();
                Creator creator = rc.getCreator();
                if (Persistable.Base.isTransient(creator)) {
                    resolveCreator(maxToResolve, replacements, proxy, rc, values, creator);
                }
            } else {
                replacements.put(proxy, null);
            }
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
     */
    @SuppressWarnings("unchecked")
    private void resolveCreator(int maxToResolve, Map<ResourceCreatorProxy, List<ResourceCreatorProxy>> replacements, ResourceCreatorProxy proxy,
            ResourceCreator rc, ArrayList<ResourceCreatorProxy> values, Creator creator) throws ParseException {
        QueryBuilder q;
        replacements.put(proxy, values);
        if (creator instanceof Institution) {
            q = new InstitutionQueryBuilder();
            InstitutionQueryPart iqp = new InstitutionQueryPart();
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
        FullTextQuery search = search(q, ((SortOption[]) null));
        search.setMaxResults(maxToResolve);
        list = search.list();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Creator c : list) {
                values.add(new ResourceCreatorProxy(c, rc.getRole()));
            }
        }
    }

    /**
     * Generates a query for resources created by or releated to in some way to a @link Creator given a creator and a user
     * 
     * @param creator
     * @param user
     * @return
     */
    public QueryBuilder generateQueryForRelatedResources(Creator creator, Person user,TextProvider provider) {
        QueryBuilder queryBuilder = new ResourceQueryBuilder();
        queryBuilder.setOperator(Operator.AND);

        SearchParameters params = new SearchParameters(Operator.OR);
        // could use "creator type" to filter; but this doesn't cover the creator type "OTHER"
        for (ResourceCreatorRole role : ResourceCreatorRole.values()) {
            if (role == ResourceCreatorRole.UPDATER) {
                continue;
            }
            params.getResourceCreatorProxies().add(new ResourceCreatorProxy(creator, role));
        }
        queryBuilder.append(params,provider);
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        getAuthenticationAndAuthorizationService().initializeReservedSearchParameters(reservedSearchParameters, user);
        queryBuilder.append(reservedSearchParameters,provider);
        return queryBuilder;
    }

    /**
     * Applies the @link ResourceType facet to a search
     * @param qb
     * @param selectedResourceTypes
     * @param handler
     */
    public void addResourceTypeFacetToViewPage(ResourceQueryBuilder qb, List<ResourceType> selectedResourceTypes, SearchResultHandler<?> handler) {
        if (CollectionUtils.isNotEmpty(selectedResourceTypes)) {
            qb.append(new FieldQueryPart<>(QueryFieldNames.RESOURCE_TYPE, MessageHelper.getMessage("searchService.resourceType"), Operator.OR, selectedResourceTypes));
            // If we sort by resource type, then change the primary sort field to the secondary as we're faceting by resource type
            if (handler.getSortField() == SortOption.RESOURCE_TYPE || handler.getSortField() == SortOption.RESOURCE_TYPE_REVERSE) {
                handler.setSortField(handler.getSecondarySortField());
            }
        }
    }

    public Collection<? extends Resource> findMostRecentResources(long l, Person authenticatedUser) throws ParseException {
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.getStatuses().add(Status.ACTIVE);
        ResourceQueryBuilder qb = new ResourceQueryBuilder();
        qb.append(params.toQueryPartGroup(MessageHelper.getInstance()));
        SearchResult result = new SearchResult();
        result.setAuthenticatedUser(authenticatedUser);
        result.setSortField(SortOption.DATE_REVERSE);
        result.setSecondarySortField(SortOption.TITLE);
        result.setStartRecord(0);
        result.setRecordsPerPage(10);
        handleSearch(qb, result);
        return (List<Resource>)((List<?>)result.getResults());
    }

}
