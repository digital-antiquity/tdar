package org.tdar.core.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.search.query.DynamicQueryComponent;
import org.tdar.search.query.QueryBuilder;

@Service
@Transactional
public class SearchService {
	@Autowired
	private SessionFactory sessionFactory;
	public static Logger logger = Logger.getLogger(SearchService.class);
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public FullTextQuery search(Query query, String sortField, Class<?>... classes) {
		return search(query, sortField, false, classes);
	}
	
	public org.hibernate.search.query.dsl.QueryBuilder getQueryBuilder(Class<?> obj) {
		return Search.getFullTextSession(sessionFactory.getCurrentSession()).getSearchFactory().buildQueryBuilder().forEntity(obj).get();
	}
	
	public FullTextQuery search(Query query, String sortField, boolean reverseSort, Class<?>... classes) {
		FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
		FullTextQuery ftq = fullTextSession.createFullTextQuery(query, classes);
		if (sortField != null) {
			// TODO: Determine the best way to specify sort. 
			// Here we are guessing the field being sorted is a string which I think
			// is a safe assumption b/c all fields should sort lexicographically.
			ftq.setSort(new Sort(new SortField(sortField, SortField.STRING, reverseSort)));
		} else {
		    //if no sort specified we sort by descending score  
		    ftq.setSort(new Sort());
		}
		
		return ftq;
	}
	
	public FullTextQuery search(QueryBuilder queryBuilder, String sortField, boolean reverseSort) 
	throws ParseException {
		return search(queryBuilder.buildQuery(), sortField, reverseSort, queryBuilder.getClasses());
	}
	
	public FullTextQuery search(QueryBuilder queryBuilder, String sortField) 
	throws ParseException {
		return search(queryBuilder, sortField, false);
	}
	
	/**
	 * Execute search using specified queryBuilder, sorting by descending score
	 * @param queryBuilder
	 * @return 
	 * @throws ParseException
	 */
	public FullTextQuery search(QueryBuilder queryBuilder) 
	throws ParseException {
		return search(queryBuilder, null, false);
	}
	
	/*
	 * The default static method takes the class to be inspected and a string that can specify
	 * a parent string if needed.  The parent string is the "prefix" that shows up in the lucene
	 * index.  This processes all superclasses.
	 */
	public static HashSet<DynamicQueryComponent> createFields(java.lang.Class<?> cls, String parent_) {
		return createFields(cls, parent_, true);
	}
	
	/*
	 * A more specialized method which takes the class to process, the prefix and a boolean to allows
	 * you to omit superclasses if needed.  It looks at annotations on Fields and Methods.  It processes
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
				if (current.getSuperclass() == null) break;
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
		logger.trace("Processing annotations on:" + mthd.getName() +"()");
		for (Annotation ann : mthd.getAnnotations()) {
			if (ann instanceof Field || ann instanceof Fields || ann instanceof DocumentId || ann instanceof IndexedEmbedded) {
				String label_ = cleanupMethodName(mthd.getName());
				if (ann instanceof Field) {
					cmpts.add(createField(parent_, ann, label_,null));
				}
				if (ann instanceof Fields) {
					for (Field annField : ((Fields)ann).value()) {
						cmpts.add(createField(parent_, annField, label_,null));
					}
				}
				if (ann instanceof IndexedEmbedded) {
					Class<?> embedded = mthd.getReturnType();

					try {
						Type t = mthd.getGenericReturnType();
						if (t instanceof ParameterizedType) {
							for (Object tt : ((ParameterizedType)t).getActualTypeArguments()) {
								if (tt instanceof Class) embedded = ((Class<?>)tt);
							}
						}
						logger.trace("type: " + t.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}

					cmpts.addAll(createFields(embedded, cleanupMethodName(mthd.getName())));
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
		if (StringUtils.isNotBlank(annField.name())) label_ = annField.name();
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
		/* need to get the Analyzer annotations first so that they can be stored, as they
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
					if (StringUtils.isNotBlank(annField.name())) label_ = annField.name();
					cmpts.add(createField(parent_, ann, label_,analyzerClass));
				}

				if (ann instanceof Fields) {
					for (Field annField : ((Fields)ann).value()) {
						cmpts.add(createField(parent_, annField, label_, analyzerClass));
					}
				}

				if (ann instanceof IndexedEmbedded) {
                    Class<?> embedded = fld.getType();
                    try {
                        Type t = fld.getGenericType();
                        if (fld.getGenericType() instanceof ParameterizedType) {
                            for (Object tt : ((ParameterizedType)fld.getGenericType()).getActualTypeArguments()) {
                                if (tt instanceof Class) embedded = ((Class<?>)tt);
                            }
                        }
                        logger.trace("type: " + t.toString());
                    } catch (Exception e) {}
					
					logger.trace("IndexedEmbedded on:" +parent_ +"."+ fld.getName() + " processing " + embedded.getCanonicalName());
					cmpts.addAll(createFields(embedded, addParent(parent_,fld.getName())));
				}
			}
		}

		return cmpts;
	}
	
	/**
	 * Take the method name and try and replace it with the same
	 * logic that Hibernate uses
	 * @param name
	 * @return
	 */
	private static String cleanupMethodName(String name) {
		name = name.replaceAll("^(get|set)", "");
		name = name.substring(0,1).toLowerCase() + name.substring(1);
		return name;
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

}
