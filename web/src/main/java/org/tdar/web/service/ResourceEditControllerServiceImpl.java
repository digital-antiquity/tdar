package org.tdar.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

@Service
public class ResourceEditControllerServiceImpl implements ResourceEditControllerService {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ObfuscationService obfuscationService;
    @Autowired
    private SerializationService serializationService;
    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient GenericService genericService;
    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private BillingAccountService accountService;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.ResourceEditControllerService#initializeResourceCreatorProxyLists(org.tdar.struts.data.AuthWrapper, java.util.List,
     * java.util.List)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.ResourceEditControllerService#getPotentialParents(org.tdar.core.bean.resource.InformationResource,
     * org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Project, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> getPotentialParents(InformationResource persistable, TdarUser submitter, Project project, TextProvider provider) {
        List<Resource> potentialParents = new LinkedList<>();
        boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, submitter);
        potentialParents.addAll(projectService.findSparseTitleIdProjectListByPerson(submitter, canEditAnything));
        if (!Objects.equals(project, Project.NULL) && !potentialParents.contains(project)) {
            potentialParents.add(project);
        }
        // Prepend null project so that dropdowns will see "No associated project" at the top of the list.
        Project noAssociatedProject = new Project(-1L, provider.getText("project.no_associated_project"));
        genericService.markReadOnly(project);
        potentialParents.add(0, noAssociatedProject);
        return potentialParents;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.ResourceEditControllerService#isAbleToUploadFiles(org.tdar.core.bean.entity.TdarUser, R, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public <R extends Resource> Boolean isAbleToUploadFiles(TdarUser authenticatedUser, R persistable, List<BillingAccount> activeAccounts) {
        // Check if the user has permission to upload files, if not

        if (TdarConfiguration.getInstance().isPayPerIngestEnabled() == false || PersistableUtils.isNullOrTransient(persistable)) {
            return true;
        }

        boolean isAbleToUploadFiles = authorizationService.canUploadFiles(authenticatedUser, persistable);

        if (isAbleToUploadFiles == false) {
            return false;
        }

        if (persistable.getAccount() != null) {
            List<BillingAccount> _activeAccounts = activeAccounts;
            logger.debug("_activeAccounts:{}", _activeAccounts);
            // BillingAccount account = _activeAccounts.stream().filter(a ->
            // ObjectUtils.equals(a,getPersistable().getAccount())).collect(Collectors.toList()).get(0);
            if (!persistable.getAccount().isActive()) {
                isAbleToUploadFiles = false;
            }
        }

        return isAbleToUploadFiles;
    }

    // Return list of acceptable billing accounts. If the resource has an account, this method will include it in the returned list even
    // if the user does not have explicit rights to the account (e.g. so that a user w/ edit rights on the resource can modify the resource
    // and maintain original billing account).
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.ResourceEditControllerService#determineActiveAccounts(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public List<BillingAccount> determineActiveAccounts(TdarUser authenticatedUser, Resource resource) {
        // Get all available active accounts for the user. If the resource is being edited, and its associated account is over-limit, this list will
        // not contain that billing account.
        List<BillingAccount> accounts = new LinkedList<>(accountService.listAvailableAccountsForUser(authenticatedUser, Status.ACTIVE));

        // If the resource has been created, e.g., not null, then check to see if the billing account needs to be added in.
        if (resource != null) {

            accountService.updateTransientAccountInfo(resource);

            BillingAccount resourceAccount = resource.getAccount();
            boolean resourceAccountIsNotNull = resourceAccount != null;
            boolean resourceAccountNotInList = !accounts.contains(resourceAccount);
            boolean hasInheritedEditPermission = authorizationService.canEdit(authenticatedUser, resource);

            // If the billing account is not in the list, but should be, then move it to the front of the list.
            if (resourceAccountIsNotNull && resourceAccountNotInList &&
                    (authorizationService.isEditor(authenticatedUser) || hasInheritedEditPermission)) {
                accounts.add(0, resourceAccount);
            }
        }
        return accounts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.ResourceEditControllerService#updateSharesForEdit(org.tdar.core.bean.resource.Resource, org.tdar.core.bean.entity.TdarUser,
     * java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public void updateSharesForEdit(Resource resource, TdarUser authenticatedUser,
            List<ResourceCollection> retainedSharedCollections, List<ResourceCollection> retainedListCollections, List<ResourceCollection> shares,
            List<ResourceCollection> resourceCollections) {

        logger.debug("loadEffective...");
        for (ResourceCollection rc : resource.getManagedResourceCollections()) {
            authorizationService.applyTransientViewableFlag(rc, authenticatedUser);
            if (authorizationService.canRemoveFromCollection(authenticatedUser, rc)) {
                shares.add(rc);
            } else {
                retainedSharedCollections.add(rc);
                logger.debug("adding: {} to retained collections", rc);
            }
        }
        for (ResourceCollection rc : resource.getUnmanagedResourceCollections()) {
            if (authorizationService.canRemoveFromCollection(authenticatedUser, rc)) {
                resourceCollections.add(rc);
            } else {
                retainedListCollections.add(rc);
                logger.debug("adding: {} to retained collections", rc);
            }
        }
        logger.debug(" shares          : {}", shares);
        logger.debug(" shares retained : {}", retainedSharedCollections);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.ResourceEditControllerService#loadFilesJson(org.tdar.core.bean.resource.InformationResource)
     */
    @Override
    @Transactional(readOnly = true)
    public String loadFilesJson(InformationResource persistable) {
        if (PersistableUtils.isNullOrTransient(persistable)) {
            return null;
        }
        String filesJson = "[]";
        List<FileProxy> fileProxies = new ArrayList<>();
        // FIXME: this is the same logic as the initialization of the fileProxy... could use that instead, but causes a sesion issue
        for (InformationResourceFile informationResourceFile : persistable.getInformationResourceFiles()) {
            if (!informationResourceFile.isDeleted()) {
                fileProxies.add(new FileProxy(informationResourceFile));
            }
        }

        try {
            filesJson = serializationService.convertToJson(fileProxies);
            logger.debug(filesJson);
        } catch (IOException e) {
            logger.error("could not convert file list to json", e);

        }
        return filesJson;
    }

    @Override
    public <R extends Resource> Boolean isAbleToAdjustPermissions(TdarUser authenticatedUser, R persistable) {
        // Sort of a misnomer to check if a file can be uploaded, but this will check if the user has permisison to Edit a resource & modify records.
        return authorizationService.canModifyPermissions(authenticatedUser, persistable);
    }
}
