package org.tdar.core.bean.resource;

import org.apache.commons.lang.StringUtils;

/**
 * $Id$
 * 
 * Controlled vocabulary for resource types.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public enum ResourceType {
    CODING_SHEET("Coding Sheet", "Dataset", CodingSheet.class),
    DATASET("Dataset", "Dataset", Dataset.class),
    DOCUMENT("Document", "Text", Document.class),
    IMAGE("Image", "Still Image", Image.class),
    SENSORY_DATA("Sensory Data", "Interactive Resource", SensoryData.class),
    ONTOLOGY("Ontology", "Dataset", Ontology.class),
    PROJECT("Project", Project.class);

    private final String label;
    private final String dcmiTypeString;
    private final Class<? extends Resource> resourceClass;

    private ResourceType(String label, Class<? extends Resource> resourceClass) {
        this(label, "", resourceClass);
    }

    private ResourceType(String label, String dcmiTypeString, Class<? extends Resource> resourceClass) {
        this.label = label;
        this.dcmiTypeString = dcmiTypeString;
        this.resourceClass = resourceClass;
    }
    
    public String getPlural() {
        switch(this) {
            case ONTOLOGY:
                return "Ontologies";
            case SENSORY_DATA:
                return SENSORY_DATA.label;
            default:
                return this.label.concat("s");
        }
    }

    public boolean isDataset() {
        return this == DATASET;
    }

    public boolean isSensoryData() {
        return this == SENSORY_DATA;
    }

    public boolean isCodingSheet() {
        return this == CODING_SHEET;
    }

    public boolean isImage() {
        return this == IMAGE;
    }

    public boolean isDocument() {
        return this == DOCUMENT;
    }

    public boolean isOntology() {
        return this == ONTOLOGY;
    }

    public boolean isProject() {
        return this == PROJECT;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns the DcmiType String name that corresponds to this ResourceType.
     * FIXME: should this mapping should be maintained in the database instead?
     * 
     * @return a String representing the DcmiType for this Document
     */
    public String toDcmiTypeString() {
        return dcmiTypeString;
    }

    /**
     * Returns the ResourceType corresponding to the String given or null if none
     * exists. Used in place of valueOf since valueOf throws RuntimeExceptions
     * given invalid input.
     */
    public static ResourceType fromString(String resourceTypeString) {
        if (StringUtils.isEmpty(resourceTypeString)) {
            return null;
        }
        // try to convert incoming resource type String query parameter to
        // ResourceType enum.. unfortunately valueOf only throws RuntimeExceptions.
        try {
            return ResourceType.valueOf(resourceTypeString);
        } catch (Exception exception) {
            return null;
        }
    }

    public static ResourceType fromClass(Class<?> clas) {
        for (ResourceType type : values()) {
            if (type.getResourceClass().equals(clas))
                return type;
        }

        return null;
    }

    public Class<? extends Resource> getResourceClass() {
        return resourceClass;
    }
}
