package org.tdar.core.bean.collection;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.tdar.core.bean.resource.Resource;

@Entity
public abstract class RightsBasedResourceCollection extends ResourceCollection {

    private static final long serialVersionUID = -7452202939323561883L;


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


}
