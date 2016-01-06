package org.tdar.tag.bean;

import java.util.List;

public interface QueryMapper<K> {
    List<String> findMappedValues(K key);
}
