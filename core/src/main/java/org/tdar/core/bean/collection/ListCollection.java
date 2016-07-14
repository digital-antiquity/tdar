package org.tdar.core.bean.collection;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

@DiscriminatorValue(value = "LIST")
@Entity
@SecondaryTable(name="whitelabel_collection", pkJoinColumns=@PrimaryKeyJoinColumn(name="id"))
@XmlRootElement(name = "listCollection")
public class ListCollection extends ResourceCollection implements HierarchicalCollection<ListCollection>, Comparable<ListCollection>, HasDisplayProperties {

    private static final long serialVersionUID = 1225586588061994193L;

    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "unmanagedResourceCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection.unmanagedResources")
    private Set<Resource> unmanagedResources = new LinkedHashSet<Resource>();

    public ListCollection() {
        properties = new CollectionDisplayProperties();
        setType(CollectionType.LIST);
    }
    @Embedded
    @AttributeOverrides({
        @AttributeOverride( name="whitelabel", column=@Column(table="whitelabel_collection")),
        @AttributeOverride( name="custom_header_enabled", column=@Column(table="whitelabel_collection")),
        @AttributeOverride( name="custom_doc_logo_enabled", column=@Column(table="whitelabel_collection")),
        @AttributeOverride( name="featured_resources_enabled", column=@Column(table="whitelabel_collection")),
        @AttributeOverride( name="search_enabled", column=@Column(table="whitelabel_collection")),
        @AttributeOverride( name="sub_collections_enabled", column=@Column(table="whitelabel_collection")),
        @AttributeOverride( name="subtitle", column=@Column(table="whitelabel_collection")),
        @AttributeOverride( name="css", column=@Column(table="whitelabel_collection"))
    })
    private CollectionDisplayProperties properties;

    public CollectionDisplayProperties getProperties() {
        return properties;
    }

    public void setProperties(CollectionDisplayProperties properties) {
        this.properties = properties;
    }

    public Set<Resource> getUnmanagedResources() {
        return unmanagedResources;
    }

    public void setUnmanagedResources(Set<Resource> publicResources) {
        this.unmanagedResources = publicResources;
    }


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
    
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ListCollection parent;

    @XmlAttribute(name = "parentIdRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @Override
    public ListCollection getParent() {
        return parent;
    }


    @ElementCollection()
    @CollectionTable(name = "collection_parents", joinColumns = @JoinColumn(name = "collection_id") )
    @Column(name = "parent_id")
    private Set<Long> parentIds = new HashSet<>();


    /**
     * Get ordered list of parents (ids) of this resources ... great grandfather, grandfather, father.
     * 
     * Note: in earlier implementations this contained the currentId as well, I've removed this, but am unsure
     * whether it should be there
     */
    @Transient
    @ElementCollection
    public Set<Long> getParentIds() {
        return parentIds;
    }

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;


    @XmlAttribute
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean visible) {
        this.hidden = visible;
    }


    public void setParentIds(Set<Long> parentIds) {
        this.parentIds = parentIds;
    }


    @Override
    public void setParent(ListCollection parent) {
        this.parent = parent;
    }

    private transient Set<ListCollection> transientChildren = new LinkedHashSet<>();

    @XmlTransient
    @Transient
    @Override
    public Set<ListCollection> getTransientChildren() {
        return transientChildren;
    }

    @Override
    public void setTransientChildren(Set<ListCollection> transientChildren) {
        this.transientChildren = transientChildren;
    }


    /*
     * Get all of the resource collections via a tree (actually list of lists)
     */
    @Transient
    @XmlTransient
    // infinite loop because parentTree[0]==self
    @Override
    public List<ListCollection> getHierarchicalResourceCollections() {
        return getHierarchicalResourceCollections(ListCollection.class, this);
    }

    /*
     * Default to sorting by name, but grouping by parentId, used for sorting int he tree
     */
    @Override
    public int compareTo(ListCollection o) {
        return compareTo(this, o);
    }

    @Transient
    @XmlTransient
    @Override
    public List<ListCollection> getVisibleParents() {
        return getVisibleParents(ListCollection.class);
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

}
