package org.tdar.core.service;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.ImportFileStatus;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.dao.FileProcessingDao;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.exception.FileUploadException;
import org.tdar.filestore.personal.BagitPersonalFilestore;
import org.tdar.filestore.personal.PersonalFileType;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.filestore.personal.PersonalFilestoreFile;

/**
 * Manages adding and saving files in the @link PersonalFilestore
 * 
 * @author <a href='jim.devos@asu.edu'>Jim Devos</a>, <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Service
public class PersonalFilestoreServiceImpl implements PersonalFilestoreService {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private FileProcessingDao fileProcessingDao;

    // FIXME: double check that won't leak memory
    private Map<TdarUser, PersonalFilestore> personalFilestoreCache = new WeakHashMap<TdarUser, PersonalFilestore>();

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#createPersonalFilestoreTicket(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional
    public PersonalFilestoreTicket createPersonalFilestoreTicket(TdarUser person) {
        return createPersonalFilestoreTicket(person, PersonalFileType.UPLOAD);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#createPersonalFilestoreTicket(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.filestore.personal.PersonalFileType)
     */
    @Override
    @Transactional
    public PersonalFilestoreTicket createPersonalFilestoreTicket(TdarUser person, PersonalFileType fileType) {
        PersonalFilestoreTicket tfg = new PersonalFilestoreTicket();
        tfg.setSubmitter(person);
        tfg.setPersonalFileType(fileType);
        genericDao.save(tfg);

        // FIXME: it uses the ID as the ticket, but needs to check whether the ticket actually exists
        return tfg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#getPersonalFilestore(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public synchronized PersonalFilestore getPersonalFilestore(TdarUser submitter) {
        PersonalFilestore personalFilestore = personalFilestoreCache.get(submitter);
        if (personalFilestore == null) {
            personalFilestore = new BagitPersonalFilestore();
            personalFilestoreCache.put(submitter, personalFilestore);
        }
        return personalFilestore;
    }

    @Transactional(readOnly = false)
    @Override
    public void store(PersonalFilestoreTicket ticket, File file, String fileName, BillingAccount account, TdarUser user, TdarDir dir)
            throws FileUploadException {
        PersonalFilestore filestore = getPersonalFilestore(ticket);
        try {
            PersonalFilestoreFile store = filestore.store(ticket, file, fileName);
            TdarFile tdarFile = new TdarFile();
            tdarFile.setInternalName(store.getFile().getName());
            tdarFile.setLocalPath(store.getFile().getPath());
            tdarFile.setFilename(fileName);
            tdarFile.setExtension(FilenameUtils.getExtension(fileName));
            tdarFile.setSize(file.length());
            tdarFile.setDateCreated(new Date());
            if (account != null) {
                tdarFile.setAccount(account);
            }
            tdarFile.setUploader(user);
            if (dir != null) {
                tdarFile.setParent(dir);
            }
            tdarFile.setMd5(store.getMd5());
            tdarFile.setStatus(ImportFileStatus.UPLOADED);
            genericDao.saveOrUpdate(tdarFile);
        } catch (Exception e) {
            throw new FileUploadException("uploadController.could_not_store", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#findPersonalFilestoreTicket(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public PersonalFilestoreTicket findPersonalFilestoreTicket(Long ticketId) {
        return genericDao.find(PersonalFilestoreTicket.class, ticketId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#retrieveAllPersonalFilestoreFiles(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public List<PersonalFilestoreFile> retrieveAllPersonalFilestoreFiles(Long ticketId) {
        PersonalFilestoreTicket ticket = findPersonalFilestoreTicket(ticketId);
        if (ticket == null) {
            return Collections.emptyList();
        }
        return getPersonalFilestore(ticket.getSubmitter()).retrieveAll(ticket);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#getPersonalFilestore(org.tdar.core.bean.PersonalFilestoreTicket)
     */
    @Override
    public synchronized PersonalFilestore getPersonalFilestore(PersonalFilestoreTicket ticket) {
        return getPersonalFilestore(ticket.getSubmitter());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.PersonalFilestoreService#getPersonalFilestore(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public synchronized PersonalFilestore getPersonalFilestore(Long ticketId) {
        PersonalFilestoreTicket ticket = findPersonalFilestoreTicket(ticketId);
        return getPersonalFilestore(ticket);
    }

    @Override
    @Transactional(readOnly = false)
    public TdarDir createDirectory(TdarDir parent, String name, TdarUser authenticatedUser) {
        TdarDir dir = new TdarDir();
        dir.setFilename(name);
        dir.setInternalName(name);
        dir.setParent(parent);
        dir.setDateCreated(new Date());
        dir.setUploader(authenticatedUser);
        genericDao.saveOrUpdate(dir);
        return dir;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbstractFile> listFiles(TdarDir parent, BillingAccount account, TdarUser authenticatedUser) {
        return fileProcessingDao.listFilesFor(parent, account, authenticatedUser);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteFile(AbstractFile file, TdarUser authenticatedUser) {
        fileProcessingDao.delete(file);
    }

    @Override
    @Transactional(readOnly = false)
    public void moveFiles(List<AbstractFile> files, TdarDir dir, TdarUser authenticatedUser) {
        for (AbstractFile f : files) {
            f.setParent(dir);
            genericDao.saveOrUpdate(f);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public TdarDir findUnfileDir(TdarUser authenticatedUser) {
        return fileProcessingDao.findUnfiledDirByName(authenticatedUser);
        
    }

}
