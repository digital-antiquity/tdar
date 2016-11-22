package org.tdar.struts.action.resource;

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
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceNote;
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
    
    private Set<CultureKeyword> cultures = new HashSet<>();
    private Set<InvestigationType> investigationTypes = new HashSet<>();
    private Set<TemporalKeyword> temporal = new HashSet<>();
    private Set<MaterialKeyword> material = new HashSet<>();
    private Set<OtherKeyword> other = new HashSet<>();
    private Set<GeographicKeyword> geographic = new HashSet<>();
    private Set<SiteNameKeyword> siteNames = new HashSet<>();
    private Set<SiteTypeKeyword> siteTypes = new HashSet<>();
    private Set<ResourceCreator> creators = new HashSet<>(); 
    private Set<ResourceCreator> individualRoles = new HashSet<>(); 
    private Set<LatitudeLongitudeBox> latitudeLongitude = new HashSet<>(); 
    private Set<ResourceNote> notes = new HashSet<>();
    private Set<CoverageDate> coverage = new HashSet<>(); 
    private Set<ResourceCollection> collections = new HashSet<>(); 
    private Set<ResourceAnnotation> annotations = new HashSet<>(); 


    @Override
    public void prepare() throws Exception {
        resources.addAll( genericService.findAll(Resource.class, ids));
        if (PersistableUtils.isNotNullOrTransient(getCollectionId())) {
            ResourceCollection rc = genericService.find(ResourceCollection.class, getCollectionId());
            if (rc instanceof ListCollection) {
            resources.addAll(((ListCollection) rc).getUnmanagedResources());
            } else {
                resources.addAll(((RightsBasedResourceCollection)rc).getResources());
            }
        }
        resources.forEach(resource -> {
                if (!authorizationService.canEditResource(getAuthenticatedUser(), resource, GeneralPermissions.MODIFY_RECORD)) {
                    addActionError("abstractPersistableController.unable_to_view_edit");
                }
            }
        );
        setupSharedKeywords();
    }

    public void setupSharedKeywords() {
        boolean first = true;
        for (Resource r : resources) {
            if (first) {
                cultures.addAll(r.getActiveCultureKeywords());
                investigationTypes.addAll(r.getActiveInvestigationTypes());
                temporal.addAll(r.getActiveTemporalKeywords());
                material.addAll(r.getActiveMaterialKeywords());
                other.addAll(r.getActiveOtherKeywords());
                geographic.addAll(r.getActiveGeographicKeywords());
                siteNames.addAll(r.getActiveSiteNameKeywords());
                siteTypes.addAll(r.getActiveSiteTypeKeywords());
                creators.addAll(r.getPrimaryCreators());
                individualRoles.addAll(r.getActiveIndividualAndInstitutionalCredit());
                latitudeLongitude.addAll(r.getActiveLatitudeLongitudeBoxes());
                notes.addAll(r.getActiveResourceNotes());
                coverage.addAll(r.getActiveCoverageDates());
                collections.addAll(r.getSharedResourceCollections());
                annotations.addAll(r.getActiveResourceAnnotations());
                first = false;
            } else {
                cultures = SetUtils.intersection(cultures, r.getActiveCultureKeywords());
                investigationTypes = SetUtils.intersection(investigationTypes, r.getActiveInvestigationTypes());
                temporal = SetUtils.intersection(temporal, r.getActiveTemporalKeywords());
                material = SetUtils.intersection(material, r.getActiveMaterialKeywords());
                other = SetUtils.intersection(other, r.getActiveOtherKeywords());
                geographic = SetUtils.intersection(geographic, r.getActiveGeographicKeywords());
                siteNames = SetUtils.intersection(siteNames, r.getActiveSiteNameKeywords());
                siteTypes = SetUtils.intersection(siteTypes, r.getActiveSiteTypeKeywords());
                creators = SetUtils.intersection( creators, new HashSet<>(r.getPrimaryCreators()));
                individualRoles = SetUtils.intersection(individualRoles , r.getActiveIndividualAndInstitutionalCredit());
                latitudeLongitude = SetUtils.intersection(latitudeLongitude , r.getActiveLatitudeLongitudeBoxes());
                notes = SetUtils.intersection( notes, r.getActiveResourceNotes());
                coverage = SetUtils.intersection( coverage, r.getActiveCoverageDates());
                collections = SetUtils.intersection( collections, r.getSharedResourceCollections());
                annotations = SetUtils.intersection( annotations, r.getActiveResourceAnnotations());
            }
        }
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

    public Set<CultureKeyword> getCultures() {
        return cultures;
    }

    public void setCultures(HashSet<CultureKeyword> cultures) {
        this.cultures = cultures;
    }

    public Set<InvestigationType> getInvestigationTypes() {
        return investigationTypes;
    }

    public void setInvestigationTypes(HashSet<InvestigationType> investigationTypes) {
        this.investigationTypes = investigationTypes;
    }

    public Set<TemporalKeyword> getTemporal() {
        return temporal;
    }

    public void setTemporal(HashSet<TemporalKeyword> temporal) {
        this.temporal = temporal;
    }

    public Set<MaterialKeyword> getMaterial() {
        return material;
    }

    public void setMaterial(HashSet<MaterialKeyword> material) {
        this.material = material;
    }

    public Set<OtherKeyword> getOther() {
        return other;
    }

    public void setOther(HashSet<OtherKeyword> other) {
        this.other = other;
    }

    public Set<GeographicKeyword> getGeographic() {
        return geographic;
    }

    public void setGeographic(HashSet<GeographicKeyword> geographic) {
        this.geographic = geographic;
    }

    public Set<SiteNameKeyword> getSiteNames() {
        return siteNames;
    }

    public void setSiteNames(HashSet<SiteNameKeyword> siteNames) {
        this.siteNames = siteNames;
    }

    public Set<SiteTypeKeyword> getSiteTypes() {
        return siteTypes;
    }

    public void setSiteTypes(HashSet<SiteTypeKeyword> siteTypes) {
        this.siteTypes = siteTypes;
    }

    public Set<ResourceCreator> getCreators() {
        return creators;
    }

    public void setCreators(Set<ResourceCreator> creators) {
        this.creators = creators;
    }

    public Set<ResourceCreator> getIndividualRoles() {
        return individualRoles;
    }

    public void setIndividualRoles(Set<ResourceCreator> individualRoles) {
        this.individualRoles = individualRoles;
    }

    public Set<LatitudeLongitudeBox> getLatitudeLongitude() {
        return latitudeLongitude;
    }

    public void setLatitudeLongitude(Set<LatitudeLongitudeBox> latitudeLongitude) {
        this.latitudeLongitude = latitudeLongitude;
    }

    public Set<ResourceNote> getNotes() {
        return notes;
    }

    public void setNotes(Set<ResourceNote> notes) {
        this.notes = notes;
    }

    public Set<CoverageDate> getCoverage() {
        return coverage;
    }

    public void setCoverage(Set<CoverageDate> coverage) {
        this.coverage = coverage;
    }

    public Set<ResourceCollection> getCollections() {
        return collections;
    }

    public void setCollections(Set<ResourceCollection> collections) {
        this.collections = collections;
    }

    public Set<ResourceAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Set<ResourceAnnotation> annotations) {
        this.annotations = annotations;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

}
