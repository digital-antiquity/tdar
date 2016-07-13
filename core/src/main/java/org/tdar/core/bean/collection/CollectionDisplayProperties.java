package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.opengis.annotation.XmlElement;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.HasImage;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.VersionType;

@Table(name = "collection_display_properties")
@Entity
@XmlElement("collectionDisplayProperties")
public class CollectionDisplayProperties extends AbstractPersistable implements HasImage {
    private static final long serialVersionUID = -3972164997710223460L;
    
    private transient Integer maxHeight;
    private transient Integer maxWidth;
    private transient VersionType maxSize;

    @Column(name = "whitelabel")
    private boolean whitelabel = false;
    
    @Column(name = "custom_header_enabled")
    private boolean customHeaderEnabled;

    @Column(name = "custom_doc_logo_enabled")
    private boolean customDocumentLogoEnabled;

    @Column(name = "featured_resources_enabled")
    private boolean featuredResourcesEnabled;

    @Column(name = "search_enabled")
    private boolean searchEnabled;

    @Column(name = "sub_collections_enabled")
    private boolean subCollectionsEnabled;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String css;

    @Column
    private String subtitle;

    @ManyToMany
    @JoinTable(name = "whitelabel_featured_resource", joinColumns = { @JoinColumn(nullable = false, name = "properties_id") },
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

    public boolean isCustomHeaderEnabled() {
        return customHeaderEnabled;
    }

    public void setCustomHeaderEnabled(boolean customHeaderEnabled) {
        this.customHeaderEnabled = customHeaderEnabled;
    }

    public boolean isFeaturedResourcesEnabled() {
        return featuredResourcesEnabled;
    }

    public void setFeaturedResourcesEnabled(boolean featuredResourcesEnabled) {
        this.featuredResourcesEnabled = featuredResourcesEnabled;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    public boolean isSubCollectionsEnabled() {
        return subCollectionsEnabled;
    }

    public void setSubCollectionsEnabled(boolean subCollectionsEnabled) {
        this.subCollectionsEnabled = subCollectionsEnabled;
    }

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

    public boolean isCustomDocumentLogoEnabled() {
        return customDocumentLogoEnabled;
    }

    public void setCustomDocumentLogoEnabled(boolean customDocumentLogoEnabled) {
        this.customDocumentLogoEnabled = customDocumentLogoEnabled;
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public boolean isWhitelabel() {
        return whitelabel;
    }

    public void setWhitelabel(boolean whitelabel) {
        this.whitelabel = whitelabel;
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

}
