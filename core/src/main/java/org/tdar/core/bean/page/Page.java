package org.tdar.core.bean.page;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.page.blocks.AbstractBlock;

@Entity
@Table(name = "page")
public class Page extends AbstractPersistable implements HasName {

    private static final long serialVersionUID = 2024916120938549308L;

    @Length(max = FieldLength.FIELD_LENGTH_500, min = 1)
    @NotNull
    private String name;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(name = "creator_id", nullable = false)
    private TdarUser creator;

    @Column(nullable = false, name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(name = "updator_id", nullable = false)
    private TdarUser updator;

    @Column(nullable = false, name = "date_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateUpdated;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "page_id")
    private List<AbstractBlock> parts = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TdarUser getCreator() {
        return creator;
    }

    public void setCreator(TdarUser creator) {
        this.creator = creator;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public TdarUser getUpdator() {
        return updator;
    }

    public void setUpdator(TdarUser updator) {
        this.updator = updator;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public List<AbstractBlock> getParts() {
        return parts;
    }

    public void setParts(List<AbstractBlock> parts) {
        this.parts = parts;
    }

}
