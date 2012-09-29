package org.tdar.core.bean.util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;

@Entity
@Table(name="homepage_cache_geographic_keyword")
public class HomepageGeographicKeywordCache extends Base {

    private static final long serialVersionUID = -8037868535122993612L;

    @Column(name = "resource_count")
    private Long count;

    private String label;

    @Enumerated(EnumType.STRING)
    private Level level;

    public HomepageGeographicKeywordCache() {

    }

    public HomepageGeographicKeywordCache(String label, Level level, Long count) {
        this.label = label;
        this.level = level;
        this.count = count;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

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
    
    public Double getLogCount() {
        return Math.log(getCount());
    }

}
