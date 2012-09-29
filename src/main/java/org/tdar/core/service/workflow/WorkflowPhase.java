package org.tdar.core.service.workflow;

/**
 * @author Adam Brin
 *
 */
public enum WorkflowPhase {

	SETUP("Setup"),
	PRE_PROCESS("Pre-Process"),
	CREATE_DERIVATIVE("Create Derivative"),
	CREATE_ARCHIVAL("Create Archival"),
	POST_PROCESS("Post-Process"),
	CLEANUP("Cleanup");
	
	private String label;
	
	private WorkflowPhase(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
