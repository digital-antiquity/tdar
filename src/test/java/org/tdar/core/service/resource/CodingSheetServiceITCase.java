/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service.resource;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
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
        List<CodingSheet> sparseCodingSheets = codingSheetService.findSparseCodingSheetList();
        assertTrue(!sparseCodingSheets.isEmpty());
    }

    @Rollback
    @Test
    public void testNonUniqueCodes() {
        CodingSheet sheet = new CodingSheet();
        sheet.markUpdated(getBasicUser());
        sheet.setTitle("test");
        sheet.setDescription("test");
        Throwable e = null;
        try {
            codingSheetService.parseUpload(sheet, "test.csv", new FileInputStream(new File(TestConstants.TEST_CODING_SHEET_DIR, "nonuniquecodes.csv")));
        } catch (Throwable ex) {
            ex.printStackTrace();
            e = ex;
        }
        assertTrue(e instanceof TdarRecoverableRuntimeException);
        assertTrue(e.getMessage().contains("unique"));
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
