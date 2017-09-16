package org.tdar.web.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.TextProvider;

public interface ResourceSaveControllerService {

    FileProxy processTextInput(TextProvider provider, String fileTextInput, InformationResource persistable);

    String getLatestUploadedTextVersionText(InformationResource persistable);

    /**
     * Returns a List<FileProxy> representing the final set of fully initialized FileProxy objects
     * to be processed by the service layer.
     * 
     * FIXME: conditional logic could use some additional refactoring.
     * 
     * @return a List<FileProxy> representing the final set of fully initialized FileProxy objects
     */
    List<FileProxy> getFileProxiesToProcess(AuthWrapper<InformationResource> auth, TextProvider provider, FileSaveWrapper fsw,
            FileProxy textInputFileProxy);

    /**
     * One-size-fits-all method for handling uploaded InformationResource files.
     * 
     * Handles text input files for coding sheets and ontologies,
     * async uploads, and single-file dataset uploads.
     * 
     * @return
     * 
     * @throws TdarActionException
     * @throws IOException
     */
    ErrorTransferObject handleUploadedFiles(AuthWrapper<Resource> auth, TextProvider provider, Collection<String> validFileNames, Long ticketId,
            List<FileProxy> proxies) throws TdarActionException, IOException;

    <T extends Sequenceable<T>> void prepSequence(List<T> list);

    <R extends Resource> ErrorTransferObject save(AuthWrapper<Resource> authWrapper, ResourceControllerProxy<R> rcp)
            throws TdarActionException, IOException;

    /**
     *     public void loadEffectiveResourceCollectionsForSave() {
        getLogger().debug("loadEffective...");
        for (SharedCollection rc : getResource().getSharedCollections()) {
            if (!authorizationService.canRemoveFromCollection(rc, getAuthenticatedUser())) {
                getRetainedSharedCollections().add(rc);
                getLogger().debug("adding: {} to retained collections", rc);
            }
        }
    }
    
     * @param auth
     * @param retainedSharedCollections
     * @param retainedListCollections
     */
    void loadEffectiveResourceCollectionsForSave(AuthWrapper<Resource> auth, List<SharedCollection> retainedSharedCollections,
            List<SharedCollection> retainedListCollections);

    void saveResourceProviderInformation(InformationResource resource, String resourceProviderInstitutionName,
            ResourceCreatorProxy copyrightHolderProxies, String publisherName);

    <R extends InformationResource> void setupFileProxiesForSave(ResourceControllerProxy<R> proxy, AuthWrapper<InformationResource> auth,
            FileSaveWrapper fsw, TextProvider provider);

}