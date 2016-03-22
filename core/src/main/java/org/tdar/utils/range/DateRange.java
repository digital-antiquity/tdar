package org.tdar.utils.range;

import java.util.Date;

import org.tdar.utils.Pair;

/**
 * \
 * bean for holding date range in context of a search.
 * 
 * @author Jim
 * 
 */
public class DateRange extends Pair<Date, Date> implements Range<Date> {

    private static final long serialVersionUID = -8495377173625819907L;

    public DateRange() {
        super(null, null);
    }

    public DateRange(Date first, Date second) {
        super(first, second);
    }

    @Override
    public Date getStart() {
        return getFirst();
    }

    @Override
    public void setStart(Date start) {
        setFirst(start);
    }

    @Override
    public Date getEnd() {
        return getSecond();
    }

    @Override
    public void setEnd(Date end) {
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
