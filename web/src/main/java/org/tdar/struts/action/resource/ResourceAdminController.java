package org.tdar.struts.action.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class ResourceAdminController extends AbstractAuthenticatableAction implements Preparable {

    private static final Filestore FILESTORE = TdarConfiguration.getInstance().getFilestore();
	private static final long serialVersionUID = -2071449250711089300L;
    public static final String ADMIN = "admin";
    private List<ResourceRevisionLog> resourceLogEntries;

    private List<ResourceRevisionLog> logEntries;
    private Set<ResourceCollection> effectiveResourceCollections = new HashSet<>();
    private List<File> xmlFiles = new ArrayList<>();

    private Resource resource;
    private Long id;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Action(value = ADMIN, results = {
            @Result(name = SUCCESS, location = "../resource/admin.ftl")
    })
    public String viewAdmin() throws TdarActionException {
        return SUCCESS;
    }

    @Override
    public void prepare() throws Exception {
        if (PersistableUtils.isNotNullOrTransient(getId())) {
            setResource(resourceService.find(getId()));
        } else {
            addActionError(getText("resourceAdminController.valid_resource_required"));
        }
        setResourceLogEntries(resourceService.getLogsForResource(getResource()));
        getEffectiveResourceCollections().addAll(resourceCollectionService.getEffectiveResourceCollectionsForResource(getResource()));
        getXmlFiles().addAll(FILESTORE.listXmlRecordFiles(FilestoreObjectType.RESOURCE, id));
    }

    public List<ResourceRevisionLog> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<ResourceRevisionLog> logEntries) {
        this.logEntries = logEntries;
    }

    public List<ResourceRevisionLog> getResourceLogEntries() {
        return resourceLogEntries;
    }

    public void setResourceLogEntries(List<ResourceRevisionLog> resourceLogEntries) {
        this.resourceLogEntries = resourceLogEntries;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Set<ResourceCollection> getEffectiveResourceCollections() {
        return effectiveResourceCollections;
    }

    public void setEffectiveResourceCollections(Set<ResourceCollection> effectiveResourceCollections) {
        this.effectiveResourceCollections = effectiveResourceCollections;
    }

    public List<GeneralPermissions> getAvailablePermissions() {
        List<GeneralPermissions> permissions = new ArrayList<GeneralPermissions>();
        for (GeneralPermissions permission : GeneralPermissions.values()) {
            if ((permission.getContext() == null) || Resource.class.isAssignableFrom(permission.getContext())) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

	public List<File> getXmlFiles() {
		return xmlFiles;
	}

	public void setXmlFiles(List<File> xmlFiles) {
		this.xmlFiles = xmlFiles;
	}

}
