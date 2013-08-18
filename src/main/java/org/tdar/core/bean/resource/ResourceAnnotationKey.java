package org.tdar.core.bean.resource;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.lucene.search.Explanation;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.search.index.analyzer.AutocompleteAnalyzer;

/**
 * $Id$
 * 
 * Semi-controlled list of possible resource identifier keys.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "resource_annotation_key")
@Indexed(index = "AnnotationKey")
public class ResourceAnnotationKey extends Persistable.Base implements Indexable, HasLabel {

    private static final long serialVersionUID = 6596067112791213904L;

    @Transient
    private final static String[] JSON_PROPERTIES = { "id", "key", "label" };

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_annotation_type", length=255)
    private ResourceAnnotationType resourceAnnotationType;

    // FIXME: convert to enum for control? if we want to eventually add
    // format strings then we need to capture that format string, e.g., "###-###-####" for phone numbers
    // or "xxx-xx" for arbitrary strings... maybe we can avoid this entirely.
    @Enumerated(EnumType.STRING)
    @Column(name = "annotation_data_type", length = 255)
    private ResourceAnnotationDataType annotationDataType;

    @Column(length = 128, unique = true,nullable=false)
    @Fields({ @Field(name = "annotationkey_auto", norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = AutocompleteAnalyzer.class)) })
    @Length(max = 128)
    private String key;

    @Column(length = 128, name = "format_string")
    @Length(max = 128)
    private String formatString;

    private transient Float score = -1f;
    private transient Explanation explanation;
    private transient boolean readyToIndex = true;

    @Transient
    @XmlTransient
    public boolean isReadyToIndex() {
        return readyToIndex;
    }

    public void setReadyToIndex(boolean readyToIndex) {
        this.readyToIndex = readyToIndex;
    }

    @XmlAttribute
    public ResourceAnnotationType getResourceAnnotationType() {
        return resourceAnnotationType;
    }

    public void setResourceAnnotationType(ResourceAnnotationType resourceAnnotationType) {
        this.resourceAnnotationType = resourceAnnotationType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @XmlAttribute
    public ResourceAnnotationDataType getAnnotationDataType() {
        return annotationDataType;
    }

    public void setAnnotationDataType(ResourceAnnotationDataType annotationDataType) {
        this.annotationDataType = annotationDataType;
    }

    public String format(String value) {
        // FIXME: not applying format strings yet.
        return value;
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    @Override
    public String toString() {
        return "[key:'" + key + "' id:" + getId() + "]";
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        return JSON_PROPERTIES;
    }

    @Override
    public List<?> getEqualityFields() {
        //ab probably okay as not nullable fields
        return Arrays.asList(key);
    }

    @Transient
    @XmlTransient
    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    @Transient
    @XmlTransient
    public Explanation getExplanation() {
        return explanation;
    }

    public void setExplanation(Explanation explanation) {
        this.explanation = explanation;
    }

    @Transient
    public String getLabel() {
        return this.key;
    }
}
