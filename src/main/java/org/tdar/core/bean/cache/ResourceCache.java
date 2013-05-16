package org.tdar.core.bean.cache;

import org.tdar.core.bean.HasLabel;

public interface ResourceCache<R> extends HasLabel {

    public Long getCount();
    
    public R getKey();
    
    public Double getLogCount();
 
    public String getCssId();
}
