package org.tdar.core.bean.resource;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.search.query.QueryFieldNames;

/**
 * $Id$
 * 
 * Controlled vocabulary for resource types.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public enum ResourceType implements HasLabel, Comparable<ResourceType>, Facetable {
    CODING_SHEET("Coding Sheet", 9, "Dataset", "unknown", CodingSheet.class),
    DATASET("Dataset", 3, "Dataset", "unknown", Dataset.class),
    DOCUMENT("Document", 1, "Text", "document", Document.class),
    IMAGE("Image", 2, "Still Image", "unknown", Image.class),
    SENSORY_DATA("3D & Sensory Data", 7, "Interactive Resource", "unknown", SensoryData.class),
    GEOSPATIAL("GIS",6, "Dataset", "unknown", Geospatial.class),
    ONTOLOGY("Ontology", 8, "Dataset", "unknown", Ontology.class),
    PROJECT("Project", 5, Project.class),
    VIDEO("Video", 4, "Moving Image", "unknown", Video.class);

    private final String label;
    private final String dcmiTypeString;
    private final String openUrlGenre;
    private int order;
    private transient Integer count;
    private final Class<? extends Resource> resourceClass;

    private ResourceType(String label, int order,
            Class<? extends Resource> resourceClass) {
        this(label, order, "", "unknown", resourceClass);
    }

    private ResourceType(String label, int order, String dcmiTypeString,
            String genre, Class<? extends Resource> resourceClass) {
        this.label = label;
        this.openUrlGenre = genre;
        this.setOrder(order);
        this.dcmiTypeString = dcmiTypeString;
        this.resourceClass = resourceClass;
    }

    public String getPlural() {
        switch (this) {
            case ONTOLOGY:
                return "Ontologies";
            case SENSORY_DATA:
                return SENSORY_DATA.label;
            case GEOSPATIAL:
                return GEOSPATIAL.label;
            default:
                return getLabel().concat("s");
        }
    }

    public boolean supportBulkUpload() {
        switch (this) {
            case DOCUMENT:
            case IMAGE:
                return true;
            default:
                return false;
        }
    }

    public String getFieldName() {
        StringBuilder sb = new StringBuilder();
        // get rid of underscore
        for (String part : name().split("\\_")) {
            sb.append(StringUtils.capitalize(part.toLowerCase()));
        }
        return StringUtils.uncapitalize(sb.toString());
    }

    public boolean isDataset() {
        return this == DATASET;
    }

    public String getSortName() {
        return this.order + this.name();
    }

    public boolean isSensoryData() {
        return this == SENSORY_DATA;
    }

    public boolean isGeospatial() {
        return this == GEOSPATIAL;
    }

    public boolean isCodingSheet() {
        return this == CODING_SHEET;
    }

    public boolean isImage() {
        return this == IMAGE;
    }

    public boolean isVideo() {
        return this == VIDEO;
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
     * 
     * @return a String representing the DcmiType for this Document
     */
    public String toDcmiTypeString() {
        return dcmiTypeString;
    }

    /**
     * Returns the ResourceType corresponding to the String given or null if
     * none exists. Used in place of valueOf since valueOf throws
     * RuntimeExceptions given invalid input.
     */
    public static ResourceType fromString(String resourceTypeString) {
        if (StringUtils.isEmpty(resourceTypeString)) {
            return null;
        }
        // try to convert incoming resource type String query parameter to
        // ResourceType enum.. unfortunately valueOf only throws
        // RuntimeExceptions.
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isSupporting() {
        switch (this) {
            case ONTOLOGY:
            case CODING_SHEET:
                return true;
            default:
                return false;
        }
    }

    public String getOpenUrlGenre() {
        return openUrlGenre;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getUrlNamespace() {
        String urlToReturn = name();
        return urlToReturn.toLowerCase().replaceAll("_", "-");
    }

    public String getLuceneFieldName() {
        return QueryFieldNames.RESOURCE_TYPE;
    }

    @Override
    public ResourceType getValueOf(String val) {
        return valueOf(val);
    }

    public boolean hasDemensions() {
        switch (this) {
            case IMAGE:
            case GEOSPATIAL: //?
            case SENSORY_DATA:
                return true;
            default:
                return false;
        }
    }

    public boolean hasDataTables() {
        switch (this) {
            case DATASET:
            case GEOSPATIAL: //?
//            case SENSORY_DATA:
                return true;
            default:
                return false;
        }
    }

    public boolean isCompositeFilesEnabled() {
        switch (this) {
            case DATASET:
            case GEOSPATIAL: //?
//            case SENSORY_DATA:
                return true;
            default:
                return false;
        }
    }
}
