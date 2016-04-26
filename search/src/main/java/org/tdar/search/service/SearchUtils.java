package org.tdar.search.service;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.Persistable;

public class SearchUtils {

    /**
     * Return true if the specified string meets the minimum length requirement (or if there is no minimum length requirement). Not to be confused w/
     * checking if the specified string is blank.
     */
    public static boolean checkMinString(String value, int length) {
        if (length == 0) {
            return true;
        }
        return StringUtils.isNotEmpty(value) && (value.trim().length() >= length);
    }

    public static String createKey(Persistable pers) {
        return pers.getClass().getSimpleName() + "-" + pers.getId();
    }

}
