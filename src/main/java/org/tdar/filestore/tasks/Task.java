/**
 * 
 */
package org.tdar.filestore.tasks;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.filestore.WorkflowContext;

/**
 * @author Adam Brin
 * 
 */
public interface Task extends Serializable {

    /*
     * Pass in the generic context
     */
    void setWorkflowContext(WorkflowContext ctx);

    WorkflowContext getWorkflowContext();

    /**
     * Run method called by the task work flow process. The implicit contract is that if the task exits abnormally, it will throw an exception during
     * the execution of this method.
     * Also, the exceptions are not necessarily logged: so if you want a record of the problem, log error before throwing the
     * exception.
     * 
     * @see WorkflowContext#setErrorFatal(boolean)
     * @see Workflow#run(WorkflowContext)
     */
    void run() throws Exception;

    /*
     * setup method
     */
    void prepare();

    /*
     * shutdown method
     */
    void cleanup();

    /*
     * get the Name of the task (used for logging) etc.
     */
    String getName();

    public abstract static class AbstractTask implements Task {

        private static final long serialVersionUID = 3655364565734681218L;
        private final transient Logger logger = LoggerFactory.getLogger(getClass());
        private WorkflowContext workflowContext;

        @Override
        public void setWorkflowContext(WorkflowContext ctx) {
            this.workflowContext = ctx;
        }

        @Override
        public WorkflowContext getWorkflowContext() {
            return workflowContext;
        }

        @Override
        public void prepare() {
            // override this to add your own prepare actions...
        }

        @Override
        public void cleanup() {
            // override this to add your own cleanup actions...
        }

        void deleteFile(File f) {
            f.delete();
        }

        protected InformationResourceFileVersion generateInformationResourceFileVersionFromOriginal(InformationResourceFileVersion originalVersion, File f,
                VersionType type) {
            WorkflowContext ctx = getWorkflowContext();
            InformationResourceFileVersion version = new InformationResourceFileVersion(type, f.getName(), originalVersion.getVersion(),
                    ctx.getInformationResourceId(), originalVersion.getInformationResourceFileId());

            try {
                ctx.getFilestore().store(ObjectType.RESOURCE, f, version);
                version.setTransientFile(f);
            } catch (IOException e) {
                getLogger().warn("cannot store version", e);
            }
            return version;
        }

        /**
         * Utility method to mark and log a fatal error, and then complete by throwing a TdarRecoverableRuntimeException
         * 
         * @param message
         *            The message to be logged as an error.
         * @Throws TdarRecoverableRuntimeException <b>NB</b> Each and every time this method is called!
         */
        protected void recordErrorAndExit(final String message, final Object key) {
            getWorkflowContext().setErrorFatal(true); // anything that stops us running should be reported as an error, IMHO.
            getLogger().error(message);
            throw new TdarRecoverableRuntimeException(message, Arrays.asList(key));
        }

        /**
         * Utility method to mark and log a fatal error, and then complete by throwing a TdarRecoverableRuntimeException
         * 
         * @param message
         *            The message to be logged as an error.
         * @Throws TdarRecoverableRuntimeException <b>NB</b> Each and every time this method is called!
         */
        protected void recordErrorAndExit(final String message) {
            getWorkflowContext().setErrorFatal(true); // anything that stops us running should be reported as an error, IMHO.
            getLogger().error(message);
            throw new TdarRecoverableRuntimeException(message);
        }

        void mkParentDirs(File outputFile) {
            File outputFileDirectory = getParentDirectory(outputFile);
            try {
                outputFileDirectory.mkdirs();
            } catch (Exception e) {
                getLogger().warn("cannot make parent directories", e);
            }
        }

        File getParentDirectory(File outputFile) {
            return new File(outputFile.getParent());
        }

        void addDerivativeFile(InformationResourceFileVersion originalVersion, File file, String extension, String text, VersionType type) throws Exception {
            if (StringUtils.isNotBlank(text)) {
                File f = new File(getWorkflowContext().getWorkingDirectory(), file.getName() + "." + extension);
                FileUtils.writeStringToFile(f, text);
                addDerivativeFile(originalVersion, f, type);
            }
        }

        void addDerivativeFile(InformationResourceFileVersion orginalVersion, File f, VersionType type) {
            if (f.length() > 0) {
                getLogger().info("Writing file: " + f);
                InformationResourceFileVersion version = generateInformationResourceFileVersionFromOriginal(orginalVersion, f, type);
                getWorkflowContext().addVersion(version);
            } else {
                logger.warn("writing empty file ... skipping " + f.getName());
            }
        }

        public Logger getLogger() {
            return logger;
        }
    }
}
