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
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.xml.bind.annotation.XmlType;

import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Sortable;

@Entity
@XmlType(name = "customCollBase")
@SecondaryTable(name = "whitelabel_collection", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public abstract class CustomizableCollection<C extends HierarchicalCollection<C>> extends HierarchicalCollection<C> implements Sortable {

    private static final long serialVersionUID = 3900834256299683436L;

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
            @AttributeOverride(name = "custom_header_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
            @AttributeOverride(name = "custom_doc_logo_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
            @AttributeOverride(name = "featured_resources_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
            @AttributeOverride(name = "search_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
            @AttributeOverride(name = "sub_collections_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
            @AttributeOverride(name = "subtitle", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "hide_collection_sidebar", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "css", column = @Column(table = "whitelabel_collection"))
    })
    @Access(AccessType.FIELD)
    private CollectionDisplayProperties properties;

    public CollectionDisplayProperties getProperties() {
        return properties;
    }

    public void setProperties(CollectionDisplayProperties properties) {
        this.properties = properties;
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

    @Override
    public boolean isValid() {
        if (super.isValid() == false) {
            return false;
        }
        if ((getType() == CollectionType.SHARED) && (sortBy == null)) {
            return false;
        }
        return true;
    }

}
