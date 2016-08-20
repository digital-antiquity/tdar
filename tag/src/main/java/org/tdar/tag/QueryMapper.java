package org.tdar.tag;

import java.util.List;

public interface QueryMapper<K> {
    List<String> findMappedValues(K key);
}
