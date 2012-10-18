/**
 * 
 */
package org.tdar.filestore.tasks;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.filestore.WorkflowContext;

/**
 * @author Adam Brin
 * 
 */
public interface Task extends Serializable {

    /*
     * Pass in the generic context
     */
    public void setWorkflowContext(WorkflowContext ctx);

    public WorkflowContext getWorkflowContext();

    /*
     * Run method called by the task workflow process
     */
    public void run() throws Exception;

    /*
     * setup method
     */
    public void prepare();

    /*
     * shutdown method
     */
    public void cleanup();

    /*
     * get the Name of the task (used for logging) etc.
     */
    public String getName();

    public abstract static class AbstractTask implements Task {

        private static final long serialVersionUID = 3655364565734681218L;
        private final transient Logger logger = Logger.getLogger(getClass());
        private WorkflowContext workflowContext;

        public void setWorkflowContext(WorkflowContext ctx) {
            this.workflowContext = ctx;
        }

        public WorkflowContext getWorkflowContext() {
            return workflowContext;
        }

        public void prepare() {
        }

        public void cleanup() {
        }

        void deleteFile(File f) {
            f.delete();
        }

        protected InformationResourceFileVersion generateInformationResourceFileVersion(File f, VersionType type) {
            WorkflowContext ctx = getWorkflowContext();
            InformationResourceFileVersion version = new InformationResourceFileVersion(type, f.getName(), ctx.getOriginalFile().getVersion(),
                    ctx.getInformationResourceId(), ctx.getInformationResourceFileId());

            try {
                ctx.getFilestore().store(f, version);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return version;
        }

        void mkParentDirs(File outputFile) {
            File outputFileDirectory = getParentDirectory(outputFile);
            try {
                outputFileDirectory.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File getParentDirectory(File outputFile) {
            return new File(outputFile.getParent());
        }

        void addDerivativeFile(File file, String extension, String text, VersionType type) throws Exception {
            if (StringUtils.isNotBlank(text)) {
                File f = new File(getWorkflowContext().getWorkingDirectory(), file.getName() + "." + extension);
                FileUtils.writeStringToFile(f, text);
                addDerivativeFile(f, type);
            }
        }

        void addDerivativeFile(File f, VersionType type) throws IOException {
            if (f.length() > 0) {
                getLogger().info("Writing file: " + f);
                InformationResourceFileVersion version = generateInformationResourceFileVersion(f, type);
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
