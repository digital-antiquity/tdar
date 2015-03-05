package org.tdar.struts.data;

import org.tdar.utils.Pair;

/**
 * \
 * bean for holding date range in context of a search.
 * 
 * @author Jim
 * 
 */
public class RangeImpl<D extends Comparable<D>> extends Pair<D, D> implements Range<D> {

    private static final long serialVersionUID = -8495377173625819907L;

    public RangeImpl() {
        super(null, null);
    }

    public RangeImpl(D first, D second) {
        super(first, second);
    }

    @Override
    public D getStart() {
        return getFirst();
    }

    @Override
    public void setStart(D start) {
        setFirst(start);
    }

    @Override
    public D getEnd() {
        return getSecond();
    }

    @Override
    public void setEnd(D end) {
        setSecond(end);
    }

    @Override
    public boolean isValid() {
        if ((getFirst() != null) && (getSecond() != null)) {
            return getFirst().compareTo(getSecond()) <= 0;
        }
        return (getFirst() != null) || (getSecond() != null);
    }

    @Override
    public boolean isValidForController() {
        return isValid();
    }

    @Override
    public boolean isInitialized() {
        if ((getFirst() == null) && (getSecond() == null)) {
            return false;
        }

        return true;
    }

}
