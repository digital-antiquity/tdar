package org.tdar.core.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.filestore.personal.PersonalFilestoreFile;
import org.tdar.utils.HashQueue;

/**
 * Service to help manage and handle the complexity of @link FileProxy objects
 * 
 * @author jtdevos
 * 
 */
@Component
public class FileProxyServiceImpl implements FileProxyService {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PersonalFilestoreService filestoreService;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.FileProxyService#buildProxyQueue(java.util.List)
     */
    @Override
    public HashQueue<String, FileProxy> buildProxyQueue(List<FileProxy> proxies) {
        HashQueue<String, FileProxy> hashQueue = new HashQueue<>();
        for (FileProxy proxy : proxies) {
            if (proxy == null) {
                continue;
            }
            if (proxy.getAction() == null) {
                logger.error("null proxy action on '{}'", proxy);
                proxy.setAction(FileAction.NONE);
            }
            if (proxy.getAction().shouldExpectFileHandle()) {
                hashQueue.push(proxy.getFilename(), proxy);
            }
        }
        return hashQueue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.FileProxyService#reconcilePersonalFilestoreFilesAndFileProxies(java.util.List, java.lang.Long)
     */
    @Override
    public ArrayList<FileProxy> reconcilePersonalFilestoreFilesAndFileProxies(List<FileProxy> fileProxies, Long ticketId) {
        cullInvalidProxies(fileProxies);
        List<PersonalFilestoreFile> pendingFiles = filestoreService.retrieveAllPersonalFilestoreFiles(ticketId);
        ArrayList<FileProxy> finalProxyList = new ArrayList<FileProxy>(fileProxies);

        // subset of proxy list, hashed into queues.
        HashQueue<String, FileProxy> proxiesNeedingFiles = buildProxyQueue(fileProxies);

        // FIXME: trying to handle duplicate filenames more gracefully by using hashqueue instead of hashmap, but this assumes that the sequence of pending
        // files is *similar* to sequence of incoming file proxies. probably a dodgy assumption, but arguably better than obliterating proxies w/ dupe filenames
        logger.info("pending: {} proxies: {}", pendingFiles, fileProxies);
        // associates InputStreams with all FileProxy objects that need to create a new version.
        for (PersonalFilestoreFile pendingFile : pendingFiles) {
            File file = pendingFile.getFile();
            FileProxy proxy = proxiesNeedingFiles.poll(file.getName());
            // if we encounter file that has no matching proxy, we create a new proxy and add it to the final list
            // we assume this happens when proxy fields in form were submitted in state that struts could not type-convert into a proxy instance
            if (proxy == null) {
                logger.warn(MISSING_FILE_PROXY_WARNING, file.getName());
            } else {
                proxy.setFile(file);
            }
        }
        Collections.sort(finalProxyList);
        return finalProxyList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.FileProxyService#cullInvalidProxies(java.util.List)
     */
    @Override
    public void cullInvalidProxies(List<FileProxy> proxies) {
        logger.debug("file proxies: {} ", proxies);
        ListIterator<FileProxy> iterator = proxies.listIterator();
        while (iterator.hasNext()) {
            FileProxy proxy = iterator.next();
            if (proxy == null) {
                logger.debug("fileProxy[{}] is null - culling", iterator.previousIndex());
                iterator.remove();
            } else if (StringUtils.isEmpty(proxy.getFilename())) {
                logger.debug("fileProxy[{}].fileName is blank - culling (value: {})", iterator.previousIndex(), proxy);
                iterator.remove();
            }
        }
    }

}
