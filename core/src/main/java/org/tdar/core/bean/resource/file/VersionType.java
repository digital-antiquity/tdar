package org.tdar.core.bean.resource.file;

import java.util.ArrayList;
import java.util.List;

/**
 * Types of InformationResourceFileVersions
 * 
 * @version $Rev$
 */
public enum VersionType {
    UPLOADED,
    UPLOADED_TEXT,
    UPLOADED_ARCHIVAL,
    ARCHIVAL,
    WEB_SMALL,
    WEB_MEDIUM,
    WEB_LARGE,
    GEOJSON,
    TRANSLATED,
    INDEXABLE_TEXT,
    METADATA,
    LOG,
    RECORD;

    public boolean isDerivative() {
        switch (this) {
            case INDEXABLE_TEXT:
            case WEB_SMALL:
            case WEB_MEDIUM:
            case WEB_LARGE:
            case METADATA:
            case GEOJSON:
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

    public boolean isUploaded() {
        return ((this == VersionType.UPLOADED) || (this == VersionType.UPLOADED_ARCHIVAL));
    }

    public boolean isArchival() {
        return ((this == VersionType.ARCHIVAL) || (this == VersionType.UPLOADED_ARCHIVAL));
    }

    public static VersionType forName(String version) {
        if (version == null) {
            return null;
        }
        switch (version.toLowerCase()) {
            case "sm":
                return VersionType.WEB_SMALL;
            case "md":
                return VersionType.WEB_MEDIUM;
            case "lg":
                return VersionType.WEB_LARGE;
        }
        return null;
    }

    public String toPath() {
        switch (this) {
            case WEB_LARGE:
                return "_lg";
            case WEB_SMALL:
                return "_sm";
            case WEB_MEDIUM:
                return "_md";
            default:
                return "";
        }
    }

}