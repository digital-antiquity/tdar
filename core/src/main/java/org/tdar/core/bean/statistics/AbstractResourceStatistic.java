package org.tdar.core.bean.statistics;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.Persistable;

@MappedSuperclass
/**
 * Abtract class to manage download and view statistics 
 * @author abrin
 *
 * @param <S>
 */
public abstract class AbstractResourceStatistic<S extends Persistable> extends AbstractPersistable {

    private static final long serialVersionUID = 2582663062973969024L;

    @NotNull
    @Column(name = "date_accessed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;


    @Column(name="bot", nullable=false, columnDefinition="boolean default false")
    private boolean bot;

    public abstract S getReference();

    public abstract void setReference(S reference);

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isBot() {
        return bot;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }
}
