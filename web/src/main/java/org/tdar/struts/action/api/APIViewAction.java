package org.tdar.struts.action.api;

import java.io.InputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.JaxbResultContainer;

@Namespace("/api")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class APIViewAction extends AuthenticationAware.Base {

    private static final long serialVersionUID = 539604938603061219L;

    @Autowired
    private transient SerializationService serializationService;
    @Autowired
    private transient ObfuscationService obfuscationService;
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private Long id;
    private InputStream inputStream;
    private JaxbResultContainer xmlResultObject = new JaxbResultContainer();

    @Action(value = "view", results = {
            @Result(name = SUCCESS, type = "xmldocument") })
    public String view() throws Exception {
        if (PersistableUtils.isNotNullOrTransient(getId())) {
            Resource resource = resourceService.find(getId());
            if (resource == null) {
                getLogger().debug("could not find resource: {}", getId());
                return INPUT;
            }
            if (!isAdministrator() && !authorizationService.canEdit(getAuthenticatedUser(), resource)) {
                obfuscationService.obfuscate(resource, getAuthenticatedUser());
            }
            logMessage("API VIEWING", resource.getClass(), resource.getId(), resource.getTitle());
            getXmlResultObject().setResult(resource);
            return SUCCESS;
        }
        return INPUT;
    }

    public final static String msg_ = "%s is %s %s (%s): %s";

    private void logMessage(String action_, Class<?> cls, Long id_, String name_) {
        getLogger().info(String.format(msg_, getAuthenticatedUser().getEmail(), action_, cls.getSimpleName().toUpperCase(), id_, name_));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public JaxbResultContainer getXmlResultObject() {
        return xmlResultObject;
    }

    public void setXmlResultObject(JaxbResultContainer xmlResultObject) {
        this.xmlResultObject = xmlResultObject;
    }

}
