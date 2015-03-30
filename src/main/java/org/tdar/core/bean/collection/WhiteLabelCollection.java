package org.tdar.core.bean.collection;

import javax.persistence.*;

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

    @Override
    public boolean isWhiteLabelCollection() {
        return true;
    }
}
