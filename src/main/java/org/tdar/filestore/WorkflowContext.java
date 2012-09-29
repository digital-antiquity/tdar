/**
 * 
 */
package org.tdar.filestore;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.filestore.tasks.Task;
import org.tdar.filestore.workflows.Workflow;
import org.tdar.utils.SimpleSerializer;

/**
 * @author Adam Brin
 *
 */
public class WorkflowContext implements Serializable {

    private static final long serialVersionUID = -1020989469518487007L;

    private Long informationResourceFileId;
	private Long informationResourceId;
	private List<InformationResourceFileVersion> versions;
	private InformationResourceFileVersion originalFile;
	private File workingDirectory;
	private int numPages;
	private Filestore filestore;
	private Workflow workflow;
	
	public void logTask(Task t, StringBuilder message) {
		
	}

	/*
	 * All of the derivative versions of the file 
	 */
	public List<InformationResourceFileVersion> getVersions() {
		if (versions == null) versions = new ArrayList<InformationResourceFileVersion>();
		return versions;
	}

	public void addVersion(InformationResourceFileVersion version) {
		if (this.versions == null) this.versions = new ArrayList<InformationResourceFileVersion>();
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

	public String toXML() {
		SimpleSerializer ss = new SimpleSerializer();
		ss.addToBlacklist(InformationResourceFile.class);
		ss.addAllToWhitelist(InformationResourceFileVersion.class,true);
		return ss.toXml(this);
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
	 * @param filestore the filestore to set
	 */
	public void setFilestore(Filestore filestore) {
		this.filestore = filestore;
	}

	/**
	 * @return the filestore
	 */
	public Filestore getFilestore() {
		return filestore;
	}

	/**
	 * @param w
	 */
	public void setWorkflow(Workflow w) {
		this.workflow = w;
		w.setWorkflowContext(this);
	}

	public Workflow getWorkflow() {
		return workflow;
	}
}
