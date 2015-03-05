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
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.search.index.analyzer.AutocompleteAnalyzer;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

@MappedSuperclass
@XmlType(name = "kwdbase")
@XmlTransient
@Indexed(index = "Keyword")
public abstract class KeywordBase<T extends KeywordBase<?>> extends Persistable.Base implements Keyword, HasStatus, Comparable<T>, Slugable {

        private static final long serialVersionUID = -7516574981065004043L;

        @Column(nullable = false, unique = true)
        @Fields({ @Field(name = "label", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
                @Field(name = "label_auto", norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = AutocompleteAnalyzer.class)),
                @Field(name = "labelKeyword", analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)),
                @Field(name = QueryFieldNames.LABEL_SORT, norms = Norms.NO, store = Store.YES, analyze = Analyze.NO) })
        @Length(max = FieldLength.FIELD_LENGTH_255)
        @JsonView(JsonLookupFilter.class)
        private String label;

        @Lob
        @Type(type = "org.hibernate.type.StringClobType")
        private String definition;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", length = FieldLength.FIELD_LENGTH_25)
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

        @Override
        public String getSlug() {
            return UrlUtils.slugify(getLabel());
        }
        
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

        @JsonView(JsonLookupFilter.class)
        public String getDetailUrl() {
            return String.format("/%s/%s/%s", getUrlNamespace(), getId(), getSlug());
        }
}
