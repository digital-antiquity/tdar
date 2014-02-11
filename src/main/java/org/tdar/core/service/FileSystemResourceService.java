package org.tdar.core.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.struts.action.TdarActionException;
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

    public boolean testWRO() {
        return fileSystemResourceDao.testWRO();
    }

    // helper to load the PDF Template for the cover page
    public File loadTemplate(String path) throws IOException {
        return fileSystemResourceDao.loadTemplate(path);
    }

    public List<String> parseWroXML(String prefix) throws TdarActionException {
        return fileSystemResourceDao.parseWroXML(prefix);
    }

    public Document openCreatorInfoLog(File filename) throws SAXException, IOException, ParserConfigurationException {
        return fileSystemResourceDao.openCreatorInfoLog(filename);
    }
    
    public List<NodeModel> parseCreatorInfoLog(String prefix, boolean limit, float mean, int sidebarValuesToShow, Document dom) throws TdarActionException {
        return fileSystemResourceDao.parseCreatorInfoLog(prefix, limit, mean, sidebarValuesToShow, dom);
    }

}
