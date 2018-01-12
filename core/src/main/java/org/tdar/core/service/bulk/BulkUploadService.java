package org.tdar.core.service.bulk;

import java.util.Collection;
import java.util.List;

import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;

public interface BulkUploadService {

    /**
     * The Save method needs to endpoints, one with the @Async annotation to
     * allow Spring to run it asynchronously, and one without. Note, the @Async
     * annotation does not work in the Spring testing framework
     */
    void saveAsync(InformationResource image, Long submitterId, Long ticketId,
            Collection<FileProxy> fileProxies, Long accountId);

    /**
     * The Save method needs to endpoints, one with the @Async annotation to
     * allow Spring to run it asynchronously. This method:
     * (e) processes each file through the workflow
     * (f) reconcile account issues
     * (g) save records to XML
     * (h) reindex if needed
     */
    void save(InformationResource resourceTemplate_, Long submitterId, Long ticketId,
            Collection<FileProxy> fileProxies, Long accountId);

    /**
     * Take the ManifestProxy and iterate through attached FileProxies and make resources out of them
     * 
     */
    void processFileProxiesIntoResources(Collection<FileProxy> fileProxies, InformationResource image, TdarUser authenticatedUser,
            AsyncUpdateReceiver asyncUpdateReceiver, List<Resource> resources);

    /**
     * get the set of @link ResourceType enums that support BulkUpload
     * 
     * @return
     */
    ResourceType[] getResourceTypesSupportingBulkUpload();

    /**
     * Expose the AsyncStatus Reciever
     * 
     * @param ticketId
     * @return
     */
    BulkUpdateReceiver checkAsyncStatus(Long ticketId);

}