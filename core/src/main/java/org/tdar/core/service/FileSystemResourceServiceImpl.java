package org.tdar.core.service;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.filestore.FilestoreObjectType;

/**
 * This Service provides support for getting resources off of the filesystem and
 * helps to abstract knowledge of the war file, and other placement of resources
 * on and off the classpath.
 * 
 * @author abrin
 * 
 */
@Service
public class FileSystemResourceServiceImpl implements FileSystemResourceService  {

    @Autowired
    private FileSystemResourceDao fileSystemResourceDao;

    Logger logger = LoggerFactory.getLogger(FileSystemResourceDao.class);

    // helper to load the PDF Template for the cover page
    /* (non-Javadoc)
     * @see org.tdar.core.service.FileSystemResourceService#loadTemplate(java.lang.String)
     */
    @Override
    @Transactional(readOnly=true)
    public File loadTemplate(String path) throws IOException {
        return fileSystemResourceDao.loadTemplate(path);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.FileSystemResourceService#checkHostedFileAvailable(java.lang.String, org.tdar.filestore.FilestoreObjectType, java.lang.Long)
     */
    @Override
    @Transactional(readOnly=true)
    public boolean checkHostedFileAvailable(String filename, FilestoreObjectType type, Long id) {
        return fileSystemResourceDao.checkHostedFileAvailable(filename, type, id);
    }

}
