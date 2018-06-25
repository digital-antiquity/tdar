package org.tdar.struts.action.resource;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts_base.action.TdarActionException;

import edu.asu.lib.dc.DublinCoreDocument;
import edu.asu.lib.jaxb.JaxbDocumentWriter;
import edu.asu.lib.mods.ModsDocument;

public class DocumentCitationITCase extends AbstractControllerITCase {

    private final transient Logger log = LoggerFactory.getLogger(getClass());

    private static final String DOC_TITLE = "Archeological Survey and Architectural Study of Montezuma Castle National Monument";
    private static final String DOC_DESCRIPTION_FRAGMENT = "survey of Montezuma Castle National Monument was conducted";
    private static final String DOC_AUTHOR_FIRSTNAME = "Susan J.";
    private static final String DOC_AUTHOR_LASTNAME = "Wells";
    private static final String DOC_PUBLISHER = "Western Archeological and Conservation Center";
    private static final String DOC_PUBLISHER_LOCATION = "Tucson, Arizona";

    private static final Long DOC_TDAR_ID = 4287L;

    private JAXBMetadataViewController controller;

    private void navigateTo(Long tdarId) throws Exception {
        controller.setId(tdarId);
        controller.prepare();
    }

    private String getModsXml() throws TdarActionException {
        String xml = null;
        controller.viewMods();
        ModsDocument mods = controller.getModsDocument();
        Writer writer = new StringWriter();
        try {
            JaxbDocumentWriter.write(mods, writer, true);
        } catch (JAXBException jex) {
            log.error("failed to write jax document");
        }
        xml = writer.toString();
        return xml;
    }

    private String getDcXml() throws TdarActionException {
        String xml = null;
        controller.viewDc();
        DublinCoreDocument dc = controller.getDcDocument();
        Writer writer = new StringWriter();
        try {
            JaxbDocumentWriter.write(dc, writer, true);
        } catch (JAXBException jex) {
            log.error("failed to write jax document");
        }
        xml = writer.toString();
        return xml;
    }

    @Before
    public void setup() {
        // setup prefexes to the mods/dc namespaces
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("m", "http://www.loc.gov/mods/v3");
        m.put("dcd", "http://purl.org/dc/elements/1.1/");
        m.put("ns2", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);

        controller = generateNewInitializedController(JAXBMetadataViewController.class);
    }

    @Test
    public void simpleModsTest() throws Exception {
        // a simple test to see if we even get back a non-blank mods document
        navigateTo(DOC_TDAR_ID);
        String xml = getModsXml();
        assertTrue(xml.length() > 0);
    }

    @Test
    public void simpleDcTest() throws Exception {
        // a simple test to see if we even get back a non-blank dc document
        navigateTo(DOC_TDAR_ID);
        String xml = getDcXml();
        logger.debug(xml);
        assertTrue(xml.length() > 0);
    }

    @Test
    public void testCommonCitationFieldsMods() throws Exception {
        // make sure the basics are present: title, author, and description
        navigateTo(DOC_TDAR_ID);
        String xml = getModsXml();
        log.debug(xml);
        assertXpathEvaluatesTo(DOC_TITLE, "//m:title[1]", xml);
        assertXpathEvaluatesTo(DOC_AUTHOR_FIRSTNAME, "/m:mods/m:name[1]/m:namePart[@type='given']", xml);
        assertXpathEvaluatesTo(DOC_AUTHOR_LASTNAME, "/m:mods/m:name[1]/m:namePart[@type='family']", xml);
        // really we're only checking a portion of the description
        org.w3c.dom.Document xmlDocument = XMLUnit.buildTestDocument(xml);
        XpathEngine xpathEngine = XMLUnit.newXpathEngine();
        String result = xpathEngine.evaluate("//m:abstract", xmlDocument);
        assertTrue("abstract is at least partially present", result.indexOf(DOC_DESCRIPTION_FRAGMENT) > -1);
    }

    @Test
    public void testModsForThesis() throws Exception {
        Document doc = genericService.find(Document.class, DOC_TDAR_ID);
        doc.setDocumentType(DocumentType.THESIS);
        navigateTo(DOC_TDAR_ID);
        String xml = getModsXml();
        log.debug("thesis test");
        log.debug(xml);
        assertXpathEvaluatesTo(DOC_PUBLISHER, "/m:mods/m:relatedItem/m:name[@type='corporate']/m:namePart[1]", xml);
        assertXpathEvaluatesTo(DOC_PUBLISHER_LOCATION, "/m:mods/m:relatedItem/m:name[@type='corporate']/m:namePart[2]", xml);
        assertXpathEvaluatesTo("Degree grantor", "/m:mods/m:relatedItem/m:name[@type='corporate']/m:role/m:roleTerm", xml);
    }

    @Test
    public void testModsForConference() throws Exception {
        // besides the common fields, a conference citation should include
        // conference name and place where conference occured
        Document doc = genericService.find(Document.class, DOC_TDAR_ID);
        doc.setDocumentType(DocumentType.CONFERENCE_PRESENTATION);
        navigateTo(DOC_TDAR_ID);
        String xml = getModsXml();
        log.debug("conference test");
        log.debug(xml);
        assertXpathEvaluatesTo(DOC_PUBLISHER, "m:mods/m:name[@type='conference']/m:namePart", xml);
        assertXpathEvaluatesTo("creator", "m:mods/m:name[@type='conference']/m:role/m:roleTerm", xml);
        assertXpathEvaluatesTo(DOC_PUBLISHER_LOCATION, "m:mods/m:originInfo/m:place/m:placeTerm[1]", xml);
    }

    @Test
    public void testCommonCitationFieldsDc() throws Exception {
        navigateTo(DOC_TDAR_ID);
        String xml = getDcXml();
        assertXpathEvaluatesTo(DOC_TITLE, "/ns2:dc/dcd:title[1]", xml);
    }

}
