package org.tdar.core.bean.resource;

/**
 * $Id$
 * 
 * <p>
 * Resource annotations can be:
 * <ul>
 * <li>identifiers (e.g., )</li>
 * <li>general notes (e.g., )</li>
 * <li>redaction notes (e.g., )</li>
 * </ul>
 * </p>
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public enum ResourceAnnotationType {
	
	IDENTIFIER("Resource Identifier");
    
    private final String label;
    
    private ResourceAnnotationType(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
