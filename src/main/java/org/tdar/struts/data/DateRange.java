package org.tdar.struts.data;

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

    public DateRange() {
        super(null, null);
    }

    public DateRange(Date first, Date second) {
        super(first, second);
    }

    public Date getStart() {
        return getFirst();
    }

    public void setStart(Date start) {
        setFirst(start);
    }

    public Date getEnd() {
        return getSecond();
    }

    public void setEnd(Date end) {
        setSecond(end);
    }

    @Override
    public boolean isValid() {
        if (getFirst() != null && getSecond() != null) {
            return getFirst().compareTo(getSecond()) <= 0;
        }
        return getFirst() != null || getSecond() != null;
    }

    @Override
    public boolean isValidForController() {
        return isValid();
    }

    public boolean isInitialized() {
        if (getFirst() == null && getSecond() == null)
            return false;

        return true;
    }

}
