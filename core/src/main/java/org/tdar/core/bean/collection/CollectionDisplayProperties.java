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

    @Column(name = "custom_header_enabled", nullable = false)
    private Boolean customHeaderEnabled = false;

    @Column(name = "custom_doc_logo_enabled", nullable = false)
    private Boolean customDocumentLogoEnabled = false;

    @Column(name = "featured_resources_enabled", nullable = false)
    private Boolean featuredResourcesEnabled = false;

    @Column(name = "search_enabled", nullable = false)
    private Boolean searchEnabled = false;

    @Column(name = "sub_collections_enabled", nullable = false)
    private Boolean subCollectionsEnabled = false;

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

    // @OneToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH, CascadeType.REFRESH })
    // @JoinColumn(nullable = false)
    // // Hibernate will not cascade saveOrUpdate() if object is transient and relation is also transient. (see
    // // http://www.mkyong.com/hibernate/cascade-jpa-hibernate-annotation-common-mistake/)
    // // This is probably not a big deal, as it's unlikely we will be saving a new institution and a new WhiteLabelCollection in the same session in real-world
    // // conditions.
    // @Cascade({ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
    @Transient
    private Institution institution;

//    public boolean isCustomHeaderEnabled() {
//        if (getCustomHeaderEnabled() == null) {
//            return false;
//        }
//        return getCustomHeaderEnabled();
//    }
//
//    public void setCustomHeaderEnabled(boolean customHeaderEnabled) {
//        this.customHeaderEnabled = customHeaderEnabled;
//    }
//
//    public boolean isFeaturedResourcesEnabled() {
//        if (getFeaturedResourcesEnabled() == null) {
//            return false;
//        }
//        return getFeaturedResourcesEnabled();
//    }
//
//    public void setFeaturedResourcesEnabled(boolean featuredResourcesEnabled) {
//        this.featuredResourcesEnabled = featuredResourcesEnabled;
//    }
//
//    public void setSearchEnabled(boolean searchEnabled) {
//        this.searchEnabled = searchEnabled;
//    }
//
//    public boolean isSubCollectionsEnabled() {
//        if (getSubCollectionsEnabled() == null) {
//            return false;
//        }
//        return getSubCollectionsEnabled();
//    }
//
//    public void setSubCollectionsEnabled(boolean subCollectionsEnabled) {
//        this.subCollectionsEnabled = subCollectionsEnabled;
//    }
//
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
//
//    public boolean isCustomDocumentLogoEnabled() {
//        if (getCustomDocumentLogoEnabled() == null) {
//            return false;
//        }
//        return getCustomDocumentLogoEnabled();
//    }
//
//    public void setCustomDocumentLogoEnabled(boolean customDocumentLogoEnabled) {
//        this.customDocumentLogoEnabled = customDocumentLogoEnabled;
//    }
//
//    public boolean isSearchEnabled() {
//        if (getSearchEnabled() == null) {
//            return false;
//        }
//        return getSearchEnabled();
//    }
//
//    public boolean isWhitelabel() {
//        if (whitelabel == null) {
//            return false;
//        }
//        return whitelabel;
//    }
//
//    public void setWhitelabel(boolean whitelabel) {
//        this.whitelabel = whitelabel;
//    }

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

}
