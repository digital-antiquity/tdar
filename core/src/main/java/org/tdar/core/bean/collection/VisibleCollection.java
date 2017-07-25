package org.tdar.core.bean.collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.Hideable;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
public abstract class VisibleCollection extends ResourceCollection implements OaiDcProvider, HasName, Slugable, Addressable, Validatable, Indexable, Viewable, Hideable {


    private static final long serialVersionUID = -8963749030250029536L;

    private transient boolean viewable;

    @JsonView(JsonLookupFilter.class)
    @Length(max = FieldLength.FIELD_LENGTH_500)
    @NotNull
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description_formatted")
    private String formattedDescription;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;


    @XmlAttribute
    @Override
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean visible) {
        this.hidden = visible;
    }

    @Override
    @JsonView(JsonLookupFilter.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.trimToEmpty(name);
    }

    @Override
    @JsonView(JsonLookupFilter.class)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.trimToEmpty(description);
    }

    @Override
    public String toString() {
        return String.format("%s Resource collection %s: %s (creator: %s)", getType(), getId(), getName(), getOwner());
    }

    @Override
    public boolean isValidForController() {
        return StringUtils.isNotBlank(getName());
    }

    

    @Override
    public boolean isValid() {
        logger.trace("type: {} owner: {} name: {} sort: {}", getType(), getOwner(), getName());
        if (isValidForController()) {
            return ((getOwner() != null) && (getOwner().getId() != null) && (getOwner().getId() > -1));
        }
        return false;
    }    

    @Override
    public String getTitle() {
        return getName();
    }

    public String getFormattedDescription() {
        return formattedDescription;
    }

    public void setFormattedDescription(String adminDescription) {
        this.formattedDescription = adminDescription;
    }

    @JsonView(JsonLookupFilter.class)
    public String getDetailUrl() {
        return String.format("/%s/%s/%s", getUrlNamespace(), getId(), getSlug());
    }

    @Override
    public String getSlug() {
        return UrlUtils.slugify(getName());
    }

    @XmlTransient
    @Override
    public boolean isViewable() {
        return viewable;
    }

    @Override
    public void setViewable(boolean viewable) {
        this.viewable = viewable;
    }

    public boolean isSupportsThumbnails() {
        return false;
    }

    @XmlTransient
    public boolean isVisibleAndActive() {
        if (hidden) {
            return false;
        }
        if (getStatus() != Status.ACTIVE) {
            return false;
        }
        return true;
    }
}
