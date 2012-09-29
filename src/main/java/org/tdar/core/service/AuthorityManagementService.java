package org.tdar.core.service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.ReflectionDao;

@Service
public class AuthorityManagementService {

    @Autowired
    ReflectionDao reflectionDao;
    
    @Autowired 
    private ReflectionService reflectionService;
    
    public transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    GenericDao genericDao;
    
    
    //List of classes we will evaluate when looking for references.
    private static List<Class<?>> hostClasses = Arrays.<Class<?>>asList(Resource.class, InformationResource.class, ResourceCreator.class,
            Person.class, Institution.class, AuthorizedUser.class);
    
    
    @Transactional(readOnly=true)
    public Map<Field, ScrollableResults> getReferrers(Class<?> referredClass, List<Long> dupeIds) {
        Map<Field, ScrollableResults>  referrers = new HashMap<Field, ScrollableResults>();
        for(Class<?> targetClass : hostClasses) {
            Set<Field> fields = reflectionService.findAssignableFieldsRefererencingClass(targetClass, referredClass);
            for(Field field : fields) {
                //get the list of referrer to the duplicates via this specific class and field, add it to the pile
                ScrollableResults fieldReferrers = reflectionDao.findReferrers(field, dupeIds);
                logger.trace("The following objects refer to dupeid list via {}:{}", field.getName(), fieldReferrers);
                referrers.put(field, fieldReferrers);
            }
        }
        return referrers;
    }

    @Transactional(readOnly=true)
    public Map<Class<?>, Long> getReferrerCounts(Class<?> referredClass, List<Long> idlist) {
        Map<Class<?>, Long> countMap = new HashMap<Class<?>, Long>();
        for(Class<?> targetClass : hostClasses) {
            Set<Field> fields = reflectionService.findAssignableFieldsRefererencingClass(targetClass, referredClass);
            long classTotal = 0;
            for(Field field : fields) {
                Long count = reflectionDao.getReferrerCount(field, idlist);
                classTotal += count;
            }
            countMap.put(targetClass, classTotal);
        }
        return countMap;
    }
    
    
    @Transactional(readOnly=true)
    public long getTotalReferrerCount(Class<?> referredClass, List<Long> idlist) {
        Map<Class<?>, Long> map = getReferrerCounts(referredClass, idlist);
        long total = 0;
        for(long count : map.values()) {
            total += count;
        }
        return total;
    }
    
    
    //return a map<id, count> of occurancecounts
    //TODO: maybe create a method that returns Map<id, Map<class, count>>
    @Transactional(readOnly=true)
    public Map<Long, Long> getReferrerCountMaps(Class<?> referredClass, List<Long> idlist) {
        Map<Long, Long> countmap = new HashMap<Long, Long>();
        for(Long id : idlist) {
            countmap.put(id, 0L);
        }
        //FIXME: this loop is kludgey and confusing and will surely be fodder for my next code review 
        for(Class<?> targetClass : hostClasses) {
            Set<Field> fields = reflectionService.findAssignableFieldsRefererencingClass(targetClass, referredClass);
            long total = 0;
            for(Field field : fields) {
                List<Map<String, Long>> list = reflectionDao.getReffererCountMap(field, idlist);
                for(Map<String, Long> item : list) {
                    Map<String, Long> map = item;
                    Long id = map.get("id");
                    Long updatedCount = countmap.get(id) + map.get("referenceCount");
                    countmap.put(id, updatedCount);
                }
            }
        }
        return countmap;
    }
    
    
    @Transactional
    //TODO: jim you (probably) aren't handling one-to-many correctly yet.
    /**
     *  Find objects that refer to the specified duplicates and replace the references with the specified authority,
     *  saving the referrers in the process. This method handles objects that may refer to another object via scalar
     *  fields as well as via collection fields.  A few assumptions, restricions:
     *  
     *  - this method assumes that, for collection fields, it is not necessary to perform a piecewise replacement
     *  of each duplicate record with an authority record.  In other words, a collection that contains multiple  
     *  duplicates will be replaced by one (and only one) authority record (if the authority record is not already
     *  in the collection) 
     *  
     *  - all of the potential referring classes must refer to duplicate objects via fields that have public getters
     *  and setters.
     *  
     */
    public <T> void updateReferrers(Class<T> referredClass, List<Long> dupeIds,  Long authorityId) {
        //1 - get a list of all the referrer objects and the Fields that contain the reference.
        Map<Field, ScrollableResults> referrers = getReferrers(referredClass, dupeIds);
        
        //2 - instantiate the duplicates and the authority record
        List<T> dupes = genericDao.find(referredClass, dupeIds);
        T authority = genericDao.find(referredClass, authorityId);
        
        
        //3 - for each reference
        //      -if many-to-many  or one-to-many,  
        //          + get collection getter and remove dupes
        //          + add authority records  to collection
        //          + (this may be unnecessary if many-to-many target deletion implicitly deletes rows in jointable... test this out!)
        //      -if many-to-one
        //          + get scalar setter and set to authority record
        //      -hibsession.save() each reference
        for(Map.Entry<Field, ScrollableResults> entry : referrers.entrySet() ) {
            Field field = entry.getKey();
            ScrollableResults scrollableResults = entry.getValue();
            logger.trace("field:{}", field);
            if(Collection.class.isAssignableFrom(field.getType())) { 
                // remove all dupes from the Collection and add in the authoritative entity (unless it's there already)
                while(scrollableResults.next()) {
                    Object obj = scrollableResults.get(0);
                    Collection<T> collection = reflectionService.callFieldGetter(obj, field);
                    collection.removeAll(dupes);
                    if(!collection.contains(authority)) {
                        collection.add(authority);
                    }
                    genericDao.save(obj);
                }
            } else {
                //if the field is a scalar we just call the setter with the authoritative entity 
                while(scrollableResults.next()) {
                    Object obj = scrollableResults.get(0);
                    logger.debug("using callFieldSetter to set authority::  obj:{}  field:{}  authority:{} ", new Object[]{obj, field, authority});
                    reflectionService.callFieldSetter(obj, field, authority);
                    genericDao.save(obj);
                }
            }
            scrollableResults.close();
        }
         
        //4 - delete each dupe
        genericDao.delete(dupes);
    }
    
    
    
}
