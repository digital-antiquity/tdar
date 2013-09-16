package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.List;

/**
 * $Id$
 * 
 * 
 * @version $Rev$
 */
public enum VersionType {
    UPLOADED, UPLOADED_TEXT, UPLOADED_ARCHIVAL, ARCHIVAL, WEB_SMALL, WEB_MEDIUM, WEB_LARGE,
    TRANSLATED, INDEXABLE_TEXT, METADATA, LOG, RECORD;

    public boolean isDerivative() {
        switch (this) {
            case INDEXABLE_TEXT:
            case WEB_SMALL:
            case WEB_MEDIUM:
            case WEB_LARGE:
            case METADATA:
            case TRANSLATED:
                return true;
            default:
                return false;
        }
    }

    public static List<VersionType> getDerivativeVersionTypes() {
        ArrayList<VersionType> derivativeTypes = new ArrayList<VersionType>();
        for (VersionType type : values()) {
            if (type.isDerivative()) {
                derivativeTypes.add(type);
            }
        }
        return derivativeTypes;
    }
}