package org.tdar.web.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.struts.data.AuthWrapper;

import com.opensymphony.xwork2.TextProvider;

@Service
public class ResourceEditControllerService {

    @Autowired
    private ObfuscationService obfuscationService;
    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient GenericService genericService;
    @Autowired
    private transient ProjectService projectService;


    @Transactional(readOnly = true)
    public void initializeResourceCreatorProxyLists(AuthWrapper<Resource> auth, List<ResourceCreatorProxy> authorshipProxies,
            List<ResourceCreatorProxy> creditProxies) {
        Set<ResourceCreator> resourceCreators = auth.getItem().getResourceCreators();
        if (resourceCreators == null) {
            return;
        }

        // this may be duplicative... check
        for (ResourceCreator rc : resourceCreators) {
            if (TdarConfiguration.getInstance().obfuscationInterceptorDisabled()) {
                if ((rc.getCreatorType() == CreatorType.PERSON) && !auth.isAuthenticated()) {
                    obfuscationService.obfuscate(rc.getCreator(), auth.getAuthenticatedUser());
                }
            }

            ResourceCreatorProxy proxy = new ResourceCreatorProxy(rc);
            if (ResourceCreatorRole.getAuthorshipRoles().contains(rc.getRole())) {
                authorshipProxies.add(proxy);
            } else {
                creditProxies.add(proxy);
            }
        }
    }


    public List<Resource> getPotentialParents(InformationResource persistable, TdarUser submitter, Project project, TextProvider provider ) {
        List<Resource> potentialParents = new LinkedList<>();
        boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, submitter);
        potentialParents.addAll(projectService.findSparseTitleIdProjectListByPerson(submitter, canEditAnything));
        if (!Objects.equals(project,Project.NULL) && !potentialParents.contains(project)) {
            potentialParents.add(project);
        }
        // Prepend null project so that dropdowns will see "No associated project" at the top of the list.
        Project noAssociatedProject = new Project(-1L, provider.getText("project.no_associated_project"));
        genericService.markReadOnly(project);
        potentialParents.add(0, noAssociatedProject);
        return potentialParents;
    }
}
