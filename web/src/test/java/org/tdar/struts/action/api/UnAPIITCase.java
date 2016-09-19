/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts.action.UnapiController;

/**
 * @author Adam Brin
 * 
 */
public class UnAPIITCase extends AbstractControllerITCase {

    public static Long DOCUMENT_ID = 4231L;

    @Test
    public void testUnAPIControllerDC() {
        UnapiController unapiController = generateNewController(UnapiController.class);
        unapiController.setId(DOCUMENT_ID);
        assertEquals("formats", unapiController.execute());
        unapiController.setFormat("oai_dc");
        assertEquals("asformat", unapiController.execute());
        assertEquals("/unapi/dc/" + DOCUMENT_ID, unapiController.getFormatUrl());
    }

    @Test
    public void testUnAPIControllerMods() {
        UnapiController unapiController = generateNewController(UnapiController.class);
        unapiController.setId(DOCUMENT_ID);
        assertEquals("formats", unapiController.execute());
        unapiController.setFormat("mods");
        assertEquals("asformat", unapiController.execute());
        assertEquals("/unapi/mods/" + DOCUMENT_ID, unapiController.getFormatUrl());
    }

    @Test
    public void testUnAPIControllerBad() {
        UnapiController unapiController = generateNewController(UnapiController.class);
        unapiController.setId(DOCUMENT_ID);
        assertEquals("formats", unapiController.execute());
        unapiController.setFormat("db");
        assertEquals("noformat", unapiController.execute());
        unapiController.setId(DOCUMENT_ID + 10000);
        assertEquals(TdarActionSupport.NOT_FOUND, unapiController.execute());
    }

}
