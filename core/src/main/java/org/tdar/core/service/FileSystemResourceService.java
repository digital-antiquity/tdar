package org.tdar.core.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.filestore.FilestoreObjectType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import freemarker.ext.dom.NodeModel;

/**
 * This Service provides support for getting resources off of the filesystem and
 * helps to abstract knowledge of the war file, and other placement of resources
 * on and off the classpath.
 * 
 * @author abrin
 * 
 */
@Service
public class FileSystemResourceService {

    @Autowired
    private FileSystemResourceDao fileSystemResourceDao;

    Logger logger = LoggerFactory.getLogger(FileSystemResourceDao.class);

    // helper to load the PDF Template for the cover page
    @Transactional(readOnly=true)
    public File loadTemplate(String path) throws IOException {
        return fileSystemResourceDao.loadTemplate(path);
    }

    @Transactional(readOnly=true)
    public Document openCreatorInfoLog(File filename) throws SAXException, IOException, ParserConfigurationException {
        return fileSystemResourceDao.openCreatorInfoLog(filename);
    }

    @Transactional(readOnly=true)
    public List<NodeModel> parseCreatorInfoLog(String prefix, boolean limit, float mean, int sidebarValuesToShow, Document dom) {
        return fileSystemResourceDao.parseCreatorInfoLog(prefix, limit, mean, sidebarValuesToShow, dom);
    }

    @Transactional(readOnly=true)
    public boolean checkHostedFileAvailable(String filename, FilestoreObjectType type, Long id) {
        return fileSystemResourceDao.checkHostedFileAvailable(filename, type, id);
    }

}
