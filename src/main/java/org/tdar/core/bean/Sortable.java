package org.tdar.core.bean;

import org.tdar.search.query.SortOption;

/**
 * Allows abstraction for objects that can be sorted.
 * @author abrin
 *
 */
public interface Sortable {

    SortOption getSortBy();
}
