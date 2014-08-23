package org.tdar.struts.action.resource;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.CrudAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;

import com.opensymphony.xwork2.Preparable;

import edu.asu.lib.dc.DublinCoreDocument;
import edu.asu.lib.mods.ModsDocument;

@Namespace("/unapi")
@Component
@Scope("prototype")
@ParentPackage("default")
@Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.HTTPHEADER, params = { "status", "400" })
public class JAXBMetadataViewController extends AuthenticationAware.Base implements Preparable, CrudAction<Resource> {

    private static final long serialVersionUID = -7297306518597493712L;
    public static final String DC = "dc";
    public static final String MODS = "mods";
    private ModsDocument modsDocument;
    private DublinCoreDocument dcDocument;

    @Autowired
    private transient ObfuscationService obfuscationService;

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient GenericService genericService;

    private Long id;
    private Resource resource;

    public ModsDocument getModsDocument() {
        if (modsDocument == null) {
            obfuscationService.obfuscate(getResource(), getAuthenticatedUser());
            modsDocument = ModsTransformer.transformAny(getResource());
        }
        return modsDocument;
    }

    @SkipValidation
    @Action(value = MODS, results = {
            @Result(name = SUCCESS, type = JAXBRESULT, params = { "documentName", "modsDocument", "formatOutput", "true" })
    })
    public String viewMods() throws TdarActionException {
        checkValidRequest(RequestType.VIEW, this, InternalTdarRights.VIEW_ANYTHING);
        return SUCCESS;
    }

    public DublinCoreDocument getDcDocument() {
        if (dcDocument == null) {
            obfuscationService.obfuscate(getResource(), getAuthenticatedUser());
            dcDocument = DcTransformer.transformAny(getResource());
        }
        return dcDocument;
    }

    @SkipValidation
    @Action(value = DC, results = {
            @Result(name = SUCCESS, type = JAXBRESULT, params = { "documentName", "dcDocument", "formatOutput", "true" })
    })
    public String viewDc() throws TdarActionException {
        checkValidRequest(RequestType.VIEW, this, InternalTdarRights.VIEW_ANYTHING);
        return SUCCESS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void prepare() throws Exception {
        resource = genericService.find(Resource.class, id);
        if (Persistable.Base.isNullOrTransient(resource)) {
            addActionError(getText("jaxbMetadataViewController.resource_does_not_exist"));
        }
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public boolean isCreatable() throws TdarActionException {
        return false;
    }

    @Override
    public boolean isEditable() throws TdarActionException {
        return authorizationService.isResourceEditable(getAuthenticatedUser(), getResource());
    }

    @Override
    public boolean isSaveable() throws TdarActionException {
        return false;
    }

    @Override
    public boolean isDeleteable() throws TdarActionException {
        return false;
    }

    @Override
    public boolean isViewable() throws TdarActionException {
        return authorizationService.isResourceViewable(getAuthenticatedUser(), getResource());
    }

    @Override
    public Persistable getPersistable() {
        return resource;
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }
}
