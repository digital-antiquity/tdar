package org.tdar.core.bean.cache;

import org.tdar.core.bean.HasLabel;

/**
 * Interface for Key/Value Pair storage.  It tracks a string or object key, and count of value. It also exposes a cssID that can be used to associate colors or javascript selectors. 
 * @author abrin
 *
 * @param <R>
 */
public interface ResourceCache<R> extends HasLabel {

    public Long getCount();

    public R getKey();

    public Double getLogCount();

    public String getCssId();
}
