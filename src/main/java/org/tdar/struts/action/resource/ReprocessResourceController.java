package org.tdar.struts.action.resource;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.CrudAction;
import org.tdar.struts.action.TdarActionException;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource")
public class ReprocessResourceController extends AuthenticationAware.Base implements Preparable, CrudAction<InformationResource> {

    private static final long serialVersionUID = 7557491462701114284L;
    public static final String RETRANSLATE = "retranslate";
    public static final String REPROCESS = "reprocess";
    public static final String REIMPORT = "reimport";

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private InformationResourceService informationResourceService;

    private InformationResource resource;
    private Long id;

    @Action(value = REPROCESS, results = { @Result(name = SUCCESS, type = REDIRECT, location = URLConstants.VIEW_RESOURCE_ID_AS_ID) })
    public String reprocess() throws TdarActionException {
        getLogger().info("reprocessing");
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        try {
            ErrorTransferObject errors = informationResourceService.reprocessInformationResourceFiles(resource);
            processErrorObject(errors);
        } catch (Exception e) {
            // consider removing the "sorry we were unable to ... just showing error message"
            // addActionErrorWithException(null, e);
            addActionErrorWithException(getText("abstractResourceController.we_were_unable_to_process_the_uploaded_content"), e);
        }
        if (hasActionErrors()) {
            return ERROR;
        }
        return SUCCESS;
    }

    @Action(value = REIMPORT, results = { @Result(name = SUCCESS, type = REDIRECT, location = URLConstants.VIEW_RESOURCE_ID_AS_ID) })
    public String reimport() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        if (resource instanceof Dataset) {
            Dataset dataset = (Dataset) resource;
            // note this ignores the quota changes -- it's on us
            datasetService.reprocess(dataset);
        } else {
            addActionError(getText("reprocessResourceController.dataset_required"));
            return ERROR;
        }
        return SUCCESS;
    }

    /**
     * Retranslates the given dataset.
     * XXX: does this need a WritableSession?
     */
    @Action(value = RETRANSLATE, results = { @Result(name = SUCCESS, type = REDIRECT, location = URLConstants.VIEW_RESOURCE_ID_AS_ID) })
    public String retranslate() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        if (resource instanceof Dataset) {
            Dataset dataset = (Dataset) resource;
            // note this ignores the quota changes -- it's on us
            for (DataTable table : dataset.getDataTables()) {
                datasetService.retranslate(table.getDataTableColumns());
            }
            datasetService.createTranslatedFile(dataset);
        } else {
            addActionError(getText("reprocessResourceController.dataset_required"));
            return ERROR;
        }
        return SUCCESS;
    }

    @Override
    public void prepare() {
        if (Persistable.Base.isNotNullOrTransient(getId())) {
            resource = resourceService.find(getId());
            resource = getGenericService().markWritable(resource);
        } else {
            addActionError(getText("reprocessResourceController.valid_resource_required"));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean isCreatable() throws TdarActionException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEditable() throws TdarActionException {
        return getAuthorizationService().canEditResource(getAuthenticatedUser(), resource, GeneralPermissions.MODIFY_METADATA);
    }

    @Override
    public boolean isSaveable() throws TdarActionException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDeleteable() throws TdarActionException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Persistable getPersistable() {
        return resource;
    }

    @Override
    public Class<InformationResource> getPersistableClass() {
        return InformationResource.class;
    }
}
