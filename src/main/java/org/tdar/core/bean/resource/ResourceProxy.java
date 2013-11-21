package org.tdar.core.bean.resource;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;

@Entity
@Immutable
@Subselect(value = "select rp.* , project_id, inheriting_spatial_information from resource rp left join information_resource ir on rp.id=ir.id")
public class ResourceProxy implements Serializable {

    private static final long serialVersionUID = -2574871889110727564L;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id")
    @Immutable
    private Set<LatitudeLongitudeBox> latitudeLongitudeBoxes = new LinkedHashSet<>();

    @ManyToOne(optional = true)
    @JoinColumn(name="project_id")
    private ResourceProxy projectProxy;

    @OneToMany(fetch = FetchType.LAZY, targetEntity=InformationResourceFileProxy.class)
    @JoinColumn(name = "information_resource_id")
    @OrderBy("sequenceNumber asc")
    @Immutable
    private List<InformationResourceFileProxy> informationResourceFileProxies = new ArrayList<>();


    @Column(name = "date_registered")
    @DateBridge(resolution = Resolution.DAY)
    private Date dateCreated;

    @Length(max = 255)
    private String url;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "submitter_id")
    private Person submitter;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "uploader_id")
    private Person uploader;

    @ManyToOne()
    @JoinColumn(name = "updater_id")
    @NotNull
    private Person updatedBy;

    @Column(name = "date_updated")
    @DateBridge(resolution = Resolution.MILLISECOND)
    private Date dateUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private Status status = Status.ACTIVE;
    
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @Column(name = "title")
    private String title;

    @Column(name = "resource_type")
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Id
    @Column(name = "id")
    private Long id;

    @OneToMany(fetch=FetchType.EAGER, targetEntity=ResourceCreator.class)
    @JoinColumn(name = "resource_id")
    @Immutable
    @Fetch(FetchMode.JOIN)
    private Set<ResourceCreator> resourceCreators = new LinkedHashSet<>();

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s", id, title, getLatitudeLongitudeBoxes(), getResourceCreators(), getProjectProxy(), getInformationResourceFileProxies(), submitter);
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

    public ResourceProxy getProjectProxy() {
        return projectProxy;
    }

    public void setProjectProxy(ResourceProxy project) {
        this.projectProxy = project;
    }

    public List<InformationResourceFileProxy> getInformationResourceFileProxies() {
        return informationResourceFileProxies;
    }

    public void setInformationResourceFileProxies(List<InformationResourceFileProxy> informationResourceFiles) {
        this.informationResourceFileProxies = informationResourceFiles;
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

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    public Person getUploader() {
        return uploader;
    }

    public void setUploader(Person uploader) {
        this.uploader = uploader;
    }

    public Person getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Person updatedBy) {
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
    public <T extends Resource> T generateResource() throws IllegalAccessException, InvocationTargetException, InstantiationException{
        T res = (T) getResourceType().getResourceClass().newInstance();
        java.util.Date defaultValue = null;
        Converter converter = new DateConverter(defaultValue);
        BeanUtilsBean beanUtilsBean = BeanUtilsBean.getInstance();
        beanUtilsBean.getConvertUtils().register(converter, java.util.Date.class);
        beanUtilsBean.copyProperties(res, this);
        if (res instanceof InformationResource) {
            InformationResource ir = (InformationResource)res;
            for (InformationResourceFileProxy prox : getInformationResourceFileProxies()) {
                ir.getInformationResourceFiles().add(prox.generateInformationResourceFile());
            }
            Project project = Project.NULL;
            if (getProjectProxy() != null) {
                project = getProjectProxy().generateResource();
            }
            ir.setProject(project);

        }
        return res;
    }

}
