package org.tdar.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.struts.data.AuthWrapper;

@Service
public class ResourceViewControllerService {

    @Autowired
    private ObfuscationService obfuscationService;

    public void initializeResourceCreatorProxyLists(AuthWrapper<Resource> auth, List<ResourceCreatorProxy> authorshipProxies,
            List<ResourceCreatorProxy> creditProxies, List<ResourceCreatorProxy> contactProxies) {

        Set<ResourceCreator> resourceCreators = auth.getItem().getResourceCreators();
        resourceCreators = auth.getItem().getActiveResourceCreators();
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

            if (proxy.isValidEmailContact()) {
                contactProxies.add(proxy);
            }
        }
        Collections.sort(authorshipProxies);
        Collections.sort(creditProxies);
    }

}
