package org.tdar.core.bean.statistics;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.tdar.core.bean.entity.Creator;

@Entity
@Table(name = "creator_view_statistics", indexes = {
        @Index(name = "creator_view_stats_count_id", columnList = "creator_id, id")
})
public class CreatorViewStatistic extends AbstractResourceStatistic<Creator<?>> {

    private static final long serialVersionUID = 2438505951349936637L;
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "creator_id")
    private Creator<?> reference;

    public CreatorViewStatistic() {
    };

    public CreatorViewStatistic(Date date, Creator<?> r, boolean isBot) {
        setDate(date);
        setReference(r);
        setBot(isBot);
    }

    @Override
    public Creator<?> getReference() {
        return reference;
    }

    @Override
    public void setReference(Creator<?> reference) {
        this.reference = reference;
    }

}
