package org.tdar.search.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.utils.MessageHelper;

public enum ObjectType implements HasLabel, Localizable, PluralLocalizable {
    DOCUMENT, DATASET, PROJECT, LIST_COLLECTION, SHARED_COLLECTION, CODING_SHEET, IMAGE, GEOSPATIAL, SENSORY_DATA, ONTOLOGY, VIDEO, AUDIO, ARCHIVE;

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
        return null;
    }

    public static ObjectType from(CollectionType type) {
        switch (type) {
            case INTERNAL:
                break;
            case LIST:
                return LIST_COLLECTION;
            case SHARED:
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
        return lst;
    }
}
