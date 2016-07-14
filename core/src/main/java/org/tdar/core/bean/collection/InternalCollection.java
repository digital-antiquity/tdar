package org.tdar.core.bean.collection;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.tdar.core.bean.resource.Resource;

@DiscriminatorValue(value = "INTERNAL")
@XmlRootElement(name = "internalCollection")
@Entity
public class InternalCollection extends ResourceCollection implements RightsBasedResourceCollection {

    private static final long serialVersionUID = 2238608996291414672L;



    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "resourceCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection.resources")
    private Set<Resource> resources = new LinkedHashSet<Resource>();



    //if you serialize this (even if just a list IDs, hibernate will request all necessary fields and do a traversion of the full resource graph (this could crash tDAR if > 100,000)
    @XmlTransient
    public Set<Resource> getResources() {
        return resources;
    }


    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }


    public InternalCollection() {
        this.setType(CollectionType.INTERNAL);
    }

    @Override
    public boolean isValidForController() {
        return true;
    }
    
}
