package org.tdar.core.bean.collection;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;

@DiscriminatorValue(value = "SHARED")
@Entity
@XmlRootElement(name = "resourceCollection")
public class SharedCollection extends ResourceCollection
        implements Comparable<SharedCollection>, HasName, Sortable, HierarchicalCollection {
    private static final long serialVersionUID = 7900346272773477950L;


    public SharedCollection(String title, String description, boolean hidden, SortOption sortOption, DisplayOrientation displayOrientation, TdarUser creator) {
        setName(title);
        setDescription(description);
        setHidden(hidden);
        setSortBy(sortOption);
        setOrientation(displayOrientation);
        setOwner(creator);
        this.setType(CollectionType.SHARED);
    }
    
    public SharedCollection(Long id, String title, String description, SortOption sortOption, boolean hidden) {
        setId(id);
        setName(title);
        setDescription(description);
        setHidden(hidden);
        setSortBy(sortOption);
        this.setType(CollectionType.SHARED);

    }

    public SharedCollection(String title, String description, TdarUser submitter) {
        setName(title);
        setDescription(description);
        setHidden(false);
        this.setOwner(submitter);
        setSortBy(SortOption.TITLE);
        setOrientation(DisplayOrientation.LIST);
        this.setType(CollectionType.SHARED);
    }

    public SharedCollection(Document document, TdarUser tdarUser) {
        markUpdated(tdarUser);
        getResources().add(document);
        setHidden(false);
        setSortBy(SortOption.TITLE);
        setOrientation(DisplayOrientation.LIST);
        this.setType(CollectionType.SHARED);
    }

    public SharedCollection() {
        this.setType(CollectionType.SHARED);
        setSortBy(SortOption.TITLE);
        setOrientation(DisplayOrientation.LIST);
    }

}
