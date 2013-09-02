package org.tdar.filestore.tasks;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;
import org.tdar.core.bean.resource.Archive;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.WorkflowContext;

/**
 * A test to check that the preconditions work: although other classes are used, it's not an integration test, as no database input/output is performed
 * and no file input or output is performed either. It simply checks the preconditions.
 * @author Martin Paulo
 */
public class PrepareArchiveForKettleTaskPreconditionsTest {

    PrepareArchiveForKettleTask task;

    private static GenericDao getDaoThatWillReturn(final Archive archive) {
        GenericDao dao = new GenericDao () {
            @SuppressWarnings("unchecked")
            @Override
            public <E> E find(Class<E> cls, Long id) {
                return (E)archive;
            }
        };
        return dao;
    }

    private static WorkflowContext getContextForArchive(final Archive archive) {
        WorkflowContext ctx = new WorkflowContext();
        ctx.setResourceType(ResourceType.ARCHIVE);
        ctx.setGenericDao(getDaoThatWillReturn(archive));
        return ctx;
    }

    @Test
    public void mustBeArchiveResourceType() {
        WorkflowContext ctx = new WorkflowContext();
        ctx.setResourceType(ResourceType.DOCUMENT);
        task = new PrepareArchiveForKettleTask();
        task.setWorkflowContext(ctx);
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
           assertTrue(e.getMessage(), e.getMessage().startsWith("The Extract Archive Task has been called for a non archive resource!"));
        }
    }
    
    @Test
    public void mustHaveNonNullDao() {
        Archive archive = null;
        task = new PrepareArchiveForKettleTask();
        WorkflowContext ctx = new WorkflowContext();
        ctx.setResourceType(ResourceType.ARCHIVE);
        ctx.setGenericDao(null);
        task.setWorkflowContext(ctx);
        try {
            task.run();
        } catch (Exception e) {
           assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
           assertTrue(e.getMessage(), e.getMessage().startsWith("Generic DAO to retrieve archive not available..."));
        }
    }
    
    @Test
    public void mustBeSetToImportContent() {
        final Archive archive = new Archive();
        task = new PrepareArchiveForKettleTask();
        task.setWorkflowContext(getContextForArchive(archive));
        try {
            task.run();
        } catch (Exception e) {
            assertTrue("Should not be here: " + e.getMessage(), false);
        }
        assertFalse(archive.isImportPeformed());
    }


    @Test
    public void mustNotHavePerformedImport() {
        final Archive archive = new Archive();
        archive.setImportPeformed(true);
        archive.setDoImportContent(true);
        task = new PrepareArchiveForKettleTask();
        task.setWorkflowContext(getContextForArchive(archive));
        try {
            task.run();
        } catch (Exception e) {
            assertTrue("Should not be here: " + e.getMessage(), false);
        }
        assertTrue(archive.isImportPeformed());
        assertTrue(archive.isDoImportContent()); // should not have been changed by test
    }
   
    @Test
    public void mustHaveValidControlFileDir() {
        final Archive archive = new Archive();
        archive.setDoImportContent(true);
        task = new PrepareArchiveForKettleTask();
        task.setWorkflowContext(getContextForArchive(archive));
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Can not write to kettle input directory:"));
        }
    }

    @Test
    public void mustHaveValidCopyFileDir() {
        final Archive archive = new Archive();
        archive.setDoImportContent(true);
        task = new PrepareArchiveForKettleTask();
        task.setKettleInputPath(System.getProperty("java.io.tmpdir"));
        final WorkflowContext contextForArchive = getContextForArchive(archive);
        contextForArchive.setWorkingDirectory(new File(""));
        task.setWorkflowContext(contextForArchive);
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Can not write to directory for file output:"));
        }
    }
    
    @Test
    public void mustHaveAFileToWorkWith() {
        final Archive archive = new Archive();
        archive.setDoImportContent(true);
        task = new PrepareArchiveForKettleTask();
        task.setKettleInputPath(System.getProperty("java.io.tmpdir"));
        final WorkflowContext contextForArchive = getContextForArchive(archive);
        contextForArchive.setWorkingDirectory(new File(System.getProperty("java.io.tmpdir")));
        task.setWorkflowContext(contextForArchive);
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Must have an archive file to work with"));
        }
    }
}
