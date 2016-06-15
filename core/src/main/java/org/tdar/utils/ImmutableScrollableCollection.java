package org.tdar.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;
/**
 * wrapper for scrollable result set
 */
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a mock interface for a collection that backs a ScrollableResultSet so we can index a project's contents. This is less than ideal, but it appears
 * necessary for HibernateSearch to index a collection properly and to manage memory intelligently.
 * 
 * @author abrin
 *
 * @param <T>
 */
public class ImmutableScrollableCollection<T> implements Collection<T>, Serializable {

    private final class ScrollableResultSetIterator implements Iterator<T> {
        private ScrollableResults scrollable;
        private final Logger logger = LoggerFactory.getLogger(getClass());

        public ScrollableResultSetIterator(ScrollableResults results) {
            this.scrollable = results;
        }

        @Override
        public boolean hasNext() {
            logger.trace("hasNext() called");
            return scrollable.next();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            T t = (T) scrollable.get(0);
            if (logger.isTraceEnabled()) {
                logger.trace("next() called: {}", t);
            }
            return t;
        }

        @Override
        public void remove() {
            logger.trace("remove() called");
            throw new NotImplementedException();
        }
    }

    private static final long serialVersionUID = 5208534726931139828L;
    private final ScrollableResultSetIterator iterator;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ImmutableScrollableCollection(ScrollableResults sr) {
        this.iterator = new ScrollableResultSetIterator(sr);
    }

    @Override
    public String toString() {
        return "ScrollableResultSet Set";
    };

    @Override
    public Iterator<T> iterator() {
        logger.trace("iterator called...");
        // could try to be intelligent and reset the scrollable to the beginning at the point of iterator being called
        // iterator.scrollable.beforeFirst();
        return iterator;
    }

    @Override
    public boolean add(T e) {
        logger.debug("add() called");
        throw new NotImplementedException();
    }

    @Override
    public void clear() {
        logger.debug("clear() called");
        throw new NotImplementedException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        logger.debug("addAll() called");
        throw new NotImplementedException();
    }

    @Override
    public boolean contains(Object o) {
        logger.debug("contains() called");
        throw new NotImplementedException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        logger.debug("containsAll() called");
        throw new NotImplementedException();
    }

    @Override
    public boolean isEmpty() {
        logger.debug("isEmpty() called");
        throw new NotImplementedException();
    }

    @Override
    public boolean remove(Object o) {
        logger.debug("remove() called");
        throw new NotImplementedException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        logger.debug("removeAll() called");
        throw new NotImplementedException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        logger.debug("retainAll() called");
        throw new NotImplementedException();
    }

    @Override
    public int size() {
        logger.debug("size() called");
        throw new NotImplementedException();
    }

    @Override
    public Object[] toArray() {
        logger.debug("toArray() called");
        throw new NotImplementedException();
    }

    public void reset() {
        iterator.scrollable.beforeFirst();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        logger.debug("toArray() called");
        throw new NotImplementedException();
    }

}
