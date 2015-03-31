package org.tdar.core.bean.collection;

import org.hibernate.annotations.*;
import org.tdar.core.bean.entity.Institution;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Created by jimdevos on 3/17/15.
 */
@Entity
@Table(name="whitelabel_collection")
@Inheritance(strategy = InheritanceType.JOINED)
public class WhiteLabelCollection extends ResourceCollection{

    @Column(name = "custom_header_enabled")
    private boolean customHeaderEnabled;

    @Column(name = "featured_resources_enabled")
    private boolean featuredResourcesEnabled;

    @Column(name = "search_enabled")
    private boolean searchEnabled;

    @Column(name = "sub_collections_enabled")
    private boolean subCollectionsEnabled;

    @Lob
    private String css;

    //Cascaded saves of transient references don't work for this declaration (using javasx annotations)


    @OneToOne(optional = true, cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(nullable = false)

    //Hibernate will not cascade saveOrUpdate()  if object is transient and relation is also transient. (see http://www.mkyong.com/hibernate/cascade-jpa-hibernate-annotation-common-mistake/)
    //This is probably not a big deal, as it's unlikely we will be saving a new institution and a new WhiteLabelCollection in the same session in real-world conditions.
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    Institution institution;

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
}
