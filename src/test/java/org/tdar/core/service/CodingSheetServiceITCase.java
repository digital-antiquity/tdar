/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionSupport;

/**
 * @author Adam Brin
 * 
 */
public class CodingSheetServiceITCase extends AbstractControllerITCase {

    @Autowired
    CodingSheetService codingSheetService;

    @Rollback
    @Test
    public void testCodingSheetFindAll() {
        List<CodingSheet> sparseCodingSheets = codingSheetService.findSparseCodingSheetListBySubmitter(getUser());
        assertTrue(!sparseCodingSheets.isEmpty());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        return null;
    }
}
