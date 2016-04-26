package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.resource.Resource;

/**
 * Created by jimdevos on 3/17/15.
 */
@Entity
@Table(name = "whitelabel_collection")
@Inheritance(strategy = InheritanceType.JOINED)
//@Indexed
@XmlRootElement(name = "whiteLabelCollection")
public class WhiteLabelCollection extends ResourceCollection {

    private static final long serialVersionUID = -7436222082273438465L;

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

    public WhiteLabelCollection() {
        super(CollectionType.SHARED);
    }

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

    @Override
    public boolean isSearchEnabled() {
        return searchEnabled;
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

    @Override
    public boolean isWhiteLabelCollection() {
        return true;
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
}
