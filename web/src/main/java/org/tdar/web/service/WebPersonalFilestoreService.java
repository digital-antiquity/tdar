package org.tdar.web.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.TdarFile;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.exception.FileUploadException;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.filestore.personal.PersonalFilestoreFile;

import com.opensymphony.xwork2.TextProvider;

@Service
public class WebPersonalFilestoreService {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private PersonalFilestoreService filestoreService;
    @Autowired
    private GenericDao genericDao;

    @Transactional(readOnly=false)
    public PersonalFilestoreTicket grabTicket(TdarUser authenticatedUser) {
        return filestoreService.createPersonalFilestoreTicket(authenticatedUser);
    }

    @Transactional(readOnly=false)
    public List<String> store(TdarUser submitter, List<File> uploadFile, List<String> uploadFileFileName, List<String> uploadFileContentType, PersonalFilestoreTicket ticket, TextProvider provider, BillingAccount account, TdarFile dir) throws FileUploadException {
        List<String> hashCodes = new ArrayList<>();
        for (int i = 0; i < uploadFile.size(); i++) {
            File file = uploadFile.get(i);
            String fileName = uploadFileFileName.get(i);
            // put upload in holding area to be retrieved later (maybe) by the informationResourceController
            if ((file != null) && file.exists()) {
                String contentType = "";
                try {
                    contentType = uploadFileContentType.get(i);
                } catch (Exception e) { /* OK, JUST USED FOR DEBUG */
                }
                Object[] out = { fileName, file.length(), contentType, ticket.getId() };
                logger.debug("UPLOAD CONTROLLER: processing file: {} ({}) , contentType: {} , tkt: {}", out);
                PersonalFilestore filestore = filestoreService.getPersonalFilestore(submitter);
                try {
                    PersonalFilestoreFile store = filestore.store(ticket, file, fileName);
                    TdarFile tdarFile = new TdarFile();
                    tdarFile.setFilename(fileName);
                    tdarFile.setDisplayName(fileName);
                    tdarFile.setExtension(FilenameUtils.getExtension(fileName));
                    tdarFile.setFileSize(file.length());
                    if (dir != null) {
                        tdarFile.setParentFile(dir);
                    }
                    tdarFile.setMd5(store.getMd5());
                    genericDao.saveOrUpdate(tdarFile);
                    hashCodes.add(store.getMd5());
                } catch (Exception e) {
                    throw new FileUploadException("uploadController.could_not_store", e);
                }
            }
        }
        return hashCodes;
    }
}
