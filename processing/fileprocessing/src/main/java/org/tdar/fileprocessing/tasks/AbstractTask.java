package org.tdar.fileprocessing.tasks;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.fileprocessing.workflows.WorkflowContext;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.VersionType;

public abstract class AbstractTask implements Task {

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
        boolean delete = f.delete();
        if (delete == false) {
            throw new TdarRecoverableRuntimeException("abstractTask.cannot_delete" , Arrays.asList(f));
        }
    }

    protected FileStoreFile generateInformationResourceFileVersionFromOriginal(FileStoreFile originalVersion, File f,
            VersionType type) {
        WorkflowContext ctx = getWorkflowContext();
        FileStoreFile version = new FileStoreFile(FilestoreObjectType.RESOURCE, type, f.getName(), originalVersion.getVersion(),
                ctx.getInformationResourceId(), originalVersion.getInformationResourceFileId(), null);

        if (ctx.isOkToStoreInFilestore()) {
            try {
                ctx.getFilestore().store(FilestoreObjectType.RESOURCE, f, version);
            } catch (IOException e) {
                getLogger().warn("cannot store version", e);
            }
        }
        version.setTransientFile(f);
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

    File getParentDirectory(File outputFile) {
        return new File(outputFile.getParent());
    }

    void addDerivativeFile(FileStoreFile originalVersion, File file, String extension, String text, VersionType type) throws Exception {
        if (StringUtils.isNotBlank(text)) {
            File f = new File(getWorkflowContext().getWorkingDirectory(), file.getName() + "." + extension);
            FileUtils.writeStringToFile(f, text);
            addDerivativeFile(originalVersion, f, type);
        }
    }

    void addDerivativeFile(FileStoreFile orginalVersion, File f, VersionType type) {
        if (f.length() > 0) {
            getLogger().info("Writing file: " + f);
            FileStoreFile version = generateInformationResourceFileVersionFromOriginal(orginalVersion, f, type);
            getWorkflowContext().addVersion(version);
        } else {
            logger.warn("writing empty file ... skipping " + f.getName());
        }
    }

    public Logger getLogger() {
        return logger;
    }
}