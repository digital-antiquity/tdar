package org.tdar.core.bean.resource.file;

public enum FileStatus {
    // whether or not the file is in the middle of a queued process
    QUEUED,
    // whether or not this InformationResourceFile has been converted into postgres
    PROCESSED, PROCESSING_ERROR, PROCESSING_WARNING;
}
