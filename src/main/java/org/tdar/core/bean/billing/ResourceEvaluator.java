package org.tdar.core.bean.billing;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;

public class ResourceEvaluator implements Serializable {

    private static final long serialVersionUID = 3621509880429873050L;
    private boolean includeDeletedFilesInCounts = false;
    private boolean includeOlderVersionsInCounts = false;
    private List<ResourceType> uncountedResourceTypes = Arrays.asList(ResourceType.CODING_SHEET, ResourceType.ONTOLOGY, ResourceType.PROJECT);
    private int resourcesUsed = 0;
    private int filesUsed = 0;
    private long spaceUsed = 0;

    public void evaluateResource(Resource resource) {
        if (uncountedResourceTypes.contains(resource.getResourceType()))
            return;

        resourcesUsed++;

        if (resource instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) resource;
            for (InformationResourceFile file : informationResource.getInformationResourceFiles()) {
                if (file.isDeleted() && !includeDeletedFilesInCounts)
                    continue;
                filesUsed++;
                for (InformationResourceFileVersion version : file.getInformationResourceFileVersions()) {
                    if (!includeOlderVersionsInCounts && !version.getVersion().equals(file.getLatestVersion()) || !version.isUploaded())
                        continue;
                    spaceUsed += version.getFileLength();
                }
            }
        }
    }

    public boolean isIncludeDeletedFilesInCounts() {
        return includeDeletedFilesInCounts;
    }

    public void setIncludeDeletedFilesInCounts(boolean includeDeletedFilesInCounts) {
        this.includeDeletedFilesInCounts = includeDeletedFilesInCounts;
    }

    public boolean isIncludeOlderVersionsInCounts() {
        return includeOlderVersionsInCounts;
    }

    public void setIncludeOlderVersionsInCounts(boolean includeOlderVersionsInCounts) {
        this.includeOlderVersionsInCounts = includeOlderVersionsInCounts;
    }

    public List<ResourceType> getUncountedResourceTypes() {
        return uncountedResourceTypes;
    }

    public void setUncountedResourceTypes(List<ResourceType> uncountedResourceTypes) {
        this.uncountedResourceTypes = uncountedResourceTypes;
    }

    public int getResourcesUsed() {
        return resourcesUsed;
    }

    public void setResourcesUsed(int resourcesUsed) {
        this.resourcesUsed = resourcesUsed;
    }

    public int getFilesUsed() {
        return filesUsed;
    }

    public void setFilesUsed(int filesUsed) {
        this.filesUsed = filesUsed;
    }

    public long getSpaceUsed() {
        return spaceUsed;
    }

    public void setSpaceUsed(long spaceUsed) {
        this.spaceUsed = spaceUsed;
    }
    
    
}
