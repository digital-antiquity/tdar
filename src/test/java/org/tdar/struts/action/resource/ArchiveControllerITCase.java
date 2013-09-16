package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Archive;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

/**
 * 
 * @author Martin Paulo
 */
public class ArchiveControllerITCase extends AbstractDataIntegrationTestCase {

    private static final Collection<String> ARCHIVE_EXTENSIONS_SUPPORTED = java.util.Arrays.asList(new String[] { "zip", "tar", "bz2", "tgz" });
    private ArchiveController controller;

    @Before
    public void setUpArchiveController() {
        controller = generateNewInitializedController(ArchiveController.class);
    }

    @Test
    public void isSingleFileUploadOnly() {
        assertFalse(controller.isMultipleFileUploadEnabled());
    }

    @Test
    public void isReturningCorrectFileTypes() {
        Set<String> extensions = controller.getValidFileExtensions();
        // if each set contains the other completely, then they must be identical sets...
        assertTrue("Have archive extensions changed?", extensions.containsAll(ARCHIVE_EXTENSIONS_SUPPORTED));
        assertTrue("Have archive extensions changed?", ARCHIVE_EXTENSIONS_SUPPORTED.containsAll(extensions));
    }

    @Test
    public void isFactoryForArchive() {
        controller.setPersistable(null); // make sure there is no persistable
        assertTrue(controller.getPersistableClass().isAssignableFrom(Archive.class));
        assertTrue(controller.getResource() != null);
    }

    @Test
    public void areGetAndSetCongruent() {
        assertTrue(controller.getArchive() == null);
        Archive archiveIn = new Archive();
        controller.setArchive(archiveIn);
        Archive archiveOut = controller.getArchive();
        assertTrue(archiveIn.equals(archiveOut));
    }

    @Test
    @Rollback
    public void doesSave() throws TdarActionException {
        Archive resource = controller.getResource();
        resource.setTitle("Test");
        resource.setDescription("A JUnit test");
        controller.setServletRequest(getServletPostRequest());
        String saveResult = controller.save();
        assertTrue("Unexpected Action Exceptions were found", controller.getActionErrors().size() == 0);
        assertEquals("Result was expected to be \"SUCCESS\", not " + saveResult, TdarActionSupport.SUCCESS, saveResult);
    }
}
