package org.tdar.core.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.Version;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.external.auth.InternalTdarRights;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.query.FieldQueryPart;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.QueryPartGroup;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.queryBuilder.DynamicQueryComponent;
import org.tdar.search.query.queryBuilder.QueryBuilder;
import org.tdar.search.query.queryBuilder.ResourceQueryBuilder;
import org.tdar.struts.search.query.SearchResultHandler;
import org.tdar.utils.Pair;

/*
 * This service handles all of the methods that interact directly with HibernateSearch when running searches
 */
@Service
@Transactional
public class SearchService {
    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    protected static final transient Logger logger = LoggerFactory.getLogger(SearchService.class);
    private static final String[] LUCENE_RESERVED_WORDS = new String[] { "AND", "OR", "NOT" };
    private static final Pattern luceneSantizeQueryPattern = Pattern.compile("(^|\\W)(" + StringUtils.join(LUCENE_RESERVED_WORDS, "|") + ")(\\W|$)");
    private transient Map<Class<?>, Pair<String[], PerFieldAnalyzerWrapper>> parserCacheMap = Collections
            .synchronizedMap(new HashMap<Class<?>, Pair<String[], PerFieldAnalyzerWrapper>>());

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public org.hibernate.search.query.dsl.QueryBuilder getQueryBuilder(Class<?> obj) {
        return Search.getFullTextSession(sessionFactory.getCurrentSession()).getSearchFactory().buildQueryBuilder().forEntity(obj).get();
    }

    public FullTextQuery search(QueryBuilder queryBuilder, SortOption... sortOptions) throws ParseException {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        fullTextSession.setDefaultReadOnly(true);
        setupQueryParser(queryBuilder);
        // NOTE: if the truncation or other errors continue, instead of setting the parser on the queryBuilder, pass the queryBuilder
        // into the setupQueryParser() method and make it synchronized, so that the queryParser.parse() can only be called sequentially
        // and not concurrently. The tradeOff is that you may slow down queries.
        FullTextQuery ftq = fullTextSession.createFullTextQuery(queryBuilder.buildQuery(), queryBuilder.getClasses());
        if (sortOptions != null && sortOptions.length > 0) {
            List<SortField> sortFields = new ArrayList<SortField>();
            for (SortOption sortOption : sortOptions) {
                if (sortOption != null) {
                    sortFields.add(new SortField(sortOption.getSortField(), sortOption.getLuceneSortType(), sortOption.isReversed()));
                }
            }
            ftq.setSort(new Sort(sortFields.toArray(new SortField[0])));
        } else {
            // if no sort specified we sort by descending score
            ftq.setSort(new Sort());
        }
        logger.trace("completed fulltextquery setup");
        logger.trace(ftq.getQueryString());
        return ftq;
    }

    /**
     * Execute search using specified queryBuilder, sorting by descending score
     * 
     * @param queryBuilder
     * @return
     * @throws ParseException
     */
    public FullTextQuery search(QueryBuilder queryBuilder)
            throws ParseException {
        return search(queryBuilder, null);
    }

    /*
     * The default static method takes the class to be inspected and a string that can specify
     * a parent string if needed. The parent string is the "prefix" that shows up in the lucene
     * index. This processes all superclasses.
     */
    public static HashSet<DynamicQueryComponent> createFields(java.lang.Class<?> cls, String parent_) {
        return createFields(cls, parent_, true);
    }

    /*
     * A more specialized method which takes the class to process, the prefix and a boolean to allows
     * you to omit superclasses if needed. It looks at annotations on Fields and Methods. It processes
     * all @Fields, @Field, @Id, @DocumentId, and @IndexedEmbedded annotations.
     */
    public static HashSet<DynamicQueryComponent> createFields(java.lang.Class<?> cls, String parent_, boolean navigateClasspath) {
        HashSet<DynamicQueryComponent> cmpnts = new HashSet<DynamicQueryComponent>();
        logger.trace("Processing annotations on:" + cls.getCanonicalName());

        for (java.lang.reflect.Field fld : cls.getDeclaredFields()) {
            cmpnts.addAll(SearchService.createField(fld, parent_));
        }

        for (Method mthd : cls.getDeclaredMethods()) {
            logger.trace(cls.getCanonicalName() + " - " + mthd.getName());
            cmpnts.addAll(SearchService.createFields(mthd, parent_));
        }

        if (navigateClasspath) {
            Class<?> current = cls;
            while (true) {
                if (current.getSuperclass() == null)
                    break;
                logger.trace("superclass: " + current.getSuperclass().getCanonicalName());
                current = current.getSuperclass();
                cmpnts.addAll(createFields(current, parent_, false));
            }
        }

        return cmpnts;
    }

    /*
     * iterate through each of the Methods and look for annotations to process
     */
    private static HashSet<DynamicQueryComponent> createFields(
            java.lang.reflect.Method mthd, String parent_) {
        HashSet<DynamicQueryComponent> cmpts = new HashSet<DynamicQueryComponent>();
        logger.trace("Processing annotations on:" + mthd.getName() + "()");
        for (Annotation ann : mthd.getAnnotations()) {
            if (ann instanceof Field || ann instanceof Fields || ann instanceof DocumentId || ann instanceof IndexedEmbedded) {
                String label_ = ReflectionService.cleanupMethodName(mthd);
                if (ann instanceof Field) {
                    cmpts.add(createField(parent_, ann, label_, null));
                }
                if (ann instanceof Fields) {
                    for (Field annField : ((Fields) ann).value()) {
                        cmpts.add(createField(parent_, annField, label_, null));
                    }
                }
                if (ann instanceof IndexedEmbedded) {
                    Class<?> embedded = ReflectionService.getFieldReturnType(mthd);
                    if (embedded == null) {
                        embedded = mthd.getReturnType();
                    }

                    cmpts.addAll(createFields(embedded, ReflectionService.cleanupMethodName(mthd)));
                }
            }
        }
        return cmpts;
    }

    /*
     * Method to actually create a DynamicQueryComponent
     */
    private static DynamicQueryComponent createField(String parent_, Annotation ann, String label, Class<?> analyzerClass2) {
        Field annField = (Field) ann;
        String label_ = label;
        if (StringUtils.isNotBlank(annField.name()))
            label_ = annField.name();
        Class<?> analyzerClass = evaluateAnalyzerClass(analyzerClass2, annField.analyzer());
        logger.trace("creating annotation for: " + parent_ + "." + label_);
        return new DynamicQueryComponent(label_, analyzerClass, parent_);
    }

    /*
     * Processes a Field passing the parent
     */
    private static HashSet<DynamicQueryComponent> createField(java.lang.reflect.Field fld, String parent_) {
        Class<?> analyzerClass = null;
        HashSet<DynamicQueryComponent> cmpts = new HashSet<DynamicQueryComponent>();
        // iterate through analyzers first
        logger.trace("Processing annotations on field:" + fld.getName());
        /*
         * need to get the Analyzer annotations first so that they can be stored, as they
         * can be passed in.
         */
        for (Annotation ann : fld.getAnnotations()) {
            if (ann instanceof Analyzer) {
                Analyzer annCls = (Analyzer) ann;
                analyzerClass = evaluateAnalyzerClass(analyzerClass, annCls);
            }
        }

        for (Annotation ann : fld.getAnnotations()) {
            String label_ = fld.getName();
            if (ann instanceof Field || ann instanceof DocumentId || ann instanceof IndexedEmbedded
                    || ann instanceof Fields || ann instanceof Analyzer) {
                if (ann instanceof Field) {
                    Field annField = (Field) ann;
                    if (StringUtils.isNotBlank(annField.name()))
                        label_ = annField.name();
                    cmpts.add(createField(parent_, ann, label_, analyzerClass));
                }

                if (ann instanceof Fields) {
                    for (Field annField : ((Fields) ann).value()) {
                        cmpts.add(createField(parent_, annField, label_, analyzerClass));
                    }
                }

                if (ann instanceof IndexedEmbedded) {
                    Class<?> embedded = ReflectionService.getFieldReturnType(fld);
                    if (embedded == null) {
                        embedded = fld.getType();
                    }
                    logger.trace("IndexedEmbedded on:" + parent_ + "." + fld.getName() + " processing " + embedded.getCanonicalName());
                    cmpts.addAll(createFields(embedded, addParent(parent_, fld.getName())));
                }
            }
        }

        return cmpts;
    }

    /*
     * Passes the parent and child, if the parent is null or empty,
     * just return the child, otherwise add the parent and child with the
     * dot notation.
     */
    public static String addParent(String parent_, String child) {
        if (StringUtils.isNotBlank(parent_)) {
            return parent_ + "." + child;
        }
        return child;
    }

    /*
     * Hibernate uses some special logic to initialize it's annotations
     * so that in most cases if an analyzer is not specified it gets set
     * to "void" this needs to be tested and "removed" so that we can
     * replace it with the default analyzer specified by the user.
     */
    private static Class<?> evaluateAnalyzerClass(Class<?> analyzerClass, Analyzer annCls) {
        if (annCls != null) {
            Class<?> impl = ((org.hibernate.search.annotations.Analyzer) annCls).impl();
            // hibSearch defaults to "void" so removing it
            if (!impl.getCanonicalName().equals("void"))
                return ((org.hibernate.search.annotations.Analyzer) annCls).impl();
        }
        return analyzerClass;
    }

    /*
     * This method actually handles the Lucene search, passed in from the Query Builder and sets the results
     * on the results handler
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void handleSearch(QueryBuilder q, SearchResultHandler resultHandler) throws ParseException {
        if (q.isEmpty() && !resultHandler.isShowAll()) {
            logger.trace("empty query or show all");
            resultHandler.setResults(Collections.EMPTY_LIST);
        }
        long num = System.currentTimeMillis();
        FullTextQuery ftq;
        if (resultHandler.getSecondarySortField() == null) {
            ftq = search(q, resultHandler.getSortField());
        } else {
            ftq = search(q, resultHandler.getSortField(), resultHandler.getSecondarySortField());
        }
        resultHandler.setTotalRecords(ftq.getResultSize());
        ftq.setFirstResult(resultHandler.getStartRecord());
        ftq.setMaxResults(resultHandler.getRecordsPerPage());
        long lucene = System.currentTimeMillis() - num;
        num = System.currentTimeMillis();
        logger.trace("begin adding facets");
        resultHandler.addFacets(ftq);
        logger.trace("completed adding facets");
        if (resultHandler.isDebug()) {
            logger.debug("debug mode on for results handling");
            ftq.setProjection(FullTextQuery.THIS, FullTextQuery.SCORE, FullTextQuery.EXPLANATION);
        } else {
            ftq.setProjection(FullTextQuery.THIS, FullTextQuery.SCORE);
        }
        List<Indexable> toReturn = new ArrayList<Indexable>();
        List list = ftq.list();
        logger.trace("completed hibernate hydration ");
        for (Object[] obj : (List<Object[]>) list) {
            Float score = (Float) obj[1];
            Indexable p = (Indexable) obj[0];
            if (resultHandler.isDebug()) {
                Explanation ex = (Explanation) obj[2];
                p.setExplanation(ex);
            }
            if (p == null) {
                logger.trace("persistable is null: {}", p);
            } else {
                p.setScore(score);
            }
            toReturn.add(p);
        }
        Object searchMetadata[] = { resultHandler.getMode(), q.getQuery(), resultHandler.getSortField(), resultHandler.getSecondarySortField(),
                lucene, (System.currentTimeMillis() - num),
                ftq.getResultSize(),
                resultHandler.getStartRecord() };
        logger.debug("{}: {} (SORT:{},{})\t LUCENE: {} | HYDRATION: {} | # RESULTS: {} | START #: {}", searchMetadata);
        logger.trace("returning: {}", toReturn);
        resultHandler.setResults(toReturn);
    }

    /*
     * Shared logic to find all direct children of container resource (ResourceCollections and Projects)
     */
    public <P extends Persistable> ResourceQueryBuilder buildResourceContainedInSearch(String fieldName, P indexable, Person user) {
        ResourceQueryBuilder qb = new ResourceQueryBuilder();
        qb.setOperator(Operator.OR);
        QueryPartGroup allVisible = new QueryPartGroup(Operator.AND);
        allVisible.append(new FieldQueryPart(QueryFieldNames.STATUS, Status.ACTIVE));
        allVisible.append(new FieldQueryPart(fieldName, indexable.getId().toString()));
        qb.append(allVisible);

        if (user != null) {
            buildSpecialRights(fieldName, indexable, user, qb, Status.DRAFT, InternalTdarRights.SEARCH_FOR_DRAFT_RECORDS);
            buildSpecialRights(fieldName, indexable, user, qb, Status.FLAGGED, InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS);
            buildSpecialRights(fieldName, indexable, user, qb, Status.DELETED, InternalTdarRights.SEARCH_FOR_DELETED_RECORDS);
        }

        return qb;
    }

    private <P extends Persistable> void buildSpecialRights(String fieldName, P indexable, Person user, ResourceQueryBuilder qb, Status status,
            InternalTdarRights right) {
        QueryPartGroup specialRights = new QueryPartGroup(Operator.AND);
        specialRights.append(new FieldQueryPart(QueryFieldNames.STATUS, status));
        if (authenticationAndAuthorizationService.cannot(right, user)) {
            specialRights.append(new FieldQueryPart(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, user.getId().toString()));
        }
        specialRights.append(new FieldQueryPart(fieldName, indexable.getId().toString()));
        qb.append(specialRights);
    }

    private void setupQueryParser(QueryBuilder qb) {
        Pair<String[], PerFieldAnalyzerWrapper> pair = parserCacheMap.get(qb.getClass());
        if (pair == null) {
            List<String> fields = new ArrayList<String>();
            Set<DynamicQueryComponent> cmpnts = new HashSet<DynamicQueryComponent>();
            PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new LowercaseWhiteSpaceStandardAnalyzer());

            // add all DynamicQueryComponents for specified classes
            for (Class<?> cls : qb.getClasses()) {
                cmpnts.addAll(SearchService.createFields(cls, ""));
            }

            List<DynamicQueryComponent> toRemove = new ArrayList<DynamicQueryComponent>();
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
            /*
             * The <b>fields</b> list specifies all of the generic fields that
             * use the default analyzer.
             * 
             * The rest of the fields have analyzers specified and get added to
             * the analyzer with the specific analyzer
             */
            for (DynamicQueryComponent cmp : cmpnts) {
                if (qb.stringContainedInLabel(cmp.getLabel()))
                    continue;
                fields.add(cmp.getLabel());
                if (cmp.getAnalyzer() != null) {
                    try {
                        analyzer.addAnalyzer(cmp.getLabel(), (org.apache.lucene.analysis.Analyzer) cmp.getAnalyzer().newInstance());
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    logger.trace(cmp.getLabel() + " : " + cmp.getAnalyzer().getCanonicalName());
                }
            }
            // MultiFieldQueryParser qp = new MultiFieldQueryParser(Version.LUCENE_31, fields.toArray(new String[0]), analyzer);
            pair = new Pair<String[], PerFieldAnalyzerWrapper>(fields.toArray(new String[fields.size()]), analyzer);
            parserCacheMap.put(qb.getClass(), pair);
        }
        QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_31, pair.getFirst(), pair.getSecond());
        qb.setQueryParser(parser);
    }

    /*
     * Replace AND/OR with lowercase so that lucene does not interpret them as operaters.
     * It is not necessary sanitized quoted strings.
     */
    public String sanitize(String unsafeQuery) {
        Matcher m = luceneSantizeQueryPattern.matcher(unsafeQuery);
        return m.replaceAll("$1\\\\$2$3");
    }

}
