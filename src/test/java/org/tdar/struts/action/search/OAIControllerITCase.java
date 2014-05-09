/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.struts.data.oai.OAIMetadataFormat;
import org.tdar.struts.data.oai.OAIVerb;

import com.opensymphony.xwork2.Action;

/**
 * @author Adam Brin
 * 
 */
public class OAIControllerITCase extends AbstractSearchControllerITCase {

    @Before
    @Override
    public void reset() {
        reindex();
    }

    @Test
    public void testOAIDCDocument() throws JAXBException {
        OAIController oaiController = generateNewInitializedController(OAIController.class);
        oaiController.setVerb(OAIVerb.GET_RECORD.getVerb());
        oaiController.setIdentifier("oai:tdar.org:Resource:" + TestConstants.TEST_DOCUMENT_ID);
        oaiController.setMetadataPrefix(OAIMetadataFormat.DC.getPrefix());

        String oai = oaiController.oai();
        assertEquals(OAIController.SUCCESS_GET_RECORD, oai);

        oaiController.setIdentifier(TestConstants.TEST_DOCUMENT_ID);

        String oai2 = oaiController.oai();
        assertEquals(Action.ERROR, oai2);
    }
}
