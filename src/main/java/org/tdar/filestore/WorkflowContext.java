/**
 * 
 */
package org.tdar.filestore;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.workflow.Workflow;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.filestore.tasks.Task;

/**
 * @author Adam Brin
 * 
 */
@XmlRootElement
public class WorkflowContext implements Serializable {

	private static final long serialVersionUID = -1020989469518487007L;

	private Long informationResourceFileId;
	private Long informationResourceId;
	private List<InformationResourceFileVersion> versions;
	private InformationResourceFileVersion originalFile;
	private File workingDirectory;
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

	private List<String> exceptions = new ArrayList<String>();
	private List<String> stackTraces = new ArrayList<String>();

	public void logTask(Task t, StringBuilder message) {

	}

	/*
	 * All of the derivative versions of the file
	 */
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

	public InformationResourceFileVersion getOriginalFile() {
		return originalFile;
	}

	/*
	 * Get the Original File
	 */
	public void setOriginalFile(InformationResourceFileVersion originalFile) {
		this.originalFile = originalFile;
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

	public Long getInformationResourceFileId() {
		return informationResourceFileId;
	}

	public void setInformationResourceFileId(Long informationResourceFileId) {
		this.informationResourceFileId = informationResourceFileId;
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

	public void addException(Throwable e) {
        int maxDepth = 4;
        Throwable thrw = e;
        StringBuilder sb = new StringBuilder(e.getMessage());

        while (thrw.getCause() != null && maxDepth > -1) {
            thrw = thrw.getCause();
            if (StringUtils.isNotBlank(thrw.getMessage())) {
                sb.append(": ").append(thrw.getMessage());
            }
            maxDepth--;
        }

		this.getExceptions().add(sb.toString());
		this.getStackTraces().add(ExceptionUtils.getFullStackTrace(e));
	}

	public List<String> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<String> exceptions) {
		this.exceptions = exceptions;
	}

	public List<String> getStackTraces() {
		return stackTraces;
	}

	public void setStackTraces(List<String> stackTraces) {
		this.stackTraces = stackTraces;
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
}
