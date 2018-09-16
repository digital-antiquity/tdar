package org.tdar.web.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;


@Service
@Transactional
public class ArchivalFileSaveServiceImpl implements ArchivalFileSaveService {

    private Filestore filestore = TdarConfiguration.getInstance().getFilestore();
    
    @Transactional(readOnly = false)
    public void saveArchivalVersion(Resource resource, InformationResourceFile informationResourceFile, File file, String filename) throws IOException, FileUploadException {
        if (informationResourceFile.getLatestArchival() != null) {
            throw new FileUploadException("archival file already exists");
        }
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.ARCHIVAL, filename, informationResourceFile);
        filestore.store(FilestoreObjectType.RESOURCE, file, version);
    }

}
