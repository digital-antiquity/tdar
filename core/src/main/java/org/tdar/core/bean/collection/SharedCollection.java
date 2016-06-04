package org.tdar.core.bean.collection;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;

@DiscriminatorValue(value = "SHARED")
@Entity
@XmlRootElement(name = "sharedCollection")
public class SharedCollection extends RightsBasedResourceCollection {

    public SharedCollection(String title, String description, SortOption sortBy, boolean visible, TdarUser creator) {
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setHidden(visible);
        setOwner(creator);
    }

    public SharedCollection(Long id, String title, String description, SortOption sortBy, boolean visible) {
        setId(id);
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setHidden(visible);
    }

    public SharedCollection(Document document, TdarUser tdarUser) {
        markUpdated(tdarUser);
        getResources().add(document);
    }

    public SharedCollection() {
    }

    private static final long serialVersionUID = 7900346272773477950L;

}
