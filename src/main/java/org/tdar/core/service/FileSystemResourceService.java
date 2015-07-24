package org.tdar.core.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.filestore.Filestore.ObjectType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.resource.ResourceType;
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

    public boolean testWRO() {
        return fileSystemResourceDao.testWRO();
    }

    // helper to load the PDF Template for the cover page
    public File loadTemplate(String path) throws IOException {
        return fileSystemResourceDao.loadTemplate(path);
    }

    public Document openCreatorInfoLog(File filename) throws SAXException, IOException, ParserConfigurationException {
        return fileSystemResourceDao.openCreatorInfoLog(filename);
    }

    public List<NodeModel> parseCreatorInfoLog(String prefix, boolean limit, float mean, int sidebarValuesToShow, Document dom) {
        return fileSystemResourceDao.parseCreatorInfoLog(prefix, limit, mean, sidebarValuesToShow, dom);
    }

    public String getWroDir() {
        return fileSystemResourceDao.getWroDir();
    }

    /**
     * Return list of urls associated with the specified group (included urls inherited from any parent groups).
     * 
     * @param groupName
     * @return
     * @throws URISyntaxException 
     */
    public List<String> fetchGroupUrls(String groupName) throws URISyntaxException {
        return fetchGroupUrls(groupName, null);
    }

    /**
     * Return list of urls associated with the specified group (included urls inherited from any parent groups).
     * 
     * @param groupName
     * @param resourceTypefilter
     *            only include items of the specified type (CSS or JS)
     * @return
     * @throws URISyntaxException 
     */
    public List<String> fetchGroupUrls(String groupName, ResourceType type)  {
        // borrowed heavily from https://code.google.com/p/wro4j/source/browse/wro4j-runner/src/main/java/ro/isdc/wro/runner/Wro4jCommandLineRunner.java
        return fileSystemResourceDao.fetchGroupUrls(groupName, type);
    }

    /**
     * Return list of WRO group names as specified by wro.xml
     * 
     * @return
     * @throws URISyntaxException 
     */
    public List<String> fetchGroupNames() throws URISyntaxException {
        WroModelInspector wroModelInspector = fileSystemResourceDao.getWroInspector();
        List<String> groupNames = wroModelInspector.getGroupNames();
        return groupNames;
    }

    public boolean checkHostedFileAvailable(String filename, ObjectType type, Long id) {
        return fileSystemResourceDao.checkHostedFileAvailable(filename, type, id);
    }

}
