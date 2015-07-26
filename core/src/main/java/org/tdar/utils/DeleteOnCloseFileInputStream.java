package org.tdar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRuntimeException;

/*
 * from: http://stackoverflow.com/questions/4693968/is-there-an-existing-fileinputstream-delete-on-close
 * with slight modifications to ensure we don't delete things out of our filestore
 */
public class DeleteOnCloseFileInputStream extends FileInputStream {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private File file;

    public DeleteOnCloseFileInputStream(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }

    public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (file != null) {
                if (!FilenameUtils.normalize(file.getAbsolutePath()).contains(FilenameUtils.normalize(TdarConfiguration.getInstance().getFileStoreLocation()))) {
                    logger.debug("deleting temp file: {}", file);
                    file.delete();
                    file = null;
                } else {
                    logger.error("trying to delete temp file in FILESTORE!!!!!!: {}", file);
                    throw new TdarRuntimeException("deleteOnCloseFileInputStream.cannot_delete_file", Arrays.asList(file.getCanonicalPath()));
                }
            }
        }
    }
}