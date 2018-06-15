/**
 * 
 */
package org.tdar.filestore;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.exception.NonFatalWorkflowException;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.core.service.workflow.WorkflowContextService;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.datatable.TDataTable;
import org.tdar.datatable.TDataTableRelationship;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.filestore.tasks.Task;
import org.tdar.utils.ExceptionWrapper;

/**
 * <p>
 * A work in progress.
 * <p>
 * The Workflow context is flattened to XML (hence the Serializable) and passed to the messaging service that will then reflate it and act on its contents.
 * Hence nothing that is not cleanly serializable should be added to the context (no dao's etc). Once the messaging service is finished it will flatten the
 * context back to XML and then return that structure to the application. In this way the workflow tasks are decoupled from the application, I assume with the
 * eventual goal of allowing long running tasks to be run in the background without impacting the user.
 * 
 * @see MessageService#sendFileProcessingRequest(Workflow, FileStoreFile...)
 * @author Adam Brin
 */
@XmlRootElement
public final class WorkflowContext implements Serializable {

    private static final long serialVersionUID = -1020989469518487007L;

    private Long informationResourceId;
    private List<FileStoreFile> versions = new ArrayList<>();
    private List<FileStoreFile> originalFiles = new ArrayList<>();
    private File workingDirectory = null;
    private int numPages = -1;
    private transient Filestore filestore;
    private boolean processedSuccessfully = false;
    private ResourceType resourceType;
    private Class<? extends Workflow> workflowClass;
    private List<String> dataTablesToCleanup = new ArrayList<>();
    private transient List<TDataTable> dataTables = new ArrayList<>();
    private transient List<TDataTableRelationship> relationships = new ArrayList<>();
    private boolean okToStoreInFilestore = true;
    // I would be autowired, but going across the message service and serializing/deserializing, better to just "inject"
    private transient SerializationService serializationService;
    private transient TargetDatabase targetDatabase;

    private List<ExceptionWrapper> exceptions = new ArrayList<>();

    private boolean isErrorFatal;

    public WorkflowContext() {
    }

    public WorkflowContext(Filestore store, long l) {
        this.filestore = store;
        this.informationResourceId = l;
    }

    /**
     * <b>Don't use</b>: currently not yet implemented!
     */
    public void logTask(Task t, StringBuilder message) {
        // TODO!
    }

    /*
     * All of the derivative versions of the file
     */
    @XmlElementWrapper(name = "versions")
    @XmlElement(name = "versionFile")
    public List<FileStoreFile> getVersions() {
        if (versions == null) {
            versions = new ArrayList<>();
        }
        return versions;
    }

    public void addVersion(FileStoreFile version) {
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        this.versions.add(version);
    }

    @XmlElementWrapper(name = "originalFiles")
    @XmlElement(name = "originalFiles")
    public List<FileStoreFile> getOriginalFiles() {
        return originalFiles;
    }

    /*
     * Get the Original File
     */
    public void setOriginalFiles(List<FileStoreFile> originalFile) {
        this.originalFiles = originalFile;
    }

    /*
     * temp directory
     */
    public File getWorkingDirectory() {
        if (workingDirectory == null) {
            workingDirectory = TdarConfiguration.getInstance().getTempDirectory();
            workingDirectory = new File(workingDirectory, "workflow");
            if (!workingDirectory.exists()) {
                workingDirectory.mkdir();
            }
            workingDirectory = new File(workingDirectory, Thread.currentThread().getName() + "-" + System.currentTimeMillis());
            workingDirectory.mkdirs();
        }
        return workingDirectory;
    }

    public String toXML() throws Exception {
        return getSerializationService().convertToXML(this);
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public int getNumPages() {
        return numPages;
    }

    public Long getInformationResourceId() {
        return informationResourceId;
    }

    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }

    /**
     * @param filestore
     *            the filestore to set
     */
    public void setFilestore(Filestore filestore) {
        this.filestore = filestore;
    }

    /**
     * @return the filestore
     */
    @XmlTransient
    public Filestore getFilestore() {
        return filestore;
    }

    public boolean isProcessedSuccessfully() {
        return processedSuccessfully;
    }

    /**
     * Do not call this! it is used by the Workflow instance when processing tasks, and any setting made by the tasks will be overwritten.
     * 
     * @see Workflow#run(WorkflowContext)
     */
    public void setProcessedSuccessfully(boolean processed) {
        this.processedSuccessfully = processed;
    }

    public List<String> getDataTablesToCleanup() {
        return dataTablesToCleanup;
    }

    public void setDataTablesToCleanup(List<String> dataTablesToCleanup) {
        this.dataTablesToCleanup = dataTablesToCleanup;
    }

//    public Resource getTransientResource() {
//        return transientResource;
//    }
//
//    public void setTransientResource(Resource transientResource) {
//        this.transientResource = transientResource;
//    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public void setTargetDatabase(TargetDatabase tdarDataImportDatabase) {
        this.targetDatabase = tdarDataImportDatabase;
    }

    @XmlTransient
    public TargetDatabase getTargetDatabase() {
        return this.targetDatabase;
    }

    /**
     * Keeps a history of the exceptions that are thrown by the task run method if it exits abnormally.
     * If you have an exception you want recorded during that run, that isn't thrown out of the run, then add it using this method!
     * That sure beats calling getExceptions().add(...), and makes for a consistent interface.
     * 
     * @see Workflow#run(WorkflowContext)
     * @see Task#run()
     * @param e
     *            The exception that has brought the Task#run to an untimely demise..
     */
    public void addException(Throwable e) {
        int maxDepth = 4;
        Throwable thrw = e;
        StringBuilder sb = new StringBuilder();

        sb.append(e.getMessage());
        while ((thrw.getCause() != null) && (maxDepth > -1)) {
            thrw = thrw.getCause();
            if (StringUtils.isNotBlank(thrw.getMessage())) {
                sb.append(": ").append(thrw.getMessage());
            }
            maxDepth--;
        }

        ExceptionWrapper exceptionWrapper = new ExceptionWrapper(sb.toString(), e);
        if (e instanceof NonFatalWorkflowException || thrw instanceof NonFatalWorkflowException) {
            exceptionWrapper.setFatal(false);
        }
        this.getExceptions().add(exceptionWrapper);
    }

    /**
     * If you find yourself calling this to add an exception, <b>first ask: why aren't I using addException()?</b>
     * 
     * @see #addException(Throwable)
     * @return the exceptions recorded during the executions of the tasks.
     */
    @XmlElementWrapper(name = "exceptions")
    @XmlElement(name = "exception")
    public List<ExceptionWrapper> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<ExceptionWrapper> exceptions) {
        this.exceptions = exceptions;
    }

    @XmlTransient
    public SerializationService getSerializationService() {
        return serializationService;
    }

    public void setSerializationService(SerializationService serializationService) {
        this.serializationService = serializationService;
    }

    public Class<? extends Workflow> getWorkflowClass() {
        return workflowClass;
    }

    public void setWorkflowClass(Class<? extends Workflow> workflowClass) {
        this.workflowClass = workflowClass;
    }

    @XmlTransient
    public String getExceptionAsString() {
        String exceptions = StringUtils.join(getExceptions(), "\n");
        if (StringUtils.isNotBlank(exceptions)) {
            exceptions = StringUtils.replace(exceptions, TdarConfiguration.getInstance().getFileStoreLocation(), "");
        }
        return exceptions;
    }

    public boolean isErrorFatal() {
        return isErrorFatal;
    }

    /**
     * A subtle one. Your task might have thrown an exception, but was it fatal? ie: was it an error or a warning? If it was an error best set this to true,
     * otherwise don't bother.
     * 
     * @see WorkflowContextService#processContext(WorkflowContext)
     * @param isErrorFatal
     *            If true, then there was an error that should be reported as an error, not a warning...
     */
    public void setErrorFatal(boolean isErrorFatal) {
        this.isErrorFatal = isErrorFatal;
    }

    public void clear() {
        getDataTables().clear();
        getRelationships().clear();
        versions = null;
        originalFiles = null;

        // TODO Auto-generated method stub

    }

    public boolean isOkToStoreInFilestore() {
        return okToStoreInFilestore;
    }

    public void setOkToStoreInFilestore(boolean okToStoreInFilestore) {
        this.okToStoreInFilestore = okToStoreInFilestore;
    }

    public List<TDataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(List<TDataTable> dataTables) {
        this.dataTables = dataTables;
    }

    public List<TDataTableRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<TDataTableRelationship> relationships) {
        this.relationships = relationships;
    }

}
