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
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.UserRightsProxyService;
import org.tdar.core.service.collection.ResourceCollectionService;
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

    public boolean isEditable() {
        return true;
    }
    
    private static final Filestore FILESTORE = TdarConfiguration.getInstance().getFilestore();
    private static final long serialVersionUID = -2071449250711089300L;
    public static final String ADMIN = "admin";
    private List<ResourceRevisionLog> resourceLogEntries;
    private List<UserInvite> invites;

    private List<ResourceRevisionLog> logEntries;
    private Set<ResourceCollection> effectiveShares = new HashSet<>();
    private List<File> xmlFiles = new ArrayList<>();

    private Resource resource;
    private Long id;

    @Autowired
    private transient UserRightsProxyService userRightsProxyService;

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
        getEffectiveShares().addAll(resourceCollectionService.getEffectiveSharesForResource(getResource()));
        userRightsProxyService.findUserInvites(getResource());
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
    public Resource getPersistable() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Set<ResourceCollection> getEffectiveShares() {
        return effectiveShares;
    }

    public void setEffectiveShares(Set<ResourceCollection> effectiveResourceCollections) {
        this.effectiveShares = effectiveResourceCollections;
    }

    public List<Permissions> getAvailablePermissions() {
        List<Permissions> permissions = Permissions.getAvailablePermissionsFor(Resource.class);
        return permissions;
    }

    public List<File> getXmlFiles() {
        return xmlFiles;
    }

    public void setXmlFiles(List<File> xmlFiles) {
        this.xmlFiles = xmlFiles;
    }

    public List<UserInvite> getInvites() {
        return invites;
    }

    public void setInvites(List<UserInvite> invites) {
        this.invites = invites;
    }

}
