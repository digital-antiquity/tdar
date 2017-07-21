package org.tdar.core.bean.resource;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;

/**
 * This ResourceProxy class is designed to handle one of the major performance issues with Hibernate, that being the insane lookup queries that Hibernate
 * performs to grab all of the relationships for Resource. Basically, from what we can tell, the "@Inheritance(strategy = InheritanceType.JOINED)" for
 * Resource is causing lots of joins deeper into the resource hierarchy than what we need in Lucene Searches. Thus we've looked at a number of methods for
 * handling this including: Criteria Queries with Projection, HQL with Projection, and ultimately these proxy objects.
 * 
 * With the context of these views, we've seen significant performance boosts using these hibernate proxy objects to back Hibernate Search's Lucene searches. We
 * also looked at removing the bi-directional relationships between InformationResource <=> InformationResourceFile and InformationResourceFile <=>
 * InformationResourceFileVersion.
 * 
 * To document a bit of the difference a earch For 500 resources via normal web search (http://localhost:8080/search/results?recordsPerPage=500 ):
 * - HibernateSearch Native: 19507 ms
 * - ResourceProxy with bi-directional relationships removed: 18607 ms
 * - Using the ResourceProxy, InformatonResourceFileProxy, and InformationResourceFileVersionProxy: 5224 ms
 * 
 * @author abrin
 * 
 */
@Entity
@Immutable
@Table(name = "resource")
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource")
public class ResourceProxy implements Serializable {

    private static final long serialVersionUID = -2574871889110727564L;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id")
    @Immutable
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.latitudeLongitudeBoxes")
    private Set<LatitudeLongitudeBox> latitudeLongitudeBoxes = new LinkedHashSet<>();

    @Column(name = "date_created")
//    @DateBridge(resolution = Resolution.DAY)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String url;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "submitter_id")
    private TdarUser submitter;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "uploader_id")
    private TdarUser uploader;

    @ManyToOne()
    @JoinColumn(name = "updater_id")
    @NotNull
    private TdarUser updatedBy;

    @Column(name = "date_updated")
//    @DateBridge(resolution = Resolution.MILLISECOND)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = FieldLength.FIELD_LENGTH_50)
    private Status status = Status.ACTIVE;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Column(name = "title")
    private String title;

    @Column(name = "resource_type")
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Id
    @Column(name = "id")
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "collection_resource", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false, name = "collection_id") })
    @XmlTransient
    @Where(clause="collection_type='SHARED'")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.resourceCollections")
    private Set<SharedCollection> sharedCollections = new LinkedHashSet<>();

    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinTable(name = "collection_resource", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false, name = "collection_id") })
    @XmlTransient
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.resourceCollections")
    @Where(clause="collection_type!='LIST'")
    private Set<ResourceCollection> resourceCollections = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id")
    @Immutable
    @Fetch(FetchMode.JOIN)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.authorizedUsers")
    private Set<AuthorizedUser> authorizedUsers = new LinkedHashSet<AuthorizedUser>();

    
    @OneToMany(fetch = FetchType.EAGER, targetEntity = ResourceCreator.class)
    @JoinColumn(name = "resource_id")
    @Immutable
    @Fetch(FetchMode.JOIN)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.resourceCreators")
    private Set<ResourceCreator> resourceCreators = new LinkedHashSet<>();

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s %s", id, title, getLatitudeLongitudeBoxes(), getResourceCreators(), submitter);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Set<ResourceCreator> getResourceCreators() {
        return resourceCreators;
    }

    public void setResourceCreators(Set<ResourceCreator> resourceCreators) {
        this.resourceCreators = resourceCreators;
    }

    public Set<LatitudeLongitudeBox> getLatitudeLongitudeBoxes() {
        return latitudeLongitudeBoxes;
    }

    public void setLatitudeLongitudeBoxes(Set<LatitudeLongitudeBox> latitudeLongitudeBoxes) {
        this.latitudeLongitudeBoxes = latitudeLongitudeBoxes;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TdarUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
        this.submitter = submitter;
    }

    public TdarUser getUploader() {
        return uploader;
    }

    public void setUploader(TdarUser uploader) {
        this.uploader = uploader;
    }

    public TdarUser getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(TdarUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @SuppressWarnings("unchecked")
    public <T extends Resource> T generateResource() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        logger.trace("begin bean generation: {}", this.getId());
        T res = (T) getResourceType().getResourceClass().newInstance();
        res.getLatitudeLongitudeBoxes().addAll(this.getLatitudeLongitudeBoxes());
        res.getResourceCreators().addAll(this.getResourceCreators());
        res.setSubmitter(this.getSubmitter());
        res.setUpdatedBy(this.getUpdatedBy());
        res.setUploader(this.getUploader());
        res.setDateCreated(this.getDateCreated());
        res.setStatus(this.getStatus());
        res.setResourceType(this.getResourceType());
        res.setTitle(getTitle());
        res.setDescription(getDescription());
        res.setUrl(this.getUrl());
        res.setDateUpdated(this.getDateUpdated());
        res.setId(this.getId());
        logger.trace("recursing down");
        res.getSharedCollections().addAll(getSharedCollections());
        res.getAuthorizedUsers().addAll(getAuthorizedUsers());
        logger.trace("done generation");
        return res;
    }

    public Set<SharedCollection> getSharedCollections() {
        return sharedCollections;
    }

    public void setSharedCollections(Set<SharedCollection> resourceCollections) {
        this.sharedCollections = resourceCollections;
    }

    public Set<ResourceCollection> getResourceCollections() {
        return resourceCollections;
    }

    public void setResourceCollections(Set<ResourceCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    public Set<AuthorizedUser> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public void setAuthorizedUsers(Set<AuthorizedUser> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

}
