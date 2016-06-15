package org.tdar.core.bean.resource;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.utils.json.JsonProjectLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * <p>
 * ResourceNotes allow for free-text notes about a resource.
 * 
 * @author Adam Brin
 * @version $Revision$
 */

@Entity
@Table(name = "resource_note", indexes = {
        @Index(name = "resid_noteid", columnList = "resource_id, id") })
public class ResourceNote extends Persistable.Sequence<ResourceNote> implements HasResource<Resource> {

    private static final long serialVersionUID = -3161092447810925887L;

    @Column(length = FieldLength.FIELD_LENGTH_5000)
    //@Field
    @Length(max = FieldLength.FIELD_LENGTH_5000)
    @JsonView(JsonProjectLookupFilter.class)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", length = FieldLength.FIELD_LENGTH_255)
    //@Field(norms = Norms.NO, store = Store.YES, analyzer = //@Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class))
    @JsonView(JsonProjectLookupFilter.class)
    private ResourceNoteType type;

    @Override
    public java.util.List<?> getEqualityFields() {
        return Arrays.asList(type, note);
    };

    public ResourceNote() {
    }

    public ResourceNote(ResourceNoteType type, String note) {
        this.type = type;
        this.note = note;
    }

    public String getNote() {
        if (note == null) {
            return "";
        }
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @XmlAttribute
    public ResourceNoteType getType() {
        if (type == null) {
            return ResourceNoteType.GENERAL;
        }
        return type;
    }

    public void setType(ResourceNoteType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return getType().getLabel() + ":" + getNote();
    }

    @Override
    public boolean isValid() {
        if ((type != null) && !StringUtils.isEmpty(note)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isValidForController() {
        return true;
    }

}
