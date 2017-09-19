package org.tdar.web.service;

import java.util.List;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.struts.data.AuthWrapper;

public interface ResourceViewControllerService {

    void initializeResourceCreatorProxyLists(AuthWrapper<Resource> auth, List<ResourceCreatorProxy> authorshipProxies,
            List<ResourceCreatorProxy> creditProxies, List<ResourceCreatorProxy> contactProxies);

    void updateResourceInfo(AuthWrapper<Resource> auth, boolean isBot);

    void updateInfoReadOnly(AuthWrapper<Resource> auth);

    /*
     * Creating a simple transient boolean to handle visibility here instead of freemarker
     */
    boolean setTransientViewableStatus(InformationResource ir, TdarUser p);

    void loadSharesCollectionsAuthUsers(AuthWrapper<Resource> auth, List<ResourceCollection> effectiveShares,
            List<ResourceCollection> effectiveResourceCollections,
            List<AuthorizedUser> authorizedUsers);

    List<ResourceCollection> getVisibleCollections(AuthWrapper<Resource> auth);

}