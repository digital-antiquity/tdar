package org.tdar.core.dao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.utils.MathUtils;

/*
 * This class is designed to help figure out what resources (files, resources, space) that a tDAR Resource is taking up.
 * Some resources, like Ontologies, CodingSheets, etc. you get for free.
 * 
 * A Resource Evaluator is initialized with a BillingModel which tells it some of how to evaluate things ... as we decide, we
 * may need to port more of the decisions into that boolean logic
 * 
 */
public class ResourceEvaluator implements Serializable {

    private static final long serialVersionUID = 3621509880429873050L;

    private boolean includeDeletedFilesInCounts = false;
    private boolean hasDeletedResources = false;
    private boolean includeAllVersionsInCounts = false;
    private List<ResourceType> uncountedResourceTypes = Arrays.asList(ResourceType.CODING_SHEET, ResourceType.ONTOLOGY, ResourceType.PROJECT);
    private List<Status> uncountedResourceStatuses = Arrays.asList(Status.DELETED);
    private long resourcesUsed = 0;
    private long filesUsed = 0;
    private long spaceUsedInBytes = 0;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private Set<Long> resourceIds = new HashSet<Long>();
    private BillingActivityModel model;

    /*
     * Creates a Resource Evaluator passing in a @link BillingActivityModel to specify what charges get charged
     * 
     * @param BillingActivityModel model the billing model to use
     */
    public ResourceEvaluator(BillingActivityModel model) {
        this.model = model;
    }

    /*
     * Creates a Resource Evaluator passing in a @link BillingActivityModel to specify what charges get charged, and a set of @link Resource(s) to evaluate
     * 
     * @param BillingActivityModel model the billing model to use
     * 
     * @param Resource[] resources to evaluate
     */
    public ResourceEvaluator(BillingActivityModel model, Resource... resources) {
        this.model = model;
        evaluateResources(resources);
    }

    /*
     * Checks that a the account has enough for one resource based on the account balance and the type of resource based on the settings of the
     * ResourceEvaluator, and the ResourceType.
     */
    public boolean accountHasMinimumForNewResource(BillingAccount account, ResourceType resourceType) {
        logger.trace("f: {} s: {} r: {}",
                new Object[] { account.getAvailableNumberOfFiles(), account.getAvailableSpaceInMb(), account.getAvailableResources() });
        if (evaluatesNumberOfResources()) {
            if (!getUncountedResourceTypes().contains(resourceType) && (account.getAvailableResources() <= 0)) {
                return false;
            }
        }
        if (evaluatesNumberOfFiles() && (account.getAvailableNumberOfFiles() <= 0)) {
            return false;
        }
        if (evaluatesSpace() && (account.getAvailableSpaceInMb() <= 0)) {
            return false;
        }
        return true;
    }

    /*
     * Convenience method
     */
    public void evaluateResources(Collection<? extends Resource> resources) {
        evaluateResources(resources.toArray(new Resource[0]));
    }

    /*
     * Evaluate what a resource "counts" as and set the transient flags on the resource to track usage. Resource has a set of Longs which track current file and
     * MB usage. The evaluator sets additional flags to track the difference between what was "before" the transaction and what is currently being used.
     */
    public void evaluateResources(Resource... resources) {

        for (Resource resource : resources) {
            if (resource == null) {
                continue;
            }

            // evaluate Resource count
            Status status = Status.ACTIVE;
            if (resource.isTransient()) {
                logger.warn("Resource {} is transient, it may not be updated properly", resource);
            }
            // add them to a list of internally tracked resources for future use
            getResourceIds().add(resource.getId());
            if (resource.getStatus() != null) {
                status = resource.getStatus();
            }

            if (uncountedResourceStatuses.contains(status) && resource.getStatusChanged()) {
                logger.debug("{} {}", resource.getId(), status);
                setHasDeletedResources(true);
            }

            // esacape if we're dealing with an uncounted status or resource type
            if (uncountedResourceTypes.contains(resource.getResourceType()) || uncountedResourceStatuses.contains(status)) {
                logger.trace("skipping because of status {} or type: {}", status, resource.getResourceType());
                resource.setCountedInBillingEvaluation(false);
                continue;
            }

            resourcesUsed++;
            // evaluate file count, then space
            long filesUsed_ = 0;
            long spaceUsed_ = 0;
            if (resource instanceof InformationResource) {
                InformationResource informationResource = (InformationResource) resource;
                for (InformationResourceFile file : informationResource.getInformationResourceFiles()) {
                    if (file.isDeleted() && !includeDeletedFilesInCounts) {
                        continue;
                    }

                    // composite files, like GIS or datasets are counted differently, one file per composite in total, regardless of actual files
                    if (informationResource.getResourceType().isCompositeFilesEnabled()) {
                        filesUsed_ = 1;
                    } else {
                        filesUsed_++;
                    }

                    // count space
                    for (InformationResourceFileVersion version : file.getInformationResourceFileVersions()) {
                        // we use version 1 because it's the original uploaded version
                        if ((!includeAllVersionsInCounts && !version.getVersion().equals(1)) || !version.isUploaded()) {
                            continue;
                        }
                        if (version.getFileLength() != null) {
                            spaceUsed_ += version.getFileLength();
                        }
                    }
                }
            }

            // set the transient values
            resource.setSpaceInBytesUsed(spaceUsed_);
            resource.setFilesUsed(filesUsed_);
            spaceUsedInBytes += spaceUsed_;
            filesUsed += filesUsed_;
        }
    }

    @Override
    public String toString() {
        return String.format("%s resources %s files %s mb", getResourcesUsed(), getFilesUsed(), getSpaceUsedInMb());
    }

    public boolean isIncludeDeletedFilesInCounts() {
        return includeDeletedFilesInCounts;
    }

    public void setIncludeDeletedFilesInCounts(boolean includeDeletedFilesInCounts) {
        this.includeDeletedFilesInCounts = includeDeletedFilesInCounts;
    }

    public boolean isIncludeOlderVersionsInCounts() {
        return includeAllVersionsInCounts;
    }

    public void setIncludeOlderVersionsInCounts(boolean includeOlderVersionsInCounts) {
        this.includeAllVersionsInCounts = includeOlderVersionsInCounts;
    }

    public List<ResourceType> getUncountedResourceTypes() {
        return uncountedResourceTypes;
    }

    public void setUncountedResourceTypes(List<ResourceType> uncountedResourceTypes) {
        this.uncountedResourceTypes = uncountedResourceTypes;
    }

    public long getResourcesUsed() {
        return resourcesUsed;
    }

    public void setResourcesUsed(long resourcesUsed) {
        this.resourcesUsed = resourcesUsed;
    }

    public long getFilesUsed() {
        return filesUsed;
    }

    public void setFilesUsed(long filesUsed) {
        this.filesUsed = filesUsed;
    }

    public long getSpaceUsedInBytes() {
        return spaceUsedInBytes;
    }

    public long getSpaceUsedInMb() {
        return (long) Math.ceil((double) spaceUsedInBytes / (double) MathUtils.ONE_MB);
    }

    public void setSpaceUsed(long spaceUsed) {
        this.spaceUsedInBytes = spaceUsed;
    }

    public List<Status> getUncountedResourceStatuses() {
        return uncountedResourceStatuses;
    }

    public void setUncountedResourceStatuses(List<Status> uncountedResourceStatuses) {
        this.uncountedResourceStatuses = uncountedResourceStatuses;
    }

    public BillingActivityModel getModel() {
        return model;
    }

    /*
     * Used to compare two different resource evaluators -- at the beginning of an operation and at the end, for example to see what the effective difference
     * for the account would or should be
     */
    public void subtract(ResourceEvaluator initialEvaluation) {
        if (!initialEvaluation.getModel().equals(getModel())) {
            throw new TdarValidationException("resourceEvaluator.two_different_models");
        }
        setSpaceUsed(getSpaceUsedInBytes() - initialEvaluation.getSpaceUsedInBytes());
        setFilesUsed(getFilesUsed() - initialEvaluation.getFilesUsed());
        setResourcesUsed(getResourcesUsed() - initialEvaluation.getResourcesUsed());
    }

    public boolean evaluatesSpace() {
        return model.getCountingSpace();
    }

    public boolean evaluatesNumberOfResources() {
        return model.getCountingResources();
    }

    public boolean evaluatesNumberOfFiles() {
        return model.getCountingFiles();
    }

    public Set<Long> getResourceIds() {
        return resourceIds;
    }

    public boolean isHasDeletedResources() {
        return hasDeletedResources;
    }

    public void setHasDeletedResources(boolean hasDeletedResources) {
        this.hasDeletedResources = hasDeletedResources;
    }

}
