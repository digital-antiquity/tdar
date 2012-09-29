package org.tdar.core.bean.cache;

import org.apache.commons.lang.ObjectUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.service.GenericService;

public abstract class AbstractCountCache<C extends AbstractCountCache, D extends Comparable<D>> extends Persistable.Base implements Comparable<C>,
        ResourceCache {

    private static final long serialVersionUID = -1407077845657074783L;

    public String getLabel() {
        return GenericService.extractStringValue(getKey());
    }

    public Double getLogCount() {
        return Math.log(getCount());
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(C o) {
        return ObjectUtils.compare(getKey(), (D) (o.getKey()));
    }

    public abstract D getKey();

    public abstract void setKey(D key);

}
