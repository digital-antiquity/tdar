package org.tdar.web.service;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.service.ResourceCreatorProxy;

public class ResourceControllerProxy implements Serializable {

    private static final long serialVersionUID = 1130227916644870738L;

    List<ResourceAnnotation> incomingAnnotations;
    List<LatitudeLongitudeBox> latitudeLongitudeBoxes;
    List<String> geographicKeywords;
    List<RelatedComparativeCollection> relatedComparativeCollections;
    List<SourceCollection> sourceCollections;
    List<String> siteNameKeywords;
    List<Long> approvedCultureKeywordIds;
    List<Long> approvedSiteTypeKeywordIds;
    List<Long> investigationTypeIds;
    List<Long> approvedMaterialKeywordIds;
    List<String> otherKeywords;
    List<String> uncontrolledCultureKeywords;
    List<String> uncontrolledMaterialKeywords;
    List<String> uncontrolledSiteTypeKeywords;
    List<String> temporalKeywords;
    private List<CoverageDate> coverageDates;
    private List<ResourceNote> resourceNotes;
    private List<ResourceCreatorProxy> resourceCreatorProxies;
    private List<SharedCollection> shares;
    private List<ListCollection> resourceCollections;
    private TdarUser submitter;
    private boolean save;
    
    
    public List<ResourceAnnotation> getIncomingAnnotations() {
        return incomingAnnotations;
    }
    public void setIncomingAnnotations(List<ResourceAnnotation> incomingAnnotations) {
        this.incomingAnnotations = incomingAnnotations;
    }
    public List<LatitudeLongitudeBox> getLatitudeLongitudeBoxes() {
        return latitudeLongitudeBoxes;
    }
    public void setLatitudeLongitudeBoxes(List<LatitudeLongitudeBox> latitudeLongitudeBoxes) {
        this.latitudeLongitudeBoxes = latitudeLongitudeBoxes;
    }
    public List<String> getGeographicKeywords() {
        return geographicKeywords;
    }
    public void setGeographicKeywords(List<String> geographicKeywords) {
        this.geographicKeywords = geographicKeywords;
    }
    public List<RelatedComparativeCollection> getRelatedComparativeCollections() {
        return relatedComparativeCollections;
    }
    public void setRelatedComparativeCollections(List<RelatedComparativeCollection> relatedComparativeCollections) {
        this.relatedComparativeCollections = relatedComparativeCollections;
    }
    public List<SourceCollection> getSourceCollections() {
        return sourceCollections;
    }
    public void setSourceCollections(List<SourceCollection> sourceCollections) {
        this.sourceCollections = sourceCollections;
    }
    public List<String> getSiteNameKeywords() {
        return siteNameKeywords;
    }
    public void setSiteNameKeywords(List<String> siteNameKeywords) {
        this.siteNameKeywords = siteNameKeywords;
    }
    public List<Long> getApprovedCultureKeywordIds() {
        return approvedCultureKeywordIds;
    }
    public void setApprovedCultureKeywordIds(List<Long> approvedCultureKeywordIds) {
        this.approvedCultureKeywordIds = approvedCultureKeywordIds;
    }
    public List<Long> getApprovedSiteTypeKeywordIds() {
        return approvedSiteTypeKeywordIds;
    }
    public void setApprovedSiteTypeKeywordIds(List<Long> approvedSiteTypeKeywordIds) {
        this.approvedSiteTypeKeywordIds = approvedSiteTypeKeywordIds;
    }
    public List<Long> getInvestigationTypeIds() {
        return investigationTypeIds;
    }
    public void setInvestigationTypeIds(List<Long> investigationTypeIds) {
        this.investigationTypeIds = investigationTypeIds;
    }
    public List<Long> getApprovedMaterialKeywordIds() {
        return approvedMaterialKeywordIds;
    }
    public void setApprovedMaterialKeywordIds(List<Long> approvedMaterialKeywordIds) {
        this.approvedMaterialKeywordIds = approvedMaterialKeywordIds;
    }
    public List<String> getOtherKeywords() {
        return otherKeywords;
    }
    public void setOtherKeywords(List<String> otherKeywords) {
        this.otherKeywords = otherKeywords;
    }
    public List<String> getUncontrolledCultureKeywords() {
        return uncontrolledCultureKeywords;
    }
    public void setUncontrolledCultureKeywords(List<String> uncontrolledCultureKeywords) {
        this.uncontrolledCultureKeywords = uncontrolledCultureKeywords;
    }
    public List<String> getUncontrolledMaterialKeywords() {
        return uncontrolledMaterialKeywords;
    }
    public void setUncontrolledMaterialKeywords(List<String> uncontrolledMaterialKeywords) {
        this.uncontrolledMaterialKeywords = uncontrolledMaterialKeywords;
    }
    public List<String> getUncontrolledSiteTypeKeywords() {
        return uncontrolledSiteTypeKeywords;
    }
    public void setUncontrolledSiteTypeKeywords(List<String> uncontrolledSiteTypeKeywords) {
        this.uncontrolledSiteTypeKeywords = uncontrolledSiteTypeKeywords;
    }
    public List<String> getTemporalKeywords() {
        return temporalKeywords;
    }
    public void setTemporalKeywords(List<String> temporalKeywords) {
        this.temporalKeywords = temporalKeywords;
    }
    public boolean isSave() {
        return save;
    }
    public void setSave(boolean save) {
        this.save = save;
    }
    
    public boolean shouldSaveResource() {
        return save;
    }
    
    public List<CoverageDate> getCoverageDates() {
        return coverageDates;
    }
    public void setCoverageDates(List<CoverageDate> coverageDates) {
        this.coverageDates = coverageDates;
    }
    public List<ResourceNote> getResourceNotes() {
        return resourceNotes;
    }
    public void setResourceNotes(List<ResourceNote> resourceNotes) {
        this.resourceNotes = resourceNotes;
    }
    public List<ResourceCreatorProxy> getResourceCreatorProxies() {
        return resourceCreatorProxies;
    }
    public void setResourceCreatorProxies(List<ResourceCreatorProxy> resourceCreatorProxies) {
        this.resourceCreatorProxies = resourceCreatorProxies;
    }
    public List<SharedCollection> getShares() {
        return shares;
    }
    public void setShares(List<SharedCollection> shares) {
        this.shares = shares;
    }
    public List<ListCollection> getResourceCollections() {
        return resourceCollections;
    }
    public void setResourceCollections(List<ListCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }
    public TdarUser getSubmitter() {
        return submitter;
    }
    public void setSubmitter(TdarUser submitter) {
        this.submitter = submitter;
    }


    
}
