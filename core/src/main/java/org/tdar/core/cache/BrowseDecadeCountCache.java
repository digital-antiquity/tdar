package org.tdar.core.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * This caches the resource-count-per-decade on the homepage.
 * 
 * @author abrin
 * 
 */
@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
@JsonInclude(Include.NON_NULL)
public class BrowseDecadeCountCache extends AbstractCountCache<BrowseDecadeCountCache, Integer> {

    private static final long serialVersionUID = -5905078214966414970L;

    private Long count;

    private Integer key;

    @SuppressWarnings("unused")
    private BrowseDecadeCountCache() {

    }

    public BrowseDecadeCountCache(Integer year, Long count) {
        this.setKey(year);
        setCount(count);
    }

    @Override
    public String getCssId() {
        return this.getClass().getSimpleName() + this.getKey().toString();
    }

    @Override
    public Integer getKey() {
        return key;
    }

    @Override
    public void setKey(Integer key) {
        this.key = key;
    }

    @Override
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}