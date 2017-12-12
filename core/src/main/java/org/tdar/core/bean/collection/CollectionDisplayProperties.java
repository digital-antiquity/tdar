package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.opengis.annotation.XmlElement;
import org.tdar.core.bean.HasImage;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.VersionType;

@XmlElement("collectionDisplayProperties")
@Embeddable
public class CollectionDisplayProperties implements HasImage {
    private static final long serialVersionUID = -3972164997710223460L;

    private transient Integer maxHeight;
    private transient Integer maxWidth;
    private transient VersionType maxSize;
    private transient Long id;

    @Column(name = "whitelabel", nullable = false, columnDefinition = "boolean default false")
    private Boolean whitelabel = false;

    @Column(name = "custom_header_enabled", nullable = false, columnDefinition = "boolean default false")
    private Boolean customHeaderEnabled = false;

    @Column(name = "custom_doc_logo_enabled", nullable = false, columnDefinition = "boolean default false")
    private Boolean customDocumentLogoEnabled = false;

    @Column(name = "featured_resources_enabled", nullable = false, columnDefinition = "boolean default false")
    private Boolean featuredResourcesEnabled = false;

    @Column(name = "search_enabled", nullable = false, columnDefinition = "boolean default false")
    private Boolean searchEnabled = false;

    @Column(name = "sub_collections_enabled", nullable = false, columnDefinition = "boolean default false")
    private Boolean subCollectionsEnabled = false;

    @Column(name = "hide_collection_sidebar", nullable = false, columnDefinition = "boolean default false")
    private Boolean hideCollectionSidebar = false;

    public CollectionDisplayProperties() {
        this(false,false,false,false,false,false, false);
    }
    
    public CollectionDisplayProperties(boolean whitelable, boolean customHeaderEnabled, boolean customLogoEnabled, boolean featuredResourceEnabled, boolean searchEnabled, boolean subCollectionEnabled, boolean hideCollectionSidebar) {
        this.whitelabel = whitelable;
        this.customDocumentLogoEnabled= customLogoEnabled;
        this.customHeaderEnabled = customHeaderEnabled;
        this.featuredResourcesEnabled =featuredResourceEnabled;
        this.searchEnabled = searchEnabled;
        this.subCollectionsEnabled = subCollectionEnabled;
        this.hideCollectionSidebar = hideCollectionSidebar;
    }
    
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String css;

    @Column
    private String subtitle;

    @ManyToMany
    @JoinTable(name = "whitelabel_featured_resource", joinColumns = { @JoinColumn(nullable = false, name = "collection_id") },
            inverseJoinColumns = { @JoinColumn(
                    nullable = false, name = "resource_id") })
    private List<Resource> featuredResources = new ArrayList<>();


    @Transient
    private Institution institution;

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public List<Resource> getFeaturedResources() {
        return featuredResources;
    }

    public void setFeaturedResources(List<Resource> featuredResources) {
        this.featuredResources = featuredResources;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Integer getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(Integer maxHeight) {
        this.maxHeight = maxHeight;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    public VersionType getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(VersionType maxSize) {
        this.maxSize = maxSize;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCustomHeaderEnabled() {
        if (customHeaderEnabled == null) {
            setCustomHeaderEnabled(Boolean.FALSE);
        }
        return customHeaderEnabled;
    }

    public void setCustomHeaderEnabled(Boolean customHeaderEnabled) {
        this.customHeaderEnabled = customHeaderEnabled;
    }

    public Boolean getCustomDocumentLogoEnabled() {
        if (customDocumentLogoEnabled == null) {
            setCustomDocumentLogoEnabled(Boolean.FALSE);
        }
        return customDocumentLogoEnabled;
    }

    public void setCustomDocumentLogoEnabled(Boolean customDocumentLogoEnabled) {
        this.customDocumentLogoEnabled = customDocumentLogoEnabled;
    }

    public Boolean getFeaturedResourcesEnabled() {
        if (featuredResourcesEnabled == null) {
            setFeaturedResourcesEnabled(Boolean.FALSE);
        }
        return featuredResourcesEnabled;
    }

    public void setFeaturedResourcesEnabled(Boolean featuredResourcesEnabled) {
        this.featuredResourcesEnabled = featuredResourcesEnabled;
    }

    public Boolean getSearchEnabled() {
        if (searchEnabled == null) {
            setSearchEnabled(Boolean.FALSE);
        }
        return searchEnabled;
    }

    public void setSearchEnabled(Boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    public Boolean getSubCollectionsEnabled() {
        if (subCollectionsEnabled == null) {
            setSubCollectionsEnabled(Boolean.FALSE);
        }
        return subCollectionsEnabled;
    }

    public void setSubCollectionsEnabled(Boolean subCollectionsEnabled) {
        this.subCollectionsEnabled = subCollectionsEnabled;
    }

    public Boolean getWhitelabel() {
        if (whitelabel == null) {
            setWhitelabel(Boolean.FALSE);
        }
        return whitelabel;
    }

    public void setWhitelabel(Boolean whitelabel) {
        this.whitelabel = whitelabel;
    }

    public Boolean getHideCollectionSidebar() {
        return hideCollectionSidebar;
    }

    public void setHideCollectionSidebar(Boolean hideCollectionSidebar) {
        this.hideCollectionSidebar = hideCollectionSidebar;
    }

}
