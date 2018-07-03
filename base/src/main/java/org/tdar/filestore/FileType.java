package org.tdar.filestore;

public enum FileType {
    IMAGE,
    DOCUMENT,
    COLUMNAR_DATA,
    FILE_ARCHIVE,
    GEOSPATIAL,
    AUDIO,
    VIDEO,
    OTHER;

    public boolean isComposite() {
        switch (this) {
            case COLUMNAR_DATA:
            case GEOSPATIAL:
                return true;
            default:
                return false;
        }
    }
}
