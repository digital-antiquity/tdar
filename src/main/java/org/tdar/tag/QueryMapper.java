package org.tdar.tag;

import java.util.List;

public interface QueryMapper<K> {
    public List<String> findMappedValues(K key);
}
