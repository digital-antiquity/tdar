package org.tdar.search.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.utils.MessageHelper;

public enum ObjectType implements HasLabel, Localizable, PluralLocalizable {
    DOCUMENT, DATASET, PROJECT, LIST_COLLECTION, SHARED_COLLECTION, CODING_SHEET, IMAGE, GEOSPATIAL, SENSORY_DATA, ONTOLOGY, VIDEO, AUDIO, ARCHIVE, INTEGRATION;

    public static ObjectType from(ResourceType resourceType) {
        switch (resourceType) {
            case ARCHIVE:
                return ObjectType.ARCHIVE;
            case AUDIO:
                return AUDIO;
            case CODING_SHEET:
                return ObjectType.CODING_SHEET;
            case DATASET:
                return DATASET;
            case DOCUMENT:
                return ObjectType.DOCUMENT;
            case GEOSPATIAL:
                return ObjectType.GEOSPATIAL;
            case IMAGE:
                return IMAGE;
            case ONTOLOGY:
                return ObjectType.ONTOLOGY;
            case PROJECT:
                return PROJECT;
            case SENSORY_DATA:
                return SENSORY_DATA;
            case VIDEO:
                return ObjectType.VIDEO;
        }
        return null;
    }

    public String getSortName() {
        switch (this) {
            case ARCHIVE:
                return "100" + this.name();
            case AUDIO:
                return "095" + this.name();
            case CODING_SHEET:
                return "095" + this.name();
            case DATASET:
                return "003" + this.name();
            case DOCUMENT:
                return "001" + this.name();
            case GEOSPATIAL:
                return "006" + this.name();
            case IMAGE:
                return "002" + this.name();
            case LIST_COLLECTION:
                return "050" + this.name();
            case ONTOLOGY:
                return "009" + this.name();
            case PROJECT:
                return "005" + this.name();
            case SENSORY_DATA:
                return "007" + this.name();
            case SHARED_COLLECTION:
                return "010" + this.name();
            case INTEGRATION:
                return "040" + this.name();
            case VIDEO:
                return "099" + this.name();
            default:
                break;
        }
        return null;
    }

    public static ObjectType from(CollectionResourceSection type) {
        switch (type) {
            case UNMANGED:
                return LIST_COLLECTION;
            case MANAGED:
                return SHARED_COLLECTION;
            default:
                break;
        }
        return null;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getPluralLocaleKey() {
        return MessageHelper.formatPluralLocalizableKey(this);
    }

    @Override
    public String getLabel() {
        return MessageHelper.getMessage(getLocaleKey());
    }

    public static List<ObjectType> resourceValues() {
        List<ObjectType> lst = new ArrayList<>(Arrays.asList(ObjectType.values()));
        lst.remove(SHARED_COLLECTION);
        lst.remove(LIST_COLLECTION);
        lst.remove(INTEGRATION);
        return lst;
    }
}
