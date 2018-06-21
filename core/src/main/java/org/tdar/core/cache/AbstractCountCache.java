package org.tdar.core.cache;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;
import org.tdar.db.conversion.converters.DataUtil;

/**
 * Abstract class to help manage the cache data.
 * 
 * @author abrin
 * 
 * @param <C>
 * @param <D>
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractCountCache<C extends AbstractCountCache, D extends Comparable<D>> implements Comparable<C>,
        ResourceCache, Serializable {

    private static final long serialVersionUID = 6182074332493898892L;

    @Override
    public String getLabel() {
        return DataUtil.extractStringValue(getKey());
    }

    @Override
    public Double getLogCount() {
        return Math.log(getCount());
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(C o) {
        return ObjectUtils.compare((C) getKey(), (o.getKey()));
    }

    @Override
    public abstract D getKey();

    public abstract void setKey(D key);

}
