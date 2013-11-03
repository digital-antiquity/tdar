package org.tdar.search.query.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.ReflectionService;

/**
 * Static class to help with @link DynamicQueryComponent beans and building their object graphs
 * @author abrin
 *
 */
public class DynamicQueryComponentHelper {

    private static final String VOID = "void";
    protected static final transient Logger logger = LoggerFactory.getLogger(DynamicQueryComponentHelper.class);

    
    /**
     * The default static method takes the class to be inspected and a string that can specify
     * a parent string if needed. The parent string is the "prefix" that shows up in the lucene
     * index. This processes all superclasses.
     * 
     * @param cls
     * @param parent_
     * @return
     */
    public static HashSet<DynamicQueryComponent> createFields(java.lang.Class<?> cls, String parent_) {
        return createFields(cls, parent_, true);
    }

    /**
     * A more specialized method which takes the class to process, the prefix and a boolean to allows
     * you to omit superclasses if needed. It looks at annotations on Fields and Methods. It processes
     * all @Fields, @Field, @Id, @DocumentId, and @IndexedEmbedded annotations.
     * 
     * @param cls
     * @param parent_
     * @param navigateClasspath
     * @return
     */
    public static HashSet<DynamicQueryComponent> createFields(java.lang.Class<?> cls, String parent_, boolean navigateClasspath) {
        HashSet<DynamicQueryComponent> cmpnts = new HashSet<>();
        logger.trace(String.format("Processing annotations on: %s prefix:%s navigate: %s", cls.getCanonicalName(), parent_, navigateClasspath));

        for (java.lang.reflect.Field fld : cls.getDeclaredFields()) {
            cmpnts.addAll(DynamicQueryComponentHelper.createDynamicQueryComponent(fld, parent_));
        }

        for (Method mthd : cls.getDeclaredMethods()) {
            cmpnts.addAll(DynamicQueryComponentHelper.createFields(mthd, parent_));
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

    /**
     * iterate through each of the Methods and look for annotations to process
     * 
     * @param mthd
     * @param parent_
     * @return
     */
    private static HashSet<DynamicQueryComponent> createFields(java.lang.reflect.Method mthd, String parent_) {
        HashSet<DynamicQueryComponent> cmpts = new HashSet<>();
        logger.trace(String.format("\tProcessing annotations on:  %s.%s()", parent_, mthd.getName()));
        for (Annotation ann : mthd.getAnnotations()) {
            if (ann instanceof Field || ann instanceof Fields || ann instanceof DocumentId || ann instanceof IndexedEmbedded) {
                String label_ = ReflectionService.cleanupMethodName(mthd);
                if (ann instanceof Field) {
                    cmpts.add(createDynamicQueryComponent(parent_, ann, label_, null));
                }
                if (ann instanceof Fields) {
                    for (Field annField : ((Fields) ann).value()) {
                        cmpts.add(createDynamicQueryComponent(parent_, annField, label_, null));
                    }
                }
                if (ann instanceof IndexedEmbedded) {
                    IndexedEmbedded ian = (IndexedEmbedded) ann;
                    String prefix = ReflectionService.cleanupMethodName(mthd);

                    // use prefix instead of getter name, if supplied
                    if (!StringUtils.equals(".", ian.prefix())) {
                        prefix = ian.prefix();
                    }

                    prefix = parent_ + prefix;

                    Class<?> embedded = ReflectionService.getFieldReturnType(mthd);
                    if (embedded == null) {
                        embedded = mthd.getReturnType();
                    }

                    cmpts.addAll(createFields(embedded, prefix));
                }
            }
        }
        return cmpts;
    }

    /**
     * Method to actually create a @link DynamicQueryComponent which allows us to specify and override the field analyzers on search within tDAR
     * 
     * @param parent_
     * @param ann
     * @param label
     * @param analyzerClass2
     * @return
     */
    private static DynamicQueryComponent createDynamicQueryComponent(String parent_, Annotation ann, String label, Class<?> analyzerClass2) {
        Field annField = (Field) ann;
        String label_ = label;
        if (StringUtils.isNotBlank(annField.name()))
            label_ = annField.name();
        Class<?> analyzerClass = evaluateAnalyzerClass(analyzerClass2, annField.analyzer());
        logger.trace("creating annotation for: " + parent_ + "." + label_);
        return new DynamicQueryComponent(label_, analyzerClass, parent_);
    }

    /**
     * Processes a search Field passing the parent
     * @see #createDynamicQueryComponent(String, Annotation, String, Class)
     * 
     * @param fld
     * @param parent_
     * @return
     */
    private static HashSet<DynamicQueryComponent> createDynamicQueryComponent(java.lang.reflect.Field fld, String parent_) {
        Class<?> analyzerClass = null;
        HashSet<DynamicQueryComponent> cmpts = new HashSet<>();
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
                    cmpts.add(createDynamicQueryComponent(parent_, ann, label_, analyzerClass));
                }

                if (ann instanceof Fields) {
                    for (Field annField : ((Fields) ann).value()) {
                        cmpts.add(createDynamicQueryComponent(parent_, annField, label_, analyzerClass));
                    }
                }

                if (ann instanceof IndexedEmbedded) {
                    IndexedEmbedded ian = (IndexedEmbedded) ann;
                    String prefix = parent_;

                    // use prefix instead of getter name, if supplied
                    if (!StringUtils.equals(".", ian.prefix())) {
                        prefix = ian.prefix();
                    }

                    Class<?> embedded = ReflectionService.getFieldReturnType(fld);
                    if (embedded == null) {
                        embedded = fld.getType();
                    }
                    logger.trace("IndexedEmbedded on:" + prefix + "." + fld.getName() + " processing " + embedded.getCanonicalName());
                    cmpts.addAll(createFields(embedded, addParent(prefix, fld.getName())));
                }
            }
        }

        return cmpts;
    }

    /**
     * Passes the parent and child, if the parent is null or empty,
     * just return the child, otherwise add the parent and child with the
     * dot notation.
     * 
     * @param parent_
     * @param child
     * @return
     */
    public static String addParent(String parent_, String child) {
        if (StringUtils.isNotBlank(parent_)) {
            // dont' tack on a "." if already there
            String seperator = parent_.endsWith(".") ? "" : ".";
            return parent_ + seperator + child;
        }
        return child;
    }

    /**
     * Hibernate uses some special logic to initialize it's annotations so that in most cases if an analyzer is not specified it gets set
     * to "void" this needs to be tested and "removed" so that we can replace it with the default analyzer specified by the user.
     * 
     * @param analyzerClass
     * @param annCls
     * @return
     */
    private static Class<?> evaluateAnalyzerClass(Class<?> analyzerClass, Analyzer annCls) {
        if (annCls != null) {
            Class<?> impl = annCls.impl();
            // hibSearch defaults to "void" so removing it
            if (!impl.getCanonicalName().equals(VOID))
                return annCls.impl();
        }
        return analyzerClass;
    }

}
