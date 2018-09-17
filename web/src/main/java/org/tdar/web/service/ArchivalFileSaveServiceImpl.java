package org.tdar.web.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;


@Service
@Transactional
public class ArchivalFileSaveServiceImpl implements ArchivalFileSaveService {

    private Filestore filestore = TdarConfiguration.getInstance().getFilestore();
    private GenericDao genericDao;
    
    @Autowired
    public ArchivalFileSaveServiceImpl(GenericDao genericDao) {
        this.genericDao = genericDao;
    }
    
    @Transactional(readOnly = false)
    public void saveArchivalVersion(Resource resource, InformationResourceFile informationResourceFile, File file, String filename, TdarUser authenticatedUser) throws IOException, FileUploadException {
        if (informationResourceFile.getLatestArchival() != null) {
            throw new FileUploadException("archival file already exists");
        }
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.ARCHIVAL, filename, informationResourceFile);
        filestore.store(FilestoreObjectType.RESOURCE, file, version);
        genericDao.save(version);
        String msg = String.format("%s add an archival version (%s) of file id (%s)", authenticatedUser, filename, informationResourceFile.getId());
        ResourceRevisionLog revision = new ResourceRevisionLog(msg, resource, authenticatedUser, RevisionLogType.EDIT);
        genericDao.saveOrUpdate(revision);

    }

}
