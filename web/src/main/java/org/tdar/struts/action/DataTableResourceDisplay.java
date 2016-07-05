package org.tdar.struts.action;

import java.util.List;

import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;

public interface DataTableResourceDisplay {

    public List<SharedCollection> getAllResourceCollections();

    public List<Status> getStatuses();

    public List<ResourceType> getResourceTypes();

    public List<SortOption> getResourceDatatableSortOptions();

    public List<Resource> getFullUserProjects();

    public List<Project> getAllSubmittedProjects();

}
