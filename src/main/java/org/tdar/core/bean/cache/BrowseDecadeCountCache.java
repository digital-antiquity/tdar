package org.tdar.core.bean.cache;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This caches the resource-count-per-decade on the homepage.
 * 
 * @author abrin
 * 
 */
@Entity
@Table(name = "explore_cache_decade")
public class BrowseDecadeCountCache extends AbstractCountCache<BrowseDecadeCountCache, Integer> {

    private static final long serialVersionUID = -1407077845657074783L;

    @Column(name = "item_count")
    private Long count;

    @Column(name = "key")
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