package org.tdar.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Like a priority queue, without the priority (i.e. push and poll require a key)
 * 
 * @author Jim deVos
 * 
 * @param <K>
 *            key type
 * @param <V>
 *            value type
 */
public class HashQueue<K, V> {

    // TODO: size(), hasKey(), hasValues()

    private Map<K, LinkedList<V>> queueMap = new HashMap<K, LinkedList<V>>();

    // add to the end of the list of the specified
    public void push(K key, V value) {
        if (queueMap.get(key) == null) {
            queueMap.put(key, new LinkedList<V>());
        }
        queueMap.get(key).add(value);
    }

    // pull from start of list
    public V poll(K key) {
        V value = null;
        LinkedList<V> queue = queueMap.get(key);
        if (queue != null) {
            value = queue.poll();
        }
        return value;
    }

    // sort the elements within their respective queue
    public void sort(Comparator<V> c) {
        for (Map.Entry<K, LinkedList<V>> entry : queueMap.entrySet()) {
            Collections.sort(entry.getValue(), c);
        }
    }

}
