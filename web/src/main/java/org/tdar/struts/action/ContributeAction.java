package org.tdar.struts.action;

import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

/**
 * $Id$
 * 
 * <p>
 * Action for the root namespace.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Namespace("/")
@ParentPackage("default")
@Component
@Scope("prototype")
@HttpsOnly
public class ContributeAction extends AuthenticationAware.Base {

    private static final long serialVersionUID = -8040859305171503597L;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private transient FileAnalyzer analyzer;

    @Override
    @Action(value = "contribute", results = { @Result(name = SUCCESS, location = "contribute.ftl"),
            @Result(name = AUTHENTICATED, type = TDAR_REDIRECT, location = "/resource/add") })
    public String execute() {
        if (isAuthenticated()) {
            return AUTHENTICATED;
        }
        return SUCCESS;
    }

    public Set<String> getDocumentTypes() {
        return analyzer.getExtensionsForType(ResourceType.DOCUMENT);
    }

    public Set<String> getImageTypes() {
        return analyzer.getExtensionsForType(ResourceType.IMAGE);
    }

    public Set<String> getDatasetTypes() {
        return analyzer.getExtensionsForType(ResourceType.DATASET);
    }

}
