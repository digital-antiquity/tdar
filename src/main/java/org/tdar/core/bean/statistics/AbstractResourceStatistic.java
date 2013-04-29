package org.tdar.core.bean.statistics;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.tdar.core.bean.Persistable;

@MappedSuperclass
public abstract class AbstractResourceStatistic<S extends Persistable> extends Persistable.Base {

    private static final long serialVersionUID = 2582663062973969024L;

    @NotNull
    @Column(name = "date_accessed")
    private Date date;

    public abstract S getReference();

    public abstract void setReference(S reference);

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
