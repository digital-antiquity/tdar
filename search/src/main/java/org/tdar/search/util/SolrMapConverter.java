package org.tdar.search.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.common.util.NamedList;

public class SolrMapConverter {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object toMap(Object entry) {
        Object response = null;
        if (entry instanceof NamedList) {
            response = new HashMap<>();
            NamedList lst = (NamedList) entry;
            for (int i = 0; i < lst.size(); i++) {
                ((Map) response).put(lst.getName(i), toMap(lst.getVal(i)));
            }
        } else if (entry instanceof Iterable) {
            response = new ArrayList<>();
            for (Object e : (Iterable) entry) {
                ((ArrayList<Object>) response).add(toMap(e));
            }
        } else if (entry instanceof Map) {
            response = new HashMap<>();
            for (Entry<String, ?> e : ((Map<String, ?>) entry).entrySet()) {
                ((Map) response).put(e.getKey(), toMap(e.getValue()));
            }
        } else {
            return entry;
        }
        return response;
    }
}
