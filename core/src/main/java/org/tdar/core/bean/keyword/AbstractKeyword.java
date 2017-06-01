package org.tdar.core.bean.keyword;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

@MappedSuperclass
@XmlType(name = "kwdbase")
@XmlTransient
public abstract class AbstractKeyword<T extends Keyword> extends AbstractPersistable implements Keyword, HasStatus, Comparable<T>, Slugable {

    private static final long serialVersionUID = -7516574981065004043L;

    @Column(nullable = false, unique = true)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private String label;

    @OneToMany(orphanRemoval = true)
    // note related_keyword_id is not used,it's overriden in the local class file
    @JoinColumn(nullable = false, updatable = false)
    @JsonView(JsonLookupFilter.class)
    private Set<ExternalKeywordMapping> assertions = new HashSet<>();

    @OneToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST })
    @JoinColumn(name = "merge_keyword_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<T> synonyms = new HashSet<T>();

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String definition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = FieldLength.FIELD_LENGTH_25)
    private Status status = Status.ACTIVE;

    @Transient
    @Override
    public String getKeywordType() {
        return getClass().getSimpleName();
    }

    private Long occurrence = 0L;

    @Override
    public String getSlug() {
        return UrlUtils.slugify(getLabel());
    }

    @Override
    public int compareTo(T o) {
        return this.getLabel().compareTo(o.getLabel());
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = StringUtils.trimToEmpty(label);
    }

    @XmlTransient
    @Override
    public String getDefinition() {
        return definition;
    }

    @Override
    public void setDefinition(String definition) {
        this.definition = StringUtils.trimToEmpty(definition);
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public boolean isDedupable() {
        return true;
    }

    @XmlAttribute
    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }


    @JsonView(JsonLookupFilter.class)
    public Long getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(Long occurrence) {
        this.occurrence = occurrence;
    }

    @JsonView(JsonLookupFilter.class)
    public String getDetailUrl() {
        return String.format("/%s/%s/%s", getUrlNamespace(), getId(), getSlug());
    }

    @Override
    public Set<T> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<T> synonyms) {
        this.synonyms = synonyms;
    }

    @XmlElementWrapper(name = "assertions")
    @XmlElement(name = "assertion")
    public Set<ExternalKeywordMapping> getAssertions() {
        return assertions;
    }

    public void setAssertions(Set<ExternalKeywordMapping> externalMappings) {
        this.assertions = externalMappings;
    }

    public String getSynonymFormattedName() {
        return getLabel();
    }


    @Override
    @Transient
    @XmlTransient
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isDraft() {
        return status == Status.DRAFT;
    }

    @Override
    public boolean isDuplicate() {
        return status == Status.DUPLICATE;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isFlagged() {
        return status == Status.FLAGGED;
    }

}
