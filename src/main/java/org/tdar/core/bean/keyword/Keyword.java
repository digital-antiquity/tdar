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

import org.apache.lucene.search.Explanation;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.index.analyzer.AutocompleteAnalyzer;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.query.QueryFieldNames;

/**
 * Interface and Abstract Class for all keywords.  Unique entities managed outside of resources, and linked to them.
 *  
 * Base Class for all keywords
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@SuppressWarnings("rawtypes")
public interface Keyword extends Persistable, Indexable, HasLabel, Dedupable {

    @Transient
    public static final String[] IGNORE_PROPERTIES_FOR_UNIQUENESS = { "approved", "selectable", "level", "occurrence" }; // fixme: should ID be here too?

    @Override
    public String getLabel();

    public void setLabel(String label);

    public String getDefinition();

    public String getKeywordType();

    public void setDefinition(String definition);

    @MappedSuperclass
    @XmlType(name = "kwdbase")
    public static abstract class Base<T extends Base<?>> extends Persistable.Base implements Keyword, HasStatus, Comparable<T> {

        private static final long serialVersionUID = -7516574981065004043L;

        @Transient
        private final static String[] JSON_PROPERTIES = { "id", "label" };

        @Column(nullable = false, unique = true)
        @Fields({ @Field(name = "label", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
                @Field(name = "label_auto", norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = AutocompleteAnalyzer.class)),
                @Field(name = "labelKeyword", analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)),
                @Field(name = QueryFieldNames.LABEL_SORT, norms = Norms.NO, store = Store.YES, analyze = Analyze.NO) })
        @Length(max = FieldLength.FIELD_LENGTH_255)
        private String label;

        @Lob
        @Type(type = "org.hibernate.type.StringClobType")
        private String definition;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", length = 25)
        @Field(norms = Norms.NO, store = Store.YES)
        @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
        private Status status = Status.ACTIVE;

        @Field
        @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)
        @Transient
        @Override
        public String getKeywordType() {
            return getClass().getSimpleName();
        }

        private Long occurrence = 0L;

        private transient Float score = -1f;
        private transient Explanation explanation;
        private transient boolean readyToIndex = true;

        // @Column(name = "date_created")
        // @NotNull
        // private Date dateCreated;

        @Transient
        @XmlTransient
        @Override
        public boolean isReadyToIndex() {
            return readyToIndex;
        }

        @Override
        public void setReadyToIndex(boolean readyToIndex) {
            this.readyToIndex = readyToIndex;
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

        @Override
        public String[] getIncludedJsonProperties() {
            return JSON_PROPERTIES;
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

        @Transient
        @XmlTransient
        @Override
        public Explanation getExplanation() {
            return explanation;
        }

        @Override
        public void setExplanation(Explanation explanation) {
            this.explanation = explanation;
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

    }
}
