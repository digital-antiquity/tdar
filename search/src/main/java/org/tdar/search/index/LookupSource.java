package org.tdar.search.index;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.datatable.DataTableRow;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.search.service.CoreNames;
import org.tdar.utils.MessageHelper;

@SuppressWarnings("unchecked")
public enum LookupSource implements HasLabel, Localizable {
    PERSON("people", Person.class), INSTITUTION("institutions", Institution.class), KEYWORD("items",
            CultureKeyword.class, GeographicKeyword.class, InvestigationType.class, MaterialKeyword.class,
            OtherKeyword.class, TemporalKeyword.class, SiteNameKeyword.class,
            SiteTypeKeyword.class), RESOURCE("resources", Resource.class), COLLECTION("collections", ResourceCollection.class), INTEGRATION("integrations",
                    DataIntegrationWorkflow.class), RESOURCE_ANNOTATION_KEY("annotationKeys",
                            ResourceAnnotationKey.class), CONTENTS("content", InformationResourceFile.class), DATA("data", DataTableRow.class);

    private String collectionName;
    private Class<? extends Indexable>[] classes;

    private LookupSource(String name, Class<? extends Indexable>... classes) {
        this.collectionName = name;
        this.classes = classes;
    }

    @Override
    public String getLabel() {
        return collectionName;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getProper() {
        return StringUtils.capitalize(name().toLowerCase());
    }

    public Class<? extends Indexable>[] getClasses() {
        return classes;
    }

    public String getCoreName() {
        switch (this) {
            case INSTITUTION:
                return CoreNames.INSTITUTIONS;
            case KEYWORD:
                return CoreNames.KEYWORDS;
            case PERSON:
                return CoreNames.PEOPLE;
            case COLLECTION:
            case RESOURCE:
            case INTEGRATION:
                return CoreNames.RESOURCES;
            case RESOURCE_ANNOTATION_KEY:
                return CoreNames.ANNOTATION_KEY;
            case CONTENTS:
                return CoreNames.CONTENTS;
            case DATA:
                return CoreNames.DATA_MAPPINGS;
        }
        return null;
    }

    public static String getCoreForClass(Class<? extends Indexable> item) {
        if (Person.class.isAssignableFrom(item)) {
            return CoreNames.PEOPLE;
        }
        if (Institution.class.isAssignableFrom(item)) {
            return CoreNames.INSTITUTIONS;
        }
        if (Resource.class.isAssignableFrom(item)) {
            return CoreNames.RESOURCES;
        }
        if (ResourceCollection.class.isAssignableFrom(item)) {
            return CoreNames.RESOURCES;
        }
        if (DataIntegrationWorkflow.class.isAssignableFrom(item)) {
            return CoreNames.RESOURCES;
        }
        if (Keyword.class.isAssignableFrom(item)) {
            return CoreNames.KEYWORDS;
        }
        if (ResourceAnnotationKey.class.isAssignableFrom(item)) {
            return CoreNames.ANNOTATION_KEY;
        }
        if (InformationResourceFile.class.isAssignableFrom(item)) {
            return CoreNames.CONTENTS;
        }
        if (DataTableRow.class.isAssignableFrom(item)) {
            return CoreNames.DATA_MAPPINGS;
        }
        return null;
    }

    /**
     * because resource and collection share an index, the query isn't *:* but type:{}
     * 
     * @return
     */
    public String getDeleteQuery() {
        switch (this) {
            case COLLECTION:
            case RESOURCE:
            case INTEGRATION:
                return String.format("%s:%s", "type", this.name());
            default:
                return "*:*";
        }
    }

}