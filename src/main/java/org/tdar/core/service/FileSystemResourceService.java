package org.tdar.core.service;

import freemarker.ext.dom.NodeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.dao.FileSystemResourceDao;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ro.isdc.wro.model.resource.ResourceType;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;


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

    public List<String> parseWroXML(String prefix) {
        return fileSystemResourceDao.parseWroXML(prefix);
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

//    public List<String> fetchGroupUrls(String groupName) {
//        WroConfiguration config = new WroConfiguration();
//        Context.set(Context.standaloneContext(), config);
//        GroupExtractor ge = new DefaultGroupExtractor();
//        WroManager wroManager = new WroManager.Builder().build();
//        WroModel wroModel = wroManager.getModelFactory().create();
//        WroModelInspector wroModelInspector = new WroModelInspector(wroModel);
//        List<String> groupNames = wroModelInspector.getGroupNames();
//
//        Group group = wroModelInspector.getGroupByName(groupName);
//        List<Resource> resources = group.getResources();
//        List<String> srcList = new ArrayList<>();
//        for(Resource resource : resources) {
//            srcList.add(resource.getUri());
//        }
//        return srcList;
//    }

    /**
     * Return list of urls associated with the specified group (included urls inherited from any parent groups).
     * @param groupName
     * @param resourceTypefilter only include items of the specified type (CSS or JS)
     * @return
     */
    public List<String> fetchGroupUrls(String groupName, ResourceType resourceTypefilter) {
        //todo: implement me
        return null;
    }

    /**
     * Return list of urls associated with the specified group (included urls inherited from any parent groups).
     * @param groupName
     * @return
     */
    public List<String> fetchGroupUrls(String groupName) {
        return fetchGroupUrls(groupName, null);
    }

    /**
     * Return list of WRO group names as specified by wro.xml
     * @return
     */
    public List<String> fetchGroupNames() {
        //todo: implement me
        return null;
    }

}
