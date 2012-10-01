//package org.tdar.utils;
//
//import java.lang.reflect.Field;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//import org.hibernate.collection.internal.PersistentBag;
//import org.hibernate.collection.internal.PersistentSet;
//import org.tdar.utils.db.HibernateCollectionConverter;
//import org.tdar.utils.db.HibernateMapConverter;
//
//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
//import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
//import com.thoughtworks.xstream.mapper.Mapper;
//
///**
// * 
// * @author jim
// */
//public class SimpleSerializer {
//
//    private transient Logger logger = Logger.getLogger(getClass());
//
//    private Map<Class<?>, Set<String>> blacklists = new HashMap<Class<?>, Set<String>>();
//    private Map<Class<?>, Set<String>> whitelists = new HashMap<Class<?>, Set<String>>();
//
//    private XStream xs = new XStream();
//
//    // XStream xsJson = new XStream(new JettisonMappedXmlDriver());
//    private XStream xsJson = new XStream(new JsonHierarchicalStreamDriver());
//
//    private boolean configure;
//
//    public SimpleSerializer() {
//        blacklists = new HashMap<Class<?>, Set<String>>();
//        xsJson.setMode(XStream.NO_REFERENCES);
//        // FIXME: still not accessing the getters properly.
//        // xs.registerConverter(new JavaBeanConverter(xs.getMapper()), XStream.PRIORITY_LOW);
//        // xsJson.registerConverter(new JavaBeanConverter(xsJson.getMapper()), XStream.PRIORITY_LOW);
//    }
//
//    // add all the fields from a class (and all superclasses) to the blacklist
//    public void addToBlacklist(Class<?> type) {
//        blacklists.putAll(getAllFields(type));
//    }
//
//    public void setConfigureForHibernate(boolean configure) {
//        this.configure = configure;
//    }
//
//    private void configureForHibernate() {
//        XStream xs_ = xs;
//        xs_.autodetectAnnotations(true);
//        xs_.addDefaultImplementation(org.hibernate.mapping.List.class, java.util.List.class);
//        xs_.addDefaultImplementation(org.hibernate.mapping.Map.class, java.util.Map.class);
//        xs_.addDefaultImplementation(org.hibernate.mapping.Set.class, java.util.Set.class);
//        xs_.addDefaultImplementation(PersistentSet.class, java.util.Set.class);
//        xs_.addDefaultImplementation(PersistentBag.class, java.util.List.class);
//        Mapper mapper = xs_.getMapper();
//        xs_.registerConverter(new HibernateCollectionConverter(mapper));
//        xs_.registerConverter(new HibernateMapConverter(mapper));
//        xs_.registerConverter(new JavaBeanConverter(mapper), -10);
//    }
//
//    /**
//     * Add the specified fieldnames of the specified class to the whitelist, implicitly blacklisting
//     * all other fields in the class as well superclass fields.
//     * 
//     * @param type
//     *            class containing the fields that the serializer should whitelist
//     * @param fieldnames
//     *            the fieldnames that the serializer should whitelist
//     */
//    public void justAddToWhitelist(Class<?> type, String... fieldnames) {
//        for (String field : fieldnames) {
//            justAddToWhitelist(type, field);
//        }
//    }
//
//    /**
//     * Add the specified fieldnames of the specified class to the whitelist, implicitly blacklisting
//     * all other fields in the class as well superclass fields.
//     * 
//     * @param type
//     *            class containing the fields that the serializer should whitelist
//     * @param fieldnames
//     *            the fieldnames that the serializer should whitelist
//     */
//    public void addToWhitelist(Class<?> type, String... fieldnames) {
//        addToBlacklist(type); // start by adding all fields in this class hierarchy into the blacklist
//        for (String field : fieldnames) {
//            justAddToWhitelist(type, field);
//        }
//    }
//
//    /**
//     * add to whitelist without touching the blacklist
//     * 
//     * @param type
//     * @param fieldName
//     */
//    public void justAddToWhitelist(Class<?> type, String fieldName) {
//        if (!whitelists.containsKey(type))
//            whitelists.put(type, new HashSet<String>());
//        whitelists.get(type).add(fieldName);
//        logger.trace("whitelists:" + whitelists);
//    }
//
//    /**
//     * @param type
//     * @param addSuperclassToBlackList
//     *            If true, add superclass fields to blacklist. otherwise,
//     *            leave the blacklist untouched.
//     */
//    public void addAllToWhitelist(Class<?> type, boolean addSuperclassesToBlackList) {
//        if (addSuperclassesToBlackList && type.getSuperclass() != null && !Object.class.equals(type.getSuperclass())) {
//            addToBlacklist(type.getSuperclass());
//        }
//        addAllToWhitelist(type);
//        addAllToWhitelist(type.getSuperclass(), type);
//    }
//
//    /**
//     * Add all fields in the specified type to the whitelist. This call does not affect the blacklist
//     * 
//     * @param type
//     */
//    private void addAllToWhitelist(Class<?> type, Class<?> typeOverride) {
//        for (Field field : type.getDeclaredFields()) {
//            justAddToWhitelist(type, field.getName());
//        }
//    }
//
//    public void addAllToWhitelist(Class<?> type) {
//        addAllToWhitelist(type, type);
//    }
//
//    /**
//     * replace a full classname with an alias when serializing
//     * 
//     * @param tagName
//     *            replacement tagname to use
//     * @param type
//     *            class to rename
//     */
//    public void alias(String tagName, Class<?> type) {
//        xs.alias(tagName, type);
//        xsJson.alias(tagName, type);
//    }
//
//    public Object fromXML(String s) {
//        XStream xs = new XStream();
//        return xs.fromXML(s);
//    }
//
//    public String toXml(Object obj) {
//        updateWhiteList();
//        if (configure)
//            configureForHibernate();
//        return xs.toXML(obj);
//    }
//
//    public String toJson(Object obj) {
//        updateWhiteList();
//        if (configure)
//            configureForHibernate();
//        return xsJson.toXML(obj);
//    }
//
//    private void updateWhiteList() {
//        // XStream xs = new XStream();
//        logger.debug("serializing with whitelist:" + whitelists);
//        // xstream only deals in blacklists, so to support a whitelist we omit every field *except* those for whitelist items
//        logger.trace("blacklist:" + blacklists);
//        for (Entry<Class<?>, Set<String>> entry : blacklists.entrySet()) {
//            logger.trace("scanning " + entry.getKey().getName() + " fields for whitelisted items");
//            Set<String> whiteListfields = whitelists.get(entry.getKey());
//            if (whiteListfields != null) {
//                for (String fieldName : entry.getValue()) {
//                    if (!whiteListfields.contains(fieldName)) {
//                        xs.omitField(entry.getKey(), fieldName);
//                        xsJson.omitField(entry.getKey(), fieldName);
//                        logger.trace("omitting " + entry.getKey() + "::" + fieldName);
//                    }
//                    else {
//                        logger.trace("whitelisting " + entry.getKey() + "::" + fieldName);
//                    }
//                }
//            }
//            else {
//                // no whitelist specified for this class. so omit all fields in this class blacklist
//                for (String fieldName : entry.getValue()) {
//                    xs.omitField(entry.getKey(), fieldName);
//                    xsJson.omitField(entry.getKey(), fieldName);
//                }
//                logger.trace("omitting all fields in " + entry.getKey());
//            }
//        }
//    }
//
//    private static Map<Class<?>, Set<String>> getAllFields(Class<?> type) {
//        Set<String> fields = new HashSet<String>();
//        Map<Class<?>, Set<String>> map = new HashMap<Class<?>, Set<String>>();
//        for (Field field : type.getDeclaredFields()) {
//            fields.add(field.getName());
//        }
//        map.put(type, fields);
//        if (type.getSuperclass() != null && !Object.class.equals(type.getSuperclass())) {
//            map.putAll(getAllFields(type.getSuperclass()));
//        }
//        return map;
//    }
//}
