package org.tdar.struts.action.resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource")
public class ResourceComparisonAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 1996434590439580868L;
    private List<Long> ids;
    private Long collectionId;
    private Set<Resource> resources = new HashSet<>();
    
    @Autowired
    private GenericService genericService;
    @Autowired
    private AuthorizationService authorizationService;
    
    private Set<Long> cultures = new HashSet<>();
    private Set<Long> investigationTypes = new HashSet<>();
    private Set<Long> temporal = new HashSet<>();
    private Set<Long> material = new HashSet<>();
    private Set<Long> other = new HashSet<>();
    private Set<Long> geographic = new HashSet<>();
    private Set<Long> siteNames = new HashSet<>();
    private Set<Long> siteTypes = new HashSet<>();
    private Set<Long> creators = new HashSet<>(); 
    private Set<Long> individualRoles = new HashSet<>(); 
    private Set<Long> latitudeLongitude = new HashSet<>(); 
    private Set<Long> notes = new HashSet<>();
    private Set<Long> coverage = new HashSet<>(); 
    private Set<Long> collections = new HashSet<>(); 
    private Set<Long> annotations = new HashSet<>(); 


    @Override
    public void prepare() throws Exception {
        resources.addAll(genericService.findAll(Resource.class, ids));
        if (PersistableUtils.isNotNullOrTransient(getCollectionId())) {
            ResourceCollection rc = genericService.find(ResourceCollection.class, getCollectionId());
            if (rc instanceof ListCollection) {
            resources.addAll(((ListCollection) rc).getUnmanagedResources());
            } else {
                resources.addAll(((SharedCollection)rc).getResources());
            }
        }
        resources.forEach(resource -> {
                if (!authorizationService.canEditResource(getAuthenticatedUser(), resource, GeneralPermissions.MODIFY_RECORD)) {
                    addActionError(getText("abstractPersistableController.unable_to_view_edit"));
                }
            }
        );
        setupSharedKeywords();
    }

    public void setupSharedKeywords() {
        boolean first = true;
        for (Resource r : resources) {
            if (first) {
                cultures.addAll(PersistableUtils.extractIds(r.getActiveCultureKeywords()));
                investigationTypes.addAll(PersistableUtils.extractIds(r.getActiveInvestigationTypes()));
                temporal.addAll(PersistableUtils.extractIds(r.getActiveTemporalKeywords()));
                material.addAll(PersistableUtils.extractIds(r.getActiveMaterialKeywords()));
                other.addAll(PersistableUtils.extractIds(r.getActiveOtherKeywords()));
                geographic.addAll(PersistableUtils.extractIds(r.getActiveGeographicKeywords()));
                siteNames.addAll(PersistableUtils.extractIds(r.getActiveSiteNameKeywords()));
                siteTypes.addAll(PersistableUtils.extractIds(r.getActiveSiteTypeKeywords()));
                creators.addAll(PersistableUtils.extractIds(r.getPrimaryCreators()));
                individualRoles.addAll(PersistableUtils.extractIds(r.getActiveIndividualAndInstitutionalCredit()));
                latitudeLongitude.addAll(PersistableUtils.extractIds(r.getActiveLatitudeLongitudeBoxes()));
                notes.addAll(PersistableUtils.extractIds(r.getActiveResourceNotes()));
                coverage.addAll(PersistableUtils.extractIds(r.getActiveCoverageDates()));
                collections.addAll(PersistableUtils.extractIds(r.getSharedResourceCollections()));
                annotations.addAll(PersistableUtils.extractIds(r.getActiveResourceAnnotations()));
                first = false;
            } else {
                cultures = SetUtils.intersection(cultures, toSet(r.getActiveCultureKeywords()));
                investigationTypes = SetUtils.intersection(investigationTypes, toSet(r.getActiveInvestigationTypes()));
                temporal = SetUtils.intersection(temporal, toSet(r.getActiveTemporalKeywords()));
                material = SetUtils.intersection(material, toSet(r.getActiveMaterialKeywords()));
                other = SetUtils.intersection(other, toSet(r.getActiveOtherKeywords()));
                geographic = SetUtils.intersection(geographic, toSet(r.getActiveGeographicKeywords()));
                siteNames = SetUtils.intersection(siteNames, toSet(r.getActiveSiteNameKeywords()));
                siteTypes = SetUtils.intersection(siteTypes, toSet(r.getActiveSiteTypeKeywords()));
                creators = SetUtils.intersection( creators, new HashSet<>(toSet(r.getPrimaryCreators())));
                individualRoles = SetUtils.intersection(individualRoles , toSet(r.getActiveIndividualAndInstitutionalCredit()));
                latitudeLongitude = SetUtils.intersection(latitudeLongitude , toSet(r.getActiveLatitudeLongitudeBoxes()));
                notes = SetUtils.intersection( notes, toSet(r.getActiveResourceNotes()));
                coverage = SetUtils.intersection( coverage, toSet(r.getActiveCoverageDates()));
                collections = SetUtils.intersection( collections, toSet(r.getSharedResourceCollections()));
                annotations = SetUtils.intersection( annotations, toSet(r.getActiveResourceAnnotations()));
            }
        }
    }
    
    private <R extends Persistable> Set<Long> toSet(Collection<R> collection) {
        return new HashSet<>(PersistableUtils.extractIds(collection));
    }

    @Action(value = "compare", results = {
            @Result(name = SUCCESS, location = "compare.ftl")
    })
    @Override
    public String execute() {
        return SUCCESS;
    }
    
    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public Set<Long> getCultures() {
        return cultures;
    }

    public void setCultures(HashSet<Long> cultures) {
        this.cultures = cultures;
    }

    public Set<Long> getInvestigationTypes() {
        return investigationTypes;
    }

    public void setInvestigationTypes(HashSet<Long> investigationTypes) {
        this.investigationTypes = investigationTypes;
    }

    public Set<Long> getTemporal() {
        return temporal;
    }

    public void setTemporal(HashSet<Long> temporal) {
        this.temporal = temporal;
    }

    public Set<Long> getMaterial() {
        return material;
    }

    public void setMaterial(HashSet<Long> material) {
        this.material = material;
    }

    public Set<Long> getOther() {
        return other;
    }

    public void setOther(HashSet<Long> other) {
        this.other = other;
    }

    public Set<Long> getGeographic() {
        return geographic;
    }

    public void setGeographic(HashSet<Long> geographic) {
        this.geographic = geographic;
    }

    public Set<Long> getSiteNames() {
        return siteNames;
    }

    public void setSiteNames(HashSet<Long> siteNames) {
        this.siteNames = siteNames;
    }

    public Set<Long> getSiteTypes() {
        return siteTypes;
    }

    public void setSiteTypes(HashSet<Long> siteTypes) {
        this.siteTypes = siteTypes;
    }

    public Set<Long> getCreators() {
        return creators;
    }

    public void setCreators(Set<Long> creators) {
        this.creators = creators;
    }

    public Set<Long> getIndividualRoles() {
        return individualRoles;
    }

    public void setIndividualRoles(Set<Long> individualRoles) {
        this.individualRoles = individualRoles;
    }

    public Set<Long> getLatitudeLongitude() {
        return latitudeLongitude;
    }

    public void setLatitudeLongitude(Set<Long> latitudeLongitude) {
        this.latitudeLongitude = latitudeLongitude;
    }

    public Set<Long> getNotes() {
        return notes;
    }

    public void setNotes(Set<Long> notes) {
        this.notes = notes;
    }

    public Set<Long> getCoverage() {
        return coverage;
    }

    public void setCoverage(Set<Long> coverage) {
        this.coverage = coverage;
    }

    public Set<Long> getCollections() {
        return collections;
    }

    public void setCollections(Set<Long> collections) {
        this.collections = collections;
    }

    public Set<Long> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Set<Long> annotations) {
        this.annotations = annotations;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

}
