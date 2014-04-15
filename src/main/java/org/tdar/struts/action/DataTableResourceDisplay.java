package org.tdar.struts.action;

import java.util.List;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.SortOption;

public interface DataTableResourceDisplay {

    public List<ResourceCollection> getAllResourceCollections();

    public List<Status> getStatuses();

    public List<ResourceType> getResourceTypes();

    public List<SortOption> getResourceDatatableSortOptions();

    public List<Resource> getFullUserProjects();

    public List<Project> getAllSubmittedProjects();

}
