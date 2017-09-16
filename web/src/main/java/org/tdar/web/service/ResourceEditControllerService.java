package org.tdar.web.service;

import java.util.List;

import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.struts.data.AuthWrapper;

import com.opensymphony.xwork2.TextProvider;

public interface ResourceEditControllerService {

    void initializeResourceCreatorProxyLists(AuthWrapper<Resource> auth, List<ResourceCreatorProxy> authorshipProxies,
            List<ResourceCreatorProxy> creditProxies);

    List<Resource> getPotentialParents(InformationResource persistable, TdarUser submitter, Project project, TextProvider provider);

    <R extends Resource> Boolean isAbleToUploadFiles(TdarUser authenticatedUser, R persistable, List<BillingAccount> activeAccounts);

    // Return list of acceptable billing accounts. If the resource has an account, this method will include it in the returned list even
    // if the user does not have explicit rights to the account (e.g. so that a user w/ edit rights on the resource can modify the resource
    // and maintain original billing account).
    List<BillingAccount> determineActiveAccounts(TdarUser authenticatedUser, Resource resource);

    void updateSharesForEdit(Resource resource, TdarUser authenticatedUser, List<SharedCollection> effectiveShares,
            List<SharedCollection> retainedSharedCollections,
            List<SharedCollection> effectiveResourceCollections, List<SharedCollection> retainedListCollections, List<SharedCollection> shares,
            List<SharedCollection> resourceCollections);

    String loadFilesJson(InformationResource persistable);

}