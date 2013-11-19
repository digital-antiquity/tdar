package org.tdar.filestore.tasks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.entity.Person;
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
        task.setKettleInputPath(System.getProperty("java.io.tmpdir"));
        WorkflowContext contextForArchive = getContextForArchive(archive);
        contextForArchive.setWorkingDirectory(new File(System.getProperty("java.io.tmpdir")));
        task.setWorkflowContext(contextForArchive);
    }

    @Test
    public void mustBeArchiveResourceType() {
        task.getWorkflowContext().setResourceType(ResourceType.DOCUMENT);
        try {
            task.run();
            assertTrue("Should not be here", false);
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("The Extract Archive Task has been called for a non archive resource!"));
        }
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

    @SuppressWarnings("deprecation")
    @Test
    public void mustHaveValidControlFileDir() {
        archive.setProject(new Project(1L, "test" ));
        task.setKettleInputPath("");
        try {
            task.run();
            assertTrue("Should not be here", false);
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Can not write to kettle input directory:"));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void mustHaveValidCopyFileDir() {
        archive.setProject(new Project(1L, "test" ));
        task.getWorkflowContext().setWorkingDirectory(new File(""));
        try {
            task.run();
            assertTrue("Should not be here", false);
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Can not write to directory for file output:"));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void mustHaveAFileToWorkWith() {
        archive.setProject(new Project(1L, "test" ));
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
        Person updater = new Person();
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
