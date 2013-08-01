package org.tdar.search;

import java.util.Collection;

import org.tdar.core.bean.Indexable;

public interface SearchResultProcessor {

    public <E extends Indexable>void process(E indexable);

    public <E extends Indexable>void processBatch(Collection<E> indexable);

}
