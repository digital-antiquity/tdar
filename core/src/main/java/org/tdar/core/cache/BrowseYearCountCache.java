package org.tdar.core.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * This caches the counts of resources-per-year for the browse page.
 * 
 * @author abrin
 * 
 */
@JsonAutoDetect
public class BrowseYearCountCache extends AbstractCountCache<BrowseYearCountCache, Integer> {

    private static final long serialVersionUID = -1407077845657074783L;

    private Long count;

    private Integer key;

    @SuppressWarnings("unused")
    private BrowseYearCountCache() {

    }

    public BrowseYearCountCache(Integer year, Long count) {
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