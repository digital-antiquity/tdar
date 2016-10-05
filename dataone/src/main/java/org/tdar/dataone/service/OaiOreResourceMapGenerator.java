package org.tdar.dataone.service;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dataone.ore.ResourceMapFactory;
import org.dataone.service.types.v1.Identifier;
import org.dspace.foresite.OREException;
import org.dspace.foresite.ORESerialiserException;
import org.dspace.foresite.ResourceMap;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.dataone.bean.EntryType;

/**
 * Generates an OAI-ORE ResourceMap based on an InformationResource
 * 
 * @author abrin
 *
 */
public class OaiOreResourceMapGenerator implements Serializable {

    private static final long serialVersionUID = 2404227515880634632L;
    private InformationResource ir;
    private boolean includeFiles;

    public OaiOreResourceMapGenerator(InformationResource ir2, boolean b) {
        this.ir = ir2;
        this.includeFiles = b;
    }

    public String generate() throws OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException {
        // Create an identifier for the ResourceMap
        Identifier id = new Identifier();
        String formattedId = IdentifierParser.webSafeDoi(ir.getExternalId());
        id.setValue(IdentifierParser.formatIdentifier(formattedId, ir.getDateUpdated(), EntryType.D1, null));

        // Create an Identifier for the metadata
        Identifier packageId = new Identifier();
        packageId.setValue(formattedId + DataOneService.D1_SEP + ir.getDateUpdated().toString());

        // create an identifier for each file
        List<Identifier> dataIds = new ArrayList<>();
        if (includeFiles) {
            for (InformationResourceFile irf : ir.getActiveInformationResourceFiles()) {
                Identifier fileId = new Identifier();
                fileId.setValue(IdentifierParser.formatIdentifier(formattedId, ir.getDateUpdated(), EntryType.FILE, irf));
                dataIds.add(fileId);
            }
        }
        Map<Identifier, List<Identifier>> idMap = new HashMap<Identifier, List<Identifier>>();
        idMap.put(packageId, dataIds);

        // generate the resource map
        ResourceMapFactory rmf = ResourceMapFactory.getInstance();
        ResourceMap resourceMap = rmf.createResourceMap(id, idMap);
        String rdfXml = convertToXml(resourceMap);
        if (!DataOneConfiguration.getInstance().isProduction()) {
            rdfXml = rdfXml.replace("cn.dataone.org", "cn-sandbox.test.dataone.org");
        }
        return rdfXml;
    }

    /**
     * Convert the ResourceMap to XML based on the Dryad method
     * 
     * @param resourceMap
     * @return
     * @throws ORESerialiserException
     * @throws JDOMException
     * @throws IOException
     */
    private String convertToXml(ResourceMap resourceMap) throws ORESerialiserException, JDOMException, IOException {
        Date itemModDate = ir.getDateUpdated();
        resourceMap.setModified(itemModDate);
        // convert to XML and then cleanup, this is borrowed from Dryad Code.
        String rdfXml = ResourceMapFactory.getInstance().serializeResourceMap(resourceMap);
        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(new StringReader(rdfXml));
        Iterator<Element> it = d.getRootElement().getChildren().iterator();
        List<Element> children = new ArrayList<Element>();
        while (it.hasNext()) {
            Element element = (Element) it.next();
            children.add(element);
        }
        d.getRootElement().removeContent();
        Collections.sort(children, new Comparator<Element>() {
            @Override
            public int compare(Element t, Element t1) {
                return t.getAttributes().toString().compareTo(t1.getAttributes().toString());
            }
        });
        for (Element el : children) {
            d.getRootElement().addContent(el);
        }
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        rdfXml = outputter.outputString(d);
        return rdfXml;
    }
}
