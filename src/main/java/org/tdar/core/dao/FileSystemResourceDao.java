package org.tdar.core.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.ResourceType;

import com.hp.hpl.jena.util.FileUtils;

import freemarker.ext.dom.NodeModel;

@Component
public class FileSystemResourceDao {

    @Autowired
    ResourceLoader resourceLoader;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String TESTING_PATH_FOR_INCLUDES_DIRECTORY = "target/ROOT/";
    private XPathFactory xPathFactory = XPathFactory.newInstance();

    private String wroTempDirName;

    public static Boolean wroExists = null;

    public boolean testWRO() {
        if (wroExists != null) {
            return wroExists;
        }
        try {
            List<String> urls = fetchGroupUrls("default", ResourceType.JS);
            if (CollectionUtils.isNotEmpty(urls)) {
                logger.debug("WRO found? true");
                wroExists = true;
                return true;
            } else {
                logger.debug("wro does not exist");
                wroExists = false;
            }

        } catch (Exception e) {
            logger.error("{}", e);
        }
        logger.debug("WRO found? false");
        return false;
    }

    // helper to load the PDF Template for the cover page
    public File loadTemplate(String path) throws IOException, FileNotFoundException {
        Resource resource = resourceLoader.getResource(path);
        File template = null;
        if (resource.exists()) {
            template = resource.getFile();
        } else {
            resource = resourceLoader.getResource(".");
            String parent = resource.getFile().getParent();
            logger.debug("{} {}", resource, parent);
            // TO SUPPORT TESTING
            template = new File(TESTING_PATH_FOR_INCLUDES_DIRECTORY + path);
            logger.debug("{}", path);
            if (!template.exists()) {
                throw new FileNotFoundException(template.getAbsolutePath());
            }
        }
        return template;
    }

    public Document openCreatorInfoLog(File filename) throws SAXException, IOException, ParserConfigurationException {
        logger.info("opening {}", filename);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // use the factory to take an instance of the document builder
        DocumentBuilder db = dbf.newDocumentBuilder();
        // parse using the builder to get the DOM mapping of the XML file

        if (filename.exists()) {
            return db.parse(filename);
        }
        return null;
    }

    public List<NodeModel> parseCreatorInfoLog(String prefix, boolean limit, float mean, int sidebarValuesToShow, Document dom) {
        List<NodeModel> toReturn = new ArrayList<>();
        if (dom == null) {
            return toReturn;
        }
        try {
            // Create XPath object from XPathFactory
            XPath xpath = xPathFactory.newXPath();
            XPathExpression xPathExpr = xpath.compile(prefix);
            NodeList nodes = (NodeList) xPathExpr.evaluate(dom, XPathConstants.NODESET);
            logger.trace("xpath returned: {}", nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String name = node.getAttributes().getNamedItem("name").getTextContent();
                Float count = Float.parseFloat(node.getAttributes().getNamedItem("count").getTextContent());
                if (sidebarValuesToShow < toReturn.size()) {
                    return toReturn;
                }
                if (limit || count < mean) {
                    if (StringUtils.contains(name, GeographicKeyword.Level.COUNTRY.getLabel()) ||
                            StringUtils.contains(name, GeographicKeyword.Level.CONTINENT.getLabel()) ||
                            StringUtils.contains(name, GeographicKeyword.Level.FIPS_CODE.getLabel())) {
                        continue;
                    }
                }

                toReturn.add(NodeModel.wrap(nodes.item(i)));
            }
        } catch (Exception e) {
            throw new TdarRecoverableRuntimeException("browseController.parse_creator_log", e);
        }
        return toReturn;
    }

    public String getWroDir() {
        if (wroTempDirName != null) {
            return wroTempDirName;
        }
        try {
            String file = FileUtils.readWholeFileAsUTF8(getClass().getClassLoader().getResourceAsStream("version.txt"));
            file = StringUtils.replace(file, "+", "");
            wroTempDirName = "/wro/" + file.trim();
            return wroTempDirName;
        } catch (Exception e) {
            logger.error("{}", e);
        }
        return null;
    }

    /**
     * Create and setup a model insepector
     * 
     * @return
     * @throws URISyntaxException
     */
    public WroModelInspector getWroInspector() throws URISyntaxException {
        WroManager wroManager = getManagerFactory().create();
        WroModel wroModel = wroManager.getModelFactory().create();
        WroModelInspector wroModelInspector = new WroModelInspector(wroModel);
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
        List<String> srcList = new ArrayList();
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
