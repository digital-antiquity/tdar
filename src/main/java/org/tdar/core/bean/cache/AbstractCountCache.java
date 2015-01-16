package org.tdar.core.bean.cache;

import org.apache.commons.lang3.ObjectUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.utils.DataUtil;

/**
 * Abstract class to help manage the cache data.
 * 
 * @author abrin
 * 
 * @param <C>
 * @param <D>
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractCountCache<C extends AbstractCountCache, D extends Comparable<D>> extends Persistable.Base implements Comparable<C>,
        ResourceCache {

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
        return ObjectUtils.compare(getKey(), (o.getKey()));
    }

    @Override
    public abstract D getKey();

    public abstract void setKey(D key);

}
