package org.tdar.core.bean;

import org.apache.commons.lang3.StringUtils;

/**
 * Allows abstraction for objects that can be sorted.
 * 
 * @author abrin
 * 
 */
public interface Sortable {

    public static final String TITLE_SORT_REGEX = "^([\\s\\W]|The |A |An )+";

    SortOption getSortBy();

    static String getTitleSort(String title) {
        if (StringUtils.isBlank(title)) {
            return null;
        }
        return title.toLowerCase().replaceAll(TITLE_SORT_REGEX, "");
    }
}
