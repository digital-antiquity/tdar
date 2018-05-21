package org.tdar.core.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.dao.base.GenericDao;
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
    @Autowired
    private GenericDao genericDao;

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
                hashQueue.push(proxy.getName(), proxy);
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
    public ArrayList<FileProxy> reconcilePersonalFilestoreFilesAndFileProxies(InformationResource ir, Long accountId, List<FileProxy> fileProxies_, Long ticketId) {
        
        BillingAccount account = genericDao.find(BillingAccount.class, accountId);
        
        cullInvalidProxies(fileProxies_);
        List<PersonalFilestoreFile> pendingFiles = filestoreService.retrieveAllPersonalFilestoreFiles(ticketId);
        ArrayList<FileProxy> finalProxyList = new ArrayList<FileProxy>(fileProxies_);
        ArrayList<FileProxy> fileProxies = new ArrayList<FileProxy>(fileProxies_);
        cleanupFiles(fileProxies, ir, account);

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

    /**
     * 1. setup and reconcile files
     * 2. group files together around one master file (useful for GIS files, or groups of files on a resource
     * 
     * @param fileProxies
     * @param ir
     * @param account
     */
    private void cleanupFiles(ArrayList<FileProxy> fileProxies, InformationResource ir, BillingAccount account) {
        Iterator<FileProxy> iterator = fileProxies.iterator();
        
        Set<TdarFile> primary =  new HashSet<>();
        List<TdarFile> all =  new ArrayList<>();
        // if we have a tdarFile, remove it from the proxy list we're going to reconcile below
        while (iterator.hasNext()) {
            FileProxy proxy = iterator.next();
            if (proxy.getTdarFileId() != null) {
                TdarFile file = genericDao.find(TdarFile.class, proxy.getTdarFileId());
                if (file != null) {
                    proxy.setFile(new File(file.getLocalPath()));
                    file.setResource(ir);
                    file.setAccount(account);
                    iterator.remove();
                    if (CollectionUtils.isNotEmpty(file.getParts())) {
                        primary.add(file);
                    }
                    all.add(file);
                }
            }
        }

        if (all.isEmpty() && primary.isEmpty()) {
            return;
        }
        
        TdarFile master = null;
        if (primary.isEmpty() && !all.isEmpty()) {
            logger.debug("{} ; {}", all, all.get(0)); 
            if (StringUtils.equals(all.get(0).getExtension(),"xml")) {
                //FIXME: as we improve our file grouping logic for GIS and other types, we will need to improve this logic
                // so that the primary is the right file
            }
            master = all.remove(0);
        }
        if (!primary.isEmpty()) {
            master = primary.iterator().next();
        }
        logger.debug("all: {}", all);
        logger.debug("master: {}", master);
        
        for (TdarFile file : all)  {
            if (file != master) {
                master.getParts().add(file);
                master.getParts().addAll(file.getParts());
                file.getParts().clear();
            }
        }
        logger.debug("parts: {}", master.getParts());
        genericDao.saveOrUpdate(all);
        genericDao.saveOrUpdate(master);
        
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
            } else if (StringUtils.isEmpty(proxy.getName())) {
                logger.debug("fileProxy[{}].fileName is blank - culling (value: {})", iterator.previousIndex(), proxy);
                iterator.remove();
            }
        }
    }

}
