/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.search;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.struts.data.oai.OAIMetadataFormat;
import org.tdar.struts.data.oai.OAIVerb;

/**
 * @author Adam Brin
 * 
 */
public class OAIControllerITCase extends AbstractSearchControllerITCase {

    @Before
    public void reset() {
        reindex();
    }

    @Test
    public void testOAIDCDocument() throws JAXBException {
        OAIController oaiController = generateNewInitializedController(OAIController.class);
        oaiController.setVerb(OAIVerb.GET_RECORD.getVerb());
        oaiController.setIdentifier(TestConstants.TEST_DOCUMENT_ID);
        oaiController.setMetadataPrefix(OAIMetadataFormat.DC.getPrefix());

        oaiController.oai();
    }
}
