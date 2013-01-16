package org.tdar.struts.data;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.Validatable;
import org.tdar.utils.Pair;

/**
 * Similar to StringRange. It would be simpler to have a Range<T> class, but struts is unable to guess the type argument when typeconverting to a list of
 * ranges.
 * 
 * @author jimdevos
 * 
 */
public class StringRange extends Pair<String, String> implements Range<String>, Validatable {

    public StringRange() {
        super(null, null);
    }

    public StringRange(String first, String second) {
        super(first, second);
    }

    public String getStart() {
        return getFirst();
    }

    public void setStart(String start) {
        setFirst(start);
    }

    public String getEnd() {
        return getSecond();
    }

    public void setEnd(String end) {
        setSecond(end);
    }

    @Override
    // range is valid if only one value is nonblank (an open-ended range), or if both values are nonblank and start <= end
    public boolean isValid() {
        if (StringUtils.isBlank(getFirst()) && StringUtils.isBlank(getSecond()))
            return false;

        if (StringUtils.isNotBlank(getFirst()) && StringUtils.isNotBlank(getSecond())) {
            return getFirst().compareTo(getSecond()) <= 0;
        }
        return StringUtils.isNotBlank(getFirst()) || StringUtils.isNotBlank(getSecond());
    }

    public boolean isInitialized() {
        if (StringUtils.isBlank(getFirst()) && StringUtils.isBlank(getSecond()))
            return false;

        return true;
    }
    
    @Override
    public boolean isValidForController() {
        return isValid();
    }

}
