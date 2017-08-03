package org.tdar.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
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
    @Transactional(readOnly=true)
    public boolean setTransientViewableStatus(InformationResource ir, TdarUser p) {
        boolean hasDeleted = false;
        authorizationService.applyTransientViewableFlag(ir, p);
        if (PersistableUtils.isNotNullOrTransient(p)) {
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                informationResourceFileService.updateTransientDownloadCount(irf);
                if (irf.isDeleted()) {
                    hasDeleted=true;
                }
            }
        }
        return hasDeleted;
    }

    @Transactional(readOnly= true)
    public void loadSharesCollectionsAuthUsers(AuthWrapper<Resource> auth, List<SharedCollection> effectiveShares, List<ListCollection> effectiveResourceCollections,
            List<AuthorizedUser> authorizedUsers) {
        authorizedUsers.addAll(resourceCollectionService.getAuthorizedUsersForResource(auth.getItem(), auth.getAuthenticatedUser()));
        effectiveShares.addAll(resourceCollectionService.getEffectiveSharesForResource(auth.getItem()));
        effectiveResourceCollections.addAll(resourceCollectionService.getEffectiveResourceCollectionsForResource(auth.getItem()));
        
    }


    @Transactional(readOnly=true)
    public List<ResourceCollection> getVisibleCollections(AuthWrapper<Resource> auth ) {
        List<ResourceCollection> visibleCollections = new ArrayList<>();
        visibleCollections.addAll(getViewableListResourceCollections(auth));
        visibleCollections.addAll(getViewableSharedResourceCollections(auth));
        return visibleCollections;
    }


    private Set<SharedCollection> getViewableSharedResourceCollections(AuthWrapper<Resource> auth) {

        // if nobody logged in, just get the shared+visible collections
        Set<SharedCollection> collections = new HashSet<>(auth.getItem().getVisibleSharedResourceCollections());
        addViewableCollections(collections, auth.getItem().getSharedCollections(),auth);
        return collections;
    }
    
    // return all of the collections that the currently-logged-in user is allowed to view. We define viewable as either shared+visible, or
    // shared+invisible+canEdit
    private  Set<ListCollection> getViewableListResourceCollections(AuthWrapper<Resource> auth) {

        // if nobody logged in, just get the shared+visible collections
        Set<ListCollection> collections = new HashSet<>();
        collections.addAll(auth.getItem().getVisibleUnmanagedResourceCollections());
        // if authenticated, also add the collections that the user can modify
        addViewableCollections(collections,auth.getItem().getUnmanagedResourceCollections(), auth);

        return collections;
    }

    private <C extends ResourceCollection> void addViewableCollections(Set<C> list, Collection<C> incomming, AuthWrapper<Resource> auth) {
        if (auth.isAuthenticated()) {
            for (C resourceCollection : incomming) {
                if (authorizationService.canViewCollection(auth.getAuthenticatedUser(), resourceCollection) && !resourceCollection.isSystemManaged()) {
                    list.add(resourceCollection);
                }
            }
        }
    }

    

}
