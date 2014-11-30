package org.tdar.core.dao.resource.stats;

import java.io.Serializable;

public class ResourceSpaceUsageStatistic implements Serializable {

    private static final long serialVersionUID = 2229379378756763536L;

    private Number totalSpace;
    private Number countFiles;
    private Number countResources;

    public ResourceSpaceUsageStatistic(Number space, Number fileCount, Number resourceCount) {
        this.totalSpace = space;
        this.countFiles = fileCount;
        this.countResources = resourceCount;
    }

    public Number getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(Number totalSpace) {
        this.totalSpace = totalSpace;
    }

    public Number getCountFiles() {
        return countFiles;
    }

    public void setCountFiles(Number countFiles) {
        this.countFiles = countFiles;
    }

    public Number getCountResources() {
        return countResources;
    }

    public void setCountResources(Number countResources) {
        this.countResources = countResources;
    }

}
