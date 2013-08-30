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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.workflow.WorkflowContextService;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.filestore.tasks.Task;
import org.tdar.utils.ExceptionWrapper;

/**
 * @author Adam Brin
 * 
 */
@XmlRootElement
public class WorkflowContext implements Serializable {

    private static final long serialVersionUID = -1020989469518487007L;

//    private Long informationResourceFileId;
    private Long informationResourceId;
    private List<InformationResourceFileVersion> versions = new ArrayList<>();
    private List<InformationResourceFileVersion> originalFiles = new ArrayList<>();
    private File workingDirectory = TdarConfiguration.getInstance().getTempDirectory();
    private int numPages = -1;
    private transient Filestore filestore;
    private boolean processedSuccessfully = false;
    private ResourceType resourceType;
    private Class<? extends Workflow> workflowClass;
    private List<String> dataTablesToCleanup = new ArrayList<String>();
    private transient Resource transientResource;

    // I would be autowired, but going across the message service and serializing/deserializing, better to just "inject"
    private transient XmlService xmlService;
    private transient TargetDatabase targetDatabase;

    private List<ExceptionWrapper> exceptions = new ArrayList<ExceptionWrapper>();

    private boolean isErrorFatal;

    public void logTask(Task t, StringBuilder message) {

    }

    /*
     * All of the derivative versions of the file
     */
    @XmlElementWrapper(name = "versions")
    @XmlElement(name = "informationResourceFileVersion")
    public List<InformationResourceFileVersion> getVersions() {
        if (versions == null)
            versions = new ArrayList<InformationResourceFileVersion>();
        return versions;
    }

    public void addVersion(InformationResourceFileVersion version) {
        if (this.versions == null)
            this.versions = new ArrayList<InformationResourceFileVersion>();
        this.versions.add(version);
    }

    @XmlElementWrapper(name = "originalFiles")
    @XmlElement(name = "informationResourceFileVersion")
    public List<InformationResourceFileVersion> getOriginalFiles() {
        return originalFiles;
    }

    /*
     * Get the Original File
     */
    public void setOriginalFiles(List<InformationResourceFileVersion> originalFile) {
        this.originalFiles = originalFile;
    }

    /*
     * temp directory
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String toXML() throws Exception {
        return getXmlService().convertToXML(this);
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

    public Resource getTransientResource() {
        return transientResource;
    }

    public void setTransientResource(Resource transientResource) {
        this.transientResource = transientResource;
    }

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
     * Do not use this!
     * Keeps a history of the exceptions that are thrown in the body of the task run method.
     * @see Workflow#run(WorkflowContext)
     * @see Task#run()
     * @param e The exception that has brought the Task#run to an untimely demise..
     */
    public void addException(Throwable e) {
        int maxDepth = 4;
        Throwable thrw = e;
        StringBuilder sb = new StringBuilder();

        sb.append(e.getMessage());
        while (thrw.getCause() != null && maxDepth > -1) {
            thrw = thrw.getCause();
            if (StringUtils.isNotBlank(thrw.getMessage())) {
                sb.append(": ").append(thrw.getMessage());
            }
            maxDepth--;
        }

        this.getExceptions().add(new ExceptionWrapper(sb.toString(), ExceptionUtils.getFullStackTrace(e)));
    }

    @XmlElementWrapper(name = "exceptions")
    @XmlElement(name = "exception")
    public List<ExceptionWrapper> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<ExceptionWrapper> exceptions) {
        this.exceptions = exceptions;
    }

    @XmlTransient
    public XmlService getXmlService() {
        return xmlService;
    }

    public void setXmlService(XmlService xmlService) {
        this.xmlService = xmlService;
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
     * @see WorkflowContextService#processContext(WorkflowContext)
     * @param isErrorFatal If true, then there was an error that should be reported as an error, not a warning...
     */
    public void setErrorFatal(boolean isErrorFatal) {
        this.isErrorFatal = isErrorFatal;
    }

}
