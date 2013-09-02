package org.tdar.filestore.tasks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.resource.Archive;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.WorkflowContext;

/**
 * A test to check that the preconditions work: although other classes are used, it's not an integration test, as no database or 
 * file input/output is performed. It simply checks the preconditions... 
 * @author Martin Paulo
 */
public class PrepareArchiveForKettleTaskPreconditionsTest {

    PrepareArchiveForKettleTask task;
    Archive archive;
    
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
    
    @Before
    public void prepareTask() {
        // this will get the task through all the preconditions bar the "has files to work with"
        task = new PrepareArchiveForKettleTask();
        archive = new Archive();
        archive.setDoImportContent(true);
        task.setKettleInputPath(System.getProperty("java.io.tmpdir"));
        WorkflowContext contextForArchive = getContextForArchive(archive);
        contextForArchive.setWorkingDirectory(new File(System.getProperty("java.io.tmpdir")));
        task.setWorkflowContext(contextForArchive);    }

    @Test
    public void mustBeArchiveResourceType() {
        task.getWorkflowContext().setResourceType(ResourceType.DOCUMENT);
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
           assertTrue(e.getMessage(), e.getMessage().startsWith("The Extract Archive Task has been called for a non archive resource!"));
        }
    }
    
    @Test
    public void mustHaveNonNullDao() {
        task.getWorkflowContext().setGenericDao(null);
        try {
            task.run();
        } catch (Exception e) {
           assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
           assertTrue(e.getMessage(), e.getMessage().startsWith("Generic DAO to retrieve archive not available..."));
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
        assertFalse(archive.isImportPeformed());
    }


    @Test
    public void mustNotHavePerformedImport() {
        archive.setImportPeformed(true);
        try {
            task.run();
        } catch (Exception e) {
            assertTrue("Should not be here: " + e.getMessage(), false);
        }
        // these should not have been changed by test
        assertTrue(archive.isImportPeformed());
        assertTrue(archive.isDoImportContent());
    }
   
    @Test
    public void mustHaveValidControlFileDir() {
        task.setKettleInputPath("");
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Can not write to kettle input directory:"));
        }
    }

    @Test
    public void mustHaveValidCopyFileDir() {
        task.getWorkflowContext().setWorkingDirectory(new File(""));
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Can not write to directory for file output:"));
        }
    }
    
    @Test
    public void mustHaveAFileToWorkWith() {
        try {
            task.run();
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getClass().equals(TdarRecoverableRuntimeException.class));
            assertTrue(e.getMessage(), e.getMessage().startsWith("Must have an archive file to work with"));
        }
    }
}
