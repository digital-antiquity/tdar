package org.tdar.core.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.PairtreeFilestore;

@Component
public class FileSystemResourceDao {

    @Autowired
    ResourceLoader resourceLoader;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String TESTING_PATH_FOR_INCLUDES_DIRECTORY = "target/tdar-web/";

    // helper to load the PDF Template for the cover page
    public File loadTemplate(String path) throws IOException, FileNotFoundException {
        Resource resource = resourceLoader.getResource(path);
        File template = null;
        if (resource.exists()) {
            template = resource.getFile();
        } else {
            resource = resourceLoader.getResource(".");
            String parent = resource.getFile().getParent();
            logger.debug("{} {}", resource, parent);
            // TO SUPPORT TESTING
            template = new File(TESTING_PATH_FOR_INCLUDES_DIRECTORY + path);
            logger.debug("{}", path);
            if (!template.exists()) {
                throw new FileNotFoundException(template.getAbsolutePath());
            }
        }
        return template;
    }

    public boolean checkHostedFileAvailable(String filename, FilestoreObjectType type, Long id) {
        if (getHostedFile(filename, type, id) != null) {
            return true;
        }

        return false;
    }

    public File getHostedFile(String filename, FilestoreObjectType type, Long id) {
        File baseFolder = new File(TdarConfiguration.getInstance().getHostedFileStoreLocation());
        File pairTreeRoot = new File(baseFolder, PairtreeFilestore.toPairTree(id));
        File file = new File(pairTreeRoot, filename);
        logger.trace(file.getAbsolutePath());
        if (file.exists()) {
            return file;
        }
        return null;
    }

}
