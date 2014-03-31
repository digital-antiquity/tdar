package org.tdar.core.bean.cache;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;

/**
 * This caches the count of geographic keywords for the world map on the homepage.
 * 
 * @author abrin
 *
 */
@Entity
@Table(name = "homepage_cache_geographic_keyword")
public class HomepageGeographicKeywordCache extends Base implements ResourceCache<String> {

    private static final long serialVersionUID = -8037868535122993612L;

    @Column(name = "resource_count")
    private Long count;

    @Column(name = "keyword_id")
    private Long keywordId;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(length = FieldLength.FIELD_LENGTH_50)
    private Level level;

    
    private transient double totalLogCount = 0d;
    private transient long totalCount = 0l;
    
    public HomepageGeographicKeywordCache() {

    }

    public HomepageGeographicKeywordCache(String label, Level level, Long count, Long id) {
        this.label = label;
        this.level = level;
        this.count = count;
        this.keywordId = id;
    }

    @Override
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public Double getLogCount() {
        return Math.log(getCount());
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s)", label, level, count);
    }

    @Override
    public String getKey() {
        return getLabel().substring(0,2);
    }

    @Override
    public String getCssId() {
        return this.getKey().toString();
    }

    public Long getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(Long keywordId) {
        this.keywordId = keywordId;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public double getTotalLogCount() {
        return totalLogCount;
    }

    public void setTotalLogCount(double totalLogCount) {
        this.totalLogCount = totalLogCount;
    }

    public int getColorGroup() {
        int percent = (int) Math.floor((getLogCount().doubleValue() / getTotalLogCount()) * 100d);
        if (percent < 9) {
            return 1;
        }
        if (percent < 16) {
            return 2;
        }
        if (percent < 31) {
            return 3;
        }
        if (percent < 45) {
            return 4;
        }
        if (percent < 60) {
            return 5;
        }
        if (percent < 76) {
            return 6;
        }
        return 8;

    }
}
