package org.tdar.core.cache;

import org.tdar.locale.HasLabel;

/**
 * Interface for Key/Value Pair storage. It tracks a string or object key, and count of value. It also exposes a cssID that can be used to associate colors or
 * javascript selectors.
 * 
 * @author abrin
 * 
 * @param <R>
 */
public interface ResourceCache<R> extends HasLabel {

    Long getCount();

    R getKey();

    Double getLogCount();

    String getCssId();
}
