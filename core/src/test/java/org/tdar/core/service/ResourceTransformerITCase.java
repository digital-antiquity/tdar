package org.tdar.core.service;

import static org.junit.Assert.*;

import java.io.StringWriter;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.transform.EMLDocumentTransformer;
import org.tdar.transform.ExtendedDcTransformer;
import org.tdar.transform.ModsTransformer;

import edu.asu.lib.eml.EMLDocument;
import edu.asu.lib.jaxb.JaxbDocumentWriter;
import edu.asu.lib.mods.ModsDocument;
import edu.asu.lib.qdc.QualifiedDublinCoreDocument;

public class ResourceTransformerITCase extends AbstractIntegrationTestCase {

    @Test
    public void serializeEML() throws JAXBException {
        Document resource = genericService.find(Document.class, Long.parseLong(TestConstants.TEST_DOCUMENT_ID));
        EMLDocument doc = EMLDocumentTransformer.transformAny(resource);
        StringWriter writer = new StringWriter();
        JaxbDocumentWriter.write(doc, writer, true);
        logger.debug(writer.toString());

    }
    
    @Test
    public void transformDC() throws JAXBException {
        Document d = new Document();
        d.getInvestigationTypes().add(new InvestigationType("bacd"));
        d.getSiteNameKeywords().add(new SiteNameKeyword("siteName"));
        d.markUpdated(getAdminUser());
        d.setDate(2001);
        d.getSiteTypeKeywords().add(new SiteTypeKeyword("SiteType"));
        d.getResourceCreators().add(new ResourceCreator(getBasicUser(), ResourceCreatorRole.AUTHOR));
        d.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.CONTRIBUTOR));
        d.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.CONTRIBUTOR));
        QualifiedDublinCoreDocument transformAny = ExtendedDcTransformer.transformAny(d);
        StringWriter writer = new StringWriter();
        JaxbDocumentWriter.write(transformAny, writer, true);
        String str = writer.toString();
        logger.debug(str);
        assertTrue("see investigation type", StringUtils.contains(str, "bacd"));
        assertTrue("see site name", StringUtils.contains(str, "siteName"));
        assertTrue("see site type", StringUtils.contains(str, "SiteType"));
        assertEquals("see only one contrib", 1, StringUtils.countMatches(str, getAdminUser().getName()));
    }


    
    @Test
    public void transformMods() throws JAXBException {
        Document d = new Document();
        d.getInvestigationTypes().add(new InvestigationType("bacd"));
        d.getSiteNameKeywords().add(new SiteNameKeyword("siteName"));
        d.setTitle("test");
        d.markUpdated(getAdminUser());
        d.setDate(2001);
        d.getSiteTypeKeywords().add(new SiteTypeKeyword("SiteType"));
        d.getResourceCreators().add(new ResourceCreator(getBasicUser(), ResourceCreatorRole.AUTHOR));
        d.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.CONTRIBUTOR));
        ModsDocument transformAny = ModsTransformer.transformAny(d);
        StringWriter writer = new StringWriter();
        JaxbDocumentWriter.write(transformAny, writer, true);
        String str = writer.toString();
        logger.debug(str);
        assertTrue("see investigation type", StringUtils.contains(str, "bacd"));
        assertTrue("see site name", StringUtils.contains(str, "siteName"));
        assertTrue("see site type", StringUtils.contains(str, "SiteType"));
        assertEquals("see only one contrib", 1, StringUtils.countMatches(str, "<namePart type=\"given\">admin</namePart>"));
        assertEquals("see only one contrib", 2, StringUtils.countMatches(str, "<namePart type=\"family\">user</namePart>"));
    }
}
