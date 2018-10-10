package org.tdar.web;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.tdar.core.configuration.TdarConfiguration;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * This Service provides support for getting resources off of the filesystem and
 * helps to abstract knowledge of the war file, and other placement of resources
 * on and off the classpath.
 * 
 * @author abrin
 * 
 */
@Service
public class WebFileSystemResourceService {

    Logger logger = LoggerFactory.getLogger(WebFileSystemResourceService.class);

    private String wroTempDirName;
    public static Boolean wroExists = null;

    @Autowired
    ResourceLoader resourceLoader;

    private WroModelInspector wroModelInspector;

    public boolean testWRO() {
        if (wroExists != null) {
            return wroExists;
        }
        try {
            String wroFile = getWroDir() + "/default.js";
            logger.debug("wroFile: {}", wroFile);
            Resource resource = resourceLoader.getResource(wroFile);
            wroExists = resource.exists();

            if (wroExists) {
                logger.debug("WRO found? true");
            }
            return wroExists;

        } catch (Exception e) {
            logger.error("{}", e);
        }
        logger.debug("WRO found? false");
        return false;
    }

    public String getWroDir() {
        if (wroTempDirName != null) {
            return wroTempDirName;
        }
        try {
            Properties props = TdarConfiguration.getInstance().loadChangesetProps();
            String changeset = props.getProperty("git.commit.id.abbrev");
            wroTempDirName = "/wro/" + changeset;
            return wroTempDirName;
        } catch (Exception e) {
            logger.error("{}", e);
        }
        return null;
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
     * Return list of WRO group names as specified by wro.xml
     * 
     * @return
     * @throws URISyntaxException
     */
    public List<String> fetchGroupNames() throws URISyntaxException {
        WroModelInspector wroModelInspector = getWroInspector();
        List<String> groupNames = wroModelInspector.getGroupNames();
        return groupNames;
    }

    /**
     * Create and setup a model insepector
     * 
     * @return
     * @throws URISyntaxException
     */
    public WroModelInspector getWroInspector() throws URISyntaxException {
        if (wroModelInspector == null) {
            WroManager wroManager = getManagerFactory().create();
            WroModel wroModel = wroManager.getModelFactory().create();
            wroModelInspector = new WroModelInspector(wroModel);
        }
        return wroModelInspector;
    }

    /**
     * Setup the WRO Context
     * 
     * @return
     * @throws URISyntaxException
     */
    private StandaloneContext createStandaloneContext() throws URISyntaxException {
        Context.set(Context.standaloneContext());
        final StandaloneContext runContext = new StandaloneContext();
        runContext.setWroFile(new File(getClass().getClassLoader().getResource("wro.xml").toURI()));
        return runContext;
    }

    /**
     * Create the WRO Manager Factory
     * 
     * @return
     * @throws URISyntaxException
     */
    private DefaultStandaloneContextAwareManagerFactory getManagerFactory() throws URISyntaxException {
        DefaultStandaloneContextAwareManagerFactory managerFactory = new DefaultStandaloneContextAwareManagerFactory();
        managerFactory.initialize(createStandaloneContext());
        DefaultStandaloneContextAwareManagerFactory dcsaf = new DefaultStandaloneContextAwareManagerFactory();
        dcsaf.initialize(createStandaloneContext());
        return dcsaf;
    }

    public List<String> fetchGroupUrls(String groupName, ResourceType type) {
        List<String> srcList = new ArrayList<>();
        try {
            WroModelInspector wroModelInspector = getWroInspector();

            Group group = wroModelInspector.getGroupByName(groupName);
            for (ro.isdc.wro.model.resource.Resource resource : group.getResources()) {
                if (type == null || type == resource.getType()) {
                    srcList.add(resource.getUri());
                }
            }
        } catch (URISyntaxException uriEx) {
            logger.error("could not find wro.xml", uriEx);
        }
        return srcList;
    }
}
