package org.tdar.web.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.UserRightsProxyService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.utils.PersistableUtils;

@Service
public class ResourceViewControllerService {

    @Autowired
    private ObfuscationService obfuscationService;
    @Autowired
    private BillingAccountService accountService;

    @Autowired
    private BookmarkedResourceService bookmarkedResourceService;
    
    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient UserRightsProxyService userRightsProxyService;

    @Autowired
    private transient InformationResourceFileService informationResourceFileService;

    
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

    @Transactional(readOnly=false)
    public void updateResourceInfo(AuthWrapper<Resource> auth, boolean isBot) {
        // don't count if we're an admin
        if (!PersistableUtils.isEqual(auth.getItem().getSubmitter(), auth.getAuthenticatedUser()) && !auth.isEditor()) {
            resourceService.incrementAccessCounter(auth.getItem(), isBot);
        }
        updateInfoReadOnly(auth);
        
    }

    @Transactional(readOnly=true)
    public void updateInfoReadOnly(AuthWrapper<Resource> auth) {
        // only showing access count when logged in (speeds up page loads)
        if (auth.isAuthenticated()) {
            resourceService.updateTransientAccessCount(auth.getItem());
        }
        accountService.updateTransientAccountInfo((List<Resource>) Arrays.asList(auth.getItem()));
        bookmarkedResourceService.applyTransientBookmarked(Arrays.asList(auth.getItem()), auth.getAuthenticatedUser());
    
    
        if (auth.getItem() instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) auth.getItem();
            setTransientViewableStatus(informationResource, auth.getAuthenticatedUser());
        }
    }
        
    /*
     * Creating a simple transient boolean to handle visibility here instead of freemarker
     */
    public void setTransientViewableStatus(InformationResource ir, TdarUser p) {
        authorizationService.applyTransientViewableFlag(ir, p);
        if (PersistableUtils.isNotNullOrTransient(p)) {
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                informationResourceFileService.updateTransientDownloadCount(irf);
//                if (irf.isDeleted()) {
//                    setHasDeletedFiles(true);
//                }
            }
        }
    }

    @Transactional(readOnly= true)
    public void loadSharesCollectionsAuthUsers(AuthWrapper<Resource> auth, List<RightsBasedResourceCollection> effectiveShares, List<ListCollection> effectiveResourceCollections,
            List<AuthorizedUser> authorizedUsers) {
        authorizedUsers.addAll(resourceCollectionService.getAuthorizedUsersForResource(auth.getItem(), auth.getAuthenticatedUser()));
        effectiveShares.addAll(resourceCollectionService.getEffectiveSharesForResource(auth.getItem()));
        effectiveResourceCollections.addAll(resourceCollectionService.getEffectiveResourceCollectionsForResource(auth.getItem()));
        
    }


}
