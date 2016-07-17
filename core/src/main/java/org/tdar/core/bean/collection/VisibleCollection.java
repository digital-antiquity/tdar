package org.tdar.core.bean.collection;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
@SecondaryTable(name = "whitelabel_collection", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public abstract class VisibleCollection extends ResourceCollection implements OaiDcProvider, Sortable, HasName, Slugable, Addressable, Validatable, Indexable, Viewable {


    private static final long serialVersionUID = -8963749030250029536L;
    private transient boolean viewable;

    @Column
    @JsonView(JsonLookupFilter.class)
    @Length(max = FieldLength.FIELD_LENGTH_500)
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description_formatted")
    private String formattedDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", length = FieldLength.FIELD_LENGTH_25)
    private SortOption sortBy = DEFAULT_SORT_OPTION;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_sort_order", length = FieldLength.FIELD_LENGTH_25)
    private SortOption secondarySortBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "orientation", length = FieldLength.FIELD_LENGTH_50)
    private DisplayOrientation orientation = DisplayOrientation.LIST;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "whitelabel", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "custom_header_enabled", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "custom_doc_logo_enabled", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "featured_resources_enabled", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "search_enabled", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "sub_collections_enabled", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "subtitle", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "css", column = @Column(table = "whitelabel_collection"))
    })
    @Access(AccessType.FIELD)
    private CollectionDisplayProperties properties;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;


    @XmlAttribute
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean visible) {
        this.hidden = visible;
    }


    public CollectionDisplayProperties getProperties() {
        return properties;
    }

    public void setProperties(CollectionDisplayProperties properties) {
        this.properties = properties;
    }


    @Override
    @JsonView(JsonLookupFilter.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @JsonView(JsonLookupFilter.class)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param sortBy
     *            the sortBy to set
     */
    public void setSortBy(SortOption sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * @return the sortBy
     */
    @Override
    public SortOption getSortBy() {
        return sortBy;
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
        return getTitle().replaceAll(PersistableUtils.TITLE_SORT_REGEX, "");
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getUrlNamespace() {
        return "collection";
    }
    public DisplayOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DisplayOrientation orientation) {
        this.orientation = orientation;
    }



    public SortOption getSecondarySortBy() {
        return secondarySortBy;
    }

    public void setSecondarySortBy(SortOption secondarySortBy) {
        this.secondarySortBy = secondarySortBy;
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


}
