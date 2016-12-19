package org.tdar.core.bean.collection;

import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.utils.MessageHelper;

public enum CollectionType implements Localizable, PluralLocalizable {
    INTERNAL("Internal"), SHARED("Shared"), LIST("Public");

    private String label;

    private CollectionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
    
    public static <C extends ResourceCollection> CollectionType getTypeForClass(Class<C> cls) {
        if (cls.isAssignableFrom(SharedCollection.class)) {
            return SHARED;
        }
        if (cls.isAssignableFrom(ListCollection.class)) {
            return LIST;
        }
        if (cls.isAssignableFrom(InternalCollection.class)) {
            return INTERNAL;
        }
        return null;
    }

    @Override
    public String getPluralLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatPluralLocalizableKey(this);
    }

    public Class<? extends ResourceCollection> getClassForType() {
        switch (this) {
            case INTERNAL:
                return  InternalCollection.class;
            case LIST:
                return ListCollection.class;
            case SHARED:
                return SharedCollection.class;
        }
        return null;
    }


    public String getUrlNamespace() {
        if(this==INTERNAL) {return "invalid";}
        return this == SHARED ? "share" : "collection";
    }
}
