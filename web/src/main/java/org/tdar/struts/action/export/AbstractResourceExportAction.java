package org.tdar.struts.action.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceExportService;
import org.tdar.struts.action.AuthenticationAware;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/export")
public abstract class AbstractResourceExportAction extends AuthenticationAware.Base implements Preparable {

    private List<Resource> resources;
    private List<Long> ids;

    @Autowired
    ResourceExportService resourceExportService;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    SerializationService serializationService;

    @Override
    public void prepare() throws Exception {
        resources = getGenericService().findAll(Resource.class,ids);
        List<String> issues = new ArrayList<>();
        for (Resource r : resources) {
            if (!authorizationService.canEditResource(getAuthenticatedUser(), r, GeneralPermissions.MODIFY_METADATA)) {
                issues.add(String.format("%s (%s)", r.getTitle(),r.getId()));
            }
        }
        
        if (CollectionUtils.isNotEmpty(resources)) {
            addActionError(getText("abstractResourceExportAction.cannot_export", Arrays.asList(issues)));
        }
    }


    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    

}
