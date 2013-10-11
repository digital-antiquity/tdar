package org.tdar.search;

import java.util.Collection;

import org.tdar.core.bean.Indexable;

public interface SearchResultProcessor {

    <E extends Indexable> void process(E indexable);

    <E extends Indexable> void processBatch(Collection<E> indexable);

}
