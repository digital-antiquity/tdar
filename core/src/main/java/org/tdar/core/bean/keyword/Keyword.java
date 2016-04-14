package org.tdar.core.bean.keyword;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.RelationType;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * Interface and Abstract Class for all keywords. Unique entities managed outside of resources, and linked to them.
 * 
 * Base Class for all keywords
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@SuppressWarnings("rawtypes")
public interface Keyword extends Persistable, Indexable, HasLabel, Dedupable, Addressable {

    @Transient
    public static final String[] IGNORE_PROPERTIES_FOR_UNIQUENESS = { "approved", "selectable", "level", "code", "occurrence" }; // fixme: should ID be here too?

    @Override
    public String getLabel();

    String getSlug();

    public void setLabel(String label);

    public String getDefinition();

    public String getKeywordType();

    public void setDefinition(String definition);

    @MappedSuperclass
    @XmlType(name = "kwdbase")
    @XmlTransient
    public static abstract class Base<T extends Base<?>> extends Persistable.Base implements Keyword, HasStatus, Comparable<T>, Slugable {

        private static final long serialVersionUID = -7516574981065004043L;

        @Column(nullable = false, unique = true)
        @Length(max = FieldLength.FIELD_LENGTH_255)
        @JsonView(JsonLookupFilter.class)
        private String label;

        @Lob
        @Type(type = "org.hibernate.type.TextType")
        private String definition;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", length = FieldLength.FIELD_LENGTH_25)
        private Status status = Status.ACTIVE;

//        @Column(name="relation")
//        @Length(max= FieldLength.FIELD_LENGTH_2048)
//        private String relation;
//
//        @Enumerated(EnumType.STRING)
//        @Column(name="relation_type")
//        private RelationType relationType;

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

        private transient Float score = -1f;

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
            this.label = label;
        }

        @XmlTransient
        @Override
        public String getDefinition() {
            return definition;
        }

        @Override
        public void setDefinition(String definition) {
            this.definition = definition;
        }

        @Override
        public String toString() {
            return getLabel();
        }

        @Transient
        @Override
        @XmlTransient
        public Float getScore() {
            return score;
        }

        @Override
        public void setScore(Float score) {
            this.score = score;
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

        @Override
        public boolean isActive() {
            return this.status == Status.ACTIVE;
        }

        @Override
        public boolean isDeleted() {
            return this.status == Status.DELETED;
        }

        @Override
        public boolean isDraft() {
            return status == Status.DRAFT;
        }

        @Override
        public boolean isFlagged() {
            return status == Status.FLAGGED;
        }

        @Override
        public boolean isDuplicate() {
            return status == Status.DUPLICATE;
        }

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

//        public RelationType getRelationType() {
//            return relationType;
//        }
//
//        public void setRelationType(RelationType relationType) {
//            this.relationType = relationType;
//        }
//
//        public String getRelation() {
//            return relation;
//        }
//
//        public void setRelation(String relation) {
//            this.relation = relation;
//        }
    }
}
