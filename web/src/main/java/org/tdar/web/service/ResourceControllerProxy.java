package org.tdar.web.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.service.ResourceCreatorProxy;

import com.opensymphony.xwork2.TextProvider;

public class ResourceControllerProxy<R extends Resource> implements Serializable {

    private static final long serialVersionUID = 1130227916644870738L;

    public ResourceControllerProxy(TextProvider provider) {
        this.provider = provider;
    }

    private R resource;
    private Ontology ontology;
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
    private String resourceProviderInstitutionName;
    private String publisherName;
    private Long accountId;
    private List<ResourceCreatorProxy> creditProxies;
    private List<ResourceCreatorProxy> authorshipProxies;
    private List<CoverageDate> coverageDates;
    private List<ResourceNote> resourceNotes;
    private List<ResourceCollection> shares;
    private List<ResourceCollection> resourceCollections;
    private TdarUser submitter;
    private boolean save;
    private ResourceCreatorProxy copyrightHolderProxies;
    private Long ticketId;
    private List<FileProxy> fileProxiesToProcess;
    private Collection<String> validFileExtensions;
    private TextProvider provider;
    private Long categoryId;
    private Long subcategoryId;

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

    public List<ResourceCollection> getShares() {
        return shares;
    }

    public void setShares(List<ResourceCollection> shares) {
        this.shares = shares;
    }

    public List<ResourceCollection> getResourceCollections() {
        return resourceCollections;
    }

    public void setResourceCollections(List<ResourceCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    public TdarUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
        this.submitter = submitter;
    }

    public void handleInheritance() {
        // don't save any values at the resource level that we are inheriting
        // from parent
        if (resource instanceof InformationResource) {
            InformationResource ir = (InformationResource) resource;

            if (ir.isInheritingInvestigationInformation()) {
                setInvestigationTypeIds(null);
            }
            if (ir.isInheritingSiteInformation()) {
                setSiteNameKeywords(null);
                setApprovedSiteTypeKeywordIds(null);
                setUncontrolledSiteTypeKeywords(null);
            }
            if (ir.isInheritingMaterialInformation()) {
                setApprovedMaterialKeywordIds(null);
                setUncontrolledCultureKeywords(null);
            }
            if (ir.isInheritingCulturalInformation()) {
                setApprovedCultureKeywordIds(null);
                setUncontrolledCultureKeywords(null);
            }
            if (ir.isInheritingSpatialInformation()) {
                getLatitudeLongitudeBoxes().clear();
                setGeographicKeywords(null);
            }
            if (ir.isInheritingTemporalInformation()) {
                setTemporalKeywords(null);
                ir.getCoverageDates().clear();
            }
            if (ir.isInheritingOtherInformation()) {
                setOtherKeywords(null);
            }

            if (ir.isInheritingIndividualAndInstitutionalCredit()) {
                if (CollectionUtils.isNotEmpty(getCreditProxies())) {
                    getCreditProxies().clear();
                }
            }

            if (ir.isInheritingCollectionInformation()) {
                if (CollectionUtils.isNotEmpty(getRelatedComparativeCollections())) {
                    getRelatedComparativeCollections().clear();
                }
                if (CollectionUtils.isNotEmpty(getSourceCollections())) {
                    getSourceCollections().clear();
                }
            }

            if (ir.isInheritingNoteInformation()) {
                if (CollectionUtils.isNotEmpty(getResourceNotes())) {
                    getResourceNotes().clear();
                }
            }

            if (ir.isInheritingIdentifierInformation()) {
                if (CollectionUtils.isNotEmpty(getIncomingAnnotations())) {
                    getIncomingAnnotations().clear();
                }
            }
        }
    }

    public R getResource() {
        return resource;
    }

    public void setResource(R resource) {
        this.resource = resource;
    }

    public List<ResourceCreatorProxy> getAuthorshipProxies() {
        return authorshipProxies;
    }

    public void setAuthorshipProxies(List<ResourceCreatorProxy> authorshipProxies) {
        this.authorshipProxies = authorshipProxies;
    }

    public List<ResourceCreatorProxy> getCreditProxies() {
        return creditProxies;
    }

    public void setCreditProxies(List<ResourceCreatorProxy> creditProxies) {
        this.creditProxies = creditProxies;
    }

    public void setCopyrightHolder(ResourceCreatorProxy copyrightHolderProxies) {
        this.setCopyrightHolderProxies(copyrightHolderProxies);
        // TODO Auto-generated method stub

    }

    public ResourceCreatorProxy getCopyrightHolderProxies() {
        return copyrightHolderProxies;
    }

    public void setCopyrightHolderProxies(ResourceCreatorProxy copyrightHolderProxies) {
        this.copyrightHolderProxies = copyrightHolderProxies;
    }

    public String getResourceProviderInstitutionName() {
        return resourceProviderInstitutionName;
    }

    public void setResourceProviderInstitutionName(String resourceProviderInstitutionName) {
        this.resourceProviderInstitutionName = resourceProviderInstitutionName;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;

    }

    public void setFileProxies(List<FileProxy> fileProxiesToProcess) {
        this.setFileProxiesToProcess(fileProxiesToProcess);
    }

    public void setValidFileExtensions(Collection<String> validFileExtensions) {
        this.validFileExtensions = validFileExtensions;

    }

    public List<FileProxy> getFileProxiesToProcess() {
        return fileProxiesToProcess;
    }

    public void setFileProxiesToProcess(List<FileProxy> fileProxiesToProcess) {
        this.fileProxiesToProcess = fileProxiesToProcess;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public Collection<String> getValidFileExtensions() {
        return validFileExtensions;
    }

    public TextProvider getProvider() {
        return provider;
    }

    public void setProvider(TextProvider provider) {
        this.provider = provider;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

}
