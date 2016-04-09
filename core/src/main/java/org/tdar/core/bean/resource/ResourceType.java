package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.transform.ModsTransformer.DcmiModsTypeMapper;
import org.tdar.utils.MessageHelper;

/**
 * 
 * Controlled vocabulary for resource types.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public enum ResourceType implements HasLabel, Localizable, PluralLocalizable {
    CODING_SHEET("Coding Sheet", 10, "Dataset", "unknown", "Dataset", CodingSheet.class),
    DATASET("Dataset", 3, "Dataset", "unknown", "Dataset", Dataset.class),
    DOCUMENT("Document", 1, "Text", "document", "Book", Document.class),
    IMAGE("Image", 2, "Still Image", "unknown", "Photograph", Image.class),
    SENSORY_DATA("3D & Sensory Data", 7, "Interactive Resource", "unknown", "Dataset", SensoryData.class),
    GEOSPATIAL("GIS", 6, "Dataset", "unknown", "Dataset", Geospatial.class),
    ONTOLOGY("Ontology", 9, "Dataset", "unknown", "Dataset", Ontology.class),
    PROJECT("Project", 5, "ItemList", Project.class),
    VIDEO("Video", 4, "Moving Image", "unknown", "Movie", Video.class),
    ARCHIVE("Site Archive", 8, "Collection", "unknown", "SoftwareApplication", Archive.class),
    AUDIO("Audio", 11, "Sound", "unknown", "AudioObject", Audio.class);

    private final String label;
    /**
     * If possible, should match one of the strings referenced in the DcmiModsTypeMapper...
     * At the moment PROJECT and ARCHIVE don't match. Is this an issue?
     * 
     * @see DcmiModsTypeMapper
     */
    private final String dcmiTypeString;
    private final String openUrlGenre;
    private int order;
    // Schema is one of from http://schema.org/docs/full.html
    private String schema;
    private final Class<? extends Resource> resourceClass;

    private ResourceType(String label, int order, String schema, Class<? extends Resource> resourceClass) {
        this(label, order, "", "unknown", schema, resourceClass);
    }

    private ResourceType(String label, int order, String dcmiTypeString, String genre, String schema, Class<? extends Resource> resourceClass) {
        this.label = label;
        this.openUrlGenre = genre;
        this.setOrder(order);
        this.dcmiTypeString = dcmiTypeString;
        this.resourceClass = resourceClass;
        this.schema = schema;
    }

    public String getPlural() {
        switch (this) {
            case ONTOLOGY:
                return "Ontologies";
            case SENSORY_DATA:
                return SENSORY_DATA.label;
            case GEOSPATIAL:
                return GEOSPATIAL.label;
            case AUDIO:
                return AUDIO.label;
            default:
                return getLabel().concat("s");
        }
    }

    public boolean supportBulkUpload() {
        switch (this) {
            case DOCUMENT:
            case DATASET:
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

    public String getSortName() {
        return this.order + this.name();
    }

    public boolean isDataset() {
        return this == DATASET;
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

    public boolean isArchive() {
        return this == ARCHIVE;
    }

    public boolean isAudio() {
        return this == AUDIO;
    }

    @Override
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
            if (type.getResourceClass().equals(clas)) {
                return type;
            }
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

    public String getUrlNamespace() {
        String urlToReturn = name();
        return urlToReturn.toLowerCase().replaceAll("_", "-");
    }

    // @Override
    // public String getLuceneFieldName() {
    // return QueryFieldNames.RESOURCE_TYPE;
    // }

    public boolean hasDemensions() {
        switch (this) {
            case IMAGE:
            case GEOSPATIAL:
            case SENSORY_DATA:
                return true;
            default:
                return false;
        }
    }

    public boolean isHasLanguage() {
        switch (this) {
            case PROJECT:
            case IMAGE:
            case GEOSPATIAL:
            case SENSORY_DATA:
                return false;
            default:
                return true;
        }
    }

    public boolean isDataTableSupported() {
        switch (this) {
            case DATASET:
            case GEOSPATIAL: // ?
                // case SENSORY_DATA:
                return true;
            default:
                return false;
        }
    }

    public boolean isCompositeFilesEnabled() {
        switch (this) {
            case DATASET:
            case GEOSPATIAL: // ?
                // case SENSORY_DATA:
                return true;
            default:
                return false;
        }
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getPluralLocaleKey() {
        return MessageHelper.formatPluralLocalizableKey(this);
    }

    public static ResourceType[] getTypesSupportingBulkUpload() {
        List<ResourceType> types = new ArrayList<>();
        for (ResourceType type : values()) {
            if (type.supportBulkUpload()) {
                types.add(type);
            }
        }
        return types.toArray(new ResourceType[0]);
    }

    public static boolean isImageName(String value) {
        ResourceType type = ResourceType.fromString(value);
        if (type == null) {
            return false;
        }
        return type.hasDemensions();
    }
    
    public boolean allowsMultipleFIles() {
        switch (this) {
            case DOCUMENT:
            case IMAGE:
                return true;
            default:
                return false;
        }
    }
}
