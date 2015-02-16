package org.tdar.utils;

import java.io.Serializable;
import java.util.Iterator;

/**
 * wrapper for scrollable result set
 */
import org.hibernate.ScrollableResults;

public class ScrollableIterable<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID = 5208534726931139828L;
    private ScrollableResults scrollable;

    public ScrollableIterable(ScrollableResults sr) {
        this.scrollable = sr;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return scrollable.next();
            }

            @SuppressWarnings("unchecked")
            @Override
            public T next() {
                return (T)scrollable.get(0);
            }
        };
    }

}
