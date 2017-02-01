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
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.Comparator;

@Entity
public abstract class VisibleCollection extends ResourceCollection implements OaiDcProvider, HasName, Slugable, Addressable, Validatable, Indexable, Viewable {


    private static final long serialVersionUID = -8963749030250029536L;
    public static final Comparator<? super VisibleCollection> TITLE_COMPARATOR = (Comparator.comparing(VisibleCollection::getTitleSort)
    );
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

    public String getTitleSort() {
        if (getTitle() == null) {
            return "";
        }
        return getTitle().replaceAll(PersistableUtils.TITLE_SORT_REGEX, "").toLowerCase();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getUrlNamespace() {
        return "collection";
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

    public String getAllFieldSearch() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTitle()).append(" ").append(getDescription()).append(" ");
        return sb.toString();
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

}
