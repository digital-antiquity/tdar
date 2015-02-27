package org.tdar.core.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.dao.FileSystemResourceDao;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
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

    public List<String> fetchGroupUrls(String groupName) {
        //borrowed heavily from https://code.google.com/p/wro4j/source/browse/wro4j-runner/src/main/java/ro/isdc/wro/runner/Wro4jCommandLineRunner.java
        Context.set(Context.standaloneContext());
        WroManager wroManager = getManagerFactory().create();
        WroModel wroModel = wroManager.getModelFactory().create();
        WroModelInspector wroModelInspector = new WroModelInspector(wroModel);
        List<String> groupNames = wroModelInspector.getGroupNames();
        logger.debug("names : {}", groupNames);
        Group group = wroModelInspector.getGroupByName(groupName);
        List<Resource> resources = group.getResources();
        List<String> srcList = new ArrayList<>();
        for (Resource resource : resources) {
            srcList.add(resource.getUri());
        }
        logger.debug("sourceList: {}", srcList);
        return srcList;
    }

    private StandaloneContext createStandaloneContext() {
        final StandaloneContext runContext = new StandaloneContext();
        runContext.setContextFoldersAsCSV("src/main/webapp/");
        runContext.setWroFile(new File("src/main/resources/wro.xml"));
        return runContext;
    }

    private DefaultStandaloneContextAwareManagerFactory getManagerFactory() {
        DefaultStandaloneContextAwareManagerFactory managerFactory = new DefaultStandaloneContextAwareManagerFactory();
        managerFactory.setProcessorsFactory(createProcessorsFactory());
        managerFactory.initialize(createStandaloneContext());
        DefaultStandaloneContextAwareManagerFactory dcsaf = new DefaultStandaloneContextAwareManagerFactory();
        dcsaf.initialize(createStandaloneContext());

        return dcsaf;
    }

    private ProcessorsFactory createProcessorsFactory() {
        final Properties props = new Properties();
        return new ConfigurableProcessorsFactory() {
            protected Map<String, ResourcePreProcessor> newPreProcessorsMap() {
                final Map<String, ResourcePreProcessor> map = super.newPreProcessorsMap();
                return map;
            }

            protected Map<String, ResourcePostProcessor> newPostProcessorsMap() {
                final Map<String, ResourcePostProcessor> map = super.newPostProcessorsMap();
                return map;
            }
        }.setProperties(props);
    }

    /**
     * Return list of urls associated with the specified group (included urls inherited from any parent groups).
     * 
     * @param groupName
     * @param resourceTypefilter
     *            only include items of the specified type (CSS or JS)
     * @return
     */
    public List<String> fetchGroupUrls(String groupName, ResourceType resourceTypefilter) {
        // todo: implement me
        return null;
    }

    /**
     * Return list of urls associated with the specified group (included urls inherited from any parent groups).
     * 
     * @param groupName
     * @return
     */
    // public List<String> fetchGroupUrls(String groupName) {
    // return null;
    // // return fetchGroupUrls(groupName, null);
    // }

    /**
     * Return list of WRO group names as specified by wro.xml
     * 
     * @return
     */
    public List<String> fetchGroupNames() {
        // todo: implement me
        return null;
    }

}
