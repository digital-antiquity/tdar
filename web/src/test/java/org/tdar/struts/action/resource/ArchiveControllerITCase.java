package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.resource.Archive;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.archive.ArchiveController;

/**
 *
 * @author Martin Paulo
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class ArchiveControllerITCase extends AbstractDataIntegrationTestCase {

    // Was "zip", "tar", "bz2", "tgz", but because of user interface confusion we are limiting
    // the choice that users can make.
    private static final Collection<String> ARCHIVE_EXTENSIONS_SUPPORTED = java.util.Arrays.asList(new String[] { "bz2" });
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

    // still being worked on
    @Ignore
    @Test
    @Rollback
    public void doesSave() throws TdarActionException {
        Archive resource = controller.getResource();
        resource.setTitle("Test");
        resource.setDescription("A JUnit test");
        controller.setServletRequest(getServletPostRequest());
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox();
        llb.setWest(0.0003);
        llb.setSouth(0.0001);
        llb.setEast(0.0002);
        llb.setNorth(0.0004);
        controller.setLatitudeLongitudeBoxes(Arrays.asList(llb));
        assertFalse(controller.isSwitchableMapObfuscation());
        String saveResult = controller.save();
        llb = controller.getResource().getFirstActiveLatitudeLongitudeBox();
        assertFalse(llb.getObfuscatedNorth().equals(Double.valueOf(0.0001)));
        assertTrue("Unexpected Action Exceptions were found", controller.getActionErrors().size() == 0);
        assertEquals("Result was expected to be \"SUCCESS\", not " + saveResult, TdarActionSupport.SUCCESS, saveResult);
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void testIsSwitchableMapObfuscationOnForFaims() {
        assertTrue(controller.isSwitchableMapObfuscation());
    }

}
