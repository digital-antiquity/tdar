package org.tdar.core.service;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.FileProxy;
import org.tdar.utils.HashQueue;

public interface FileProxyService {

    String MISSING_FILE_PROXY_WARNING = "something bad happened in the JS side of things, there should always be a FileProxy resulting from the upload callback {}";

    /**
     * build a priority-queue of proxies that expect files.
     * 
     * @param proxies
     * @return
     */
    HashQueue<String, FileProxy> buildProxyQueue(List<FileProxy> proxies);

    /**
     * The @link FileProxy objects come from the @link AbstractInformationResourceController, while the @link PersonalFilestoreFile entries come from the @link
     * UploadController. This method tries to reconcile the two so that the file gets associated with the appropriate user-defined metadata. We use the two
     * sources so we can handle asynchronous uploads.
     * 
     * @param fileProxies
     * @param ticketId
     * @return
     */
    ArrayList<FileProxy> reconcilePersonalFilestoreFilesAndFileProxies(List<FileProxy> fileProxies, Long ticketId);

    /**
     * return a list of fileProxies, culling null and invalid instances
     * 
     * @param proxies
     */
    void cullInvalidProxies(List<FileProxy> proxies);

}