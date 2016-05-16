package org.tdar.filestore.tasks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Archive;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.WorkflowContext;

/**
 * A test to check that the preconditions work: although other classes are used, it's not an integration test, as no database or
 * file input/output is performed. It simply checks the preconditions...
 * 
 * @author Martin Paulo
 */
public class PrepareArchiveForKettleTaskPreconditionsTest {

    private static final String BOB_AT_CAT_COM = "bob@cat.com";
    PrepareArchiveForKettleTask task;
    Archive archive;

    private static WorkflowContext getContextForArchive(final Archive archive) {
        WorkflowContext ctx = new WorkflowContext();
        ctx.setResourceType(ResourceType.ARCHIVE);
        ctx.setTransientResource(archive);
        return ctx;
    }

    @Before
    public void prepareTask() {
        // this will get the task through all the preconditions bar the "has files to work with"
        task = new PrepareArchiveForKettleTask();
        archive = new Archive();
        archive.setDoImportContent(true);
        File file = new File(TdarConfiguration.getInstance().getKettleInputPath());
        file.mkdirs();
        WorkflowContext contextForArchive = getContextForArchive(archive);
        task.setWorkflowContext(contextForArchive);
    }

    @Test
    public void mustBeArchiveResourceType() throws Exception {
        task.getWorkflowContext().setResourceType(ResourceType.DOCUMENT);
        task.run();
        assertFalse(task.getWorkflowContext().isProcessedSuccessfully());
    }

    @Test
    public void mustHaveNonNullArchive() {
        task.getWorkflowContext().setTransientResource(null);
        try {
            task.run();
            assertTrue("Should not be here", false);
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Transient copy of archive not available..."));
        }
    }

    @Test
    public void mustBeSetToImportContent() {
        archive.setDoImportContent(false);
        try {
            task.run();
        } catch (Exception e) {
            assertTrue("Should not be here: " + e.getMessage(), false);
        }
        assertFalse(archive.isImportDone());
    }

    @Test
    public void mustNotHavePerformedImport() {
        archive.setImportDone(true);
        try {
            task.run();
        } catch (Exception e) {
            assertTrue("Should not be here: " + e.getMessage(), false);
        }
        // these should not have been changed by test
        assertTrue(archive.isImportDone());
        assertTrue(archive.isDoImportContent());
    }

    @Test
    public void mustHaveValidControlFileDir() {
        archive.setProject(new Project(1L, "test"));
        task.setKettleInputPathOverride("");
        try {
            task.run();
            assertTrue("Should not be here", false);
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Can not write to kettle input directory:"));
        }
    }

    @Test
    public void mustHaveAFileToWorkWith() {

        archive.setProject(new Project(1L, "test"));
        try {
            task.run();
            assertTrue("Should not be here", false);
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Must have an archive file to work with"));
        }
    }

    @Test
    public void willSelectAdminEmailIfNoUploaderEmail() {
        assertTrue(task.getEmailToNotify(archive).equals(TdarConfiguration.getInstance().getSystemAdminEmail()));
    }

    @Test
    public void willSelectUploaderEmailIfSet() {
        TdarUser updater = new TdarUser();
        updater.setEmail(BOB_AT_CAT_COM);
        archive.setUpdatedBy(updater);
        assertTrue(task.getEmailToNotify(archive).equals(BOB_AT_CAT_COM));
    }

    @Test
    public void mustHaveAProjectAssigned() {
        try {
            task.run();
            assertTrue("Should not be here", false);
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Cannot unpack an archive that has not yet been assigned to a project!"));
        }
        // these should not have been changed by test
        assertFalse(archive.isImportDone());
        assertTrue(archive.isDoImportContent());
    }
}
