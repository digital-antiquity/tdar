package org.tdar.core.service;

import java.io.File;
import java.io.IOException;

import org.tdar.filestore.FilestoreObjectType;

public interface FileSystemResourceService {

    // helper to load the PDF Template for the cover page
    File loadTemplate(String path) throws IOException;

    boolean checkHostedFileAvailable(String filename, FilestoreObjectType type, Long id);

}