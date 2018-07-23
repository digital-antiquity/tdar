package org.tdar.db.conversion.converters;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.tdar.locale.HasLabel;

public class DataUtil {

    /**
     * Extract the "String" value from an object. This could be a date, URL, enum, or String. If it implements @link HasLabel, use that.
     * 
     * @param val
     * @return
     */
    public static String extractStringValue(Object val) {
        if (val == null) {
            return "";
        } else if (val instanceof HasLabel) {
            return ((HasLabel) val).getLabel();
        } else if (val instanceof Collection<?>) {
            Collection<?> values = (Collection<?>) val;
            StringBuilder sb = new StringBuilder();
            Iterator<?> iter = values.iterator();
            while (iter.hasNext()) {
                String val_ = extractStringValue(iter.next());
                if (StringUtils.isNotBlank(val_)) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(val_);
                }
            }
            return sb.toString();
        } else {
            String string = val.toString();
            if (StringUtils.isNotEmpty(string)) {
                return string;
            }
        }
        return "";
    }
}
