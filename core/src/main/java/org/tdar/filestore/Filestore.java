package org.tdar.filestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.List;

public interface Filestore {

    static final String FILENAME_SANITIZE_REGEX = "([\\W&&[^\\s\\-\\+\\.]])";

    public enum StorageMethod {
        NO_ROTATION,
        ROTATE,
        DATE;
        private int rotations = 5;

        public int getRotations() {
            if (this != ROTATE) {
                return 0;
            }
            return rotations;
        }

    }

    public enum LogType {
        FILESTORE_VERIFICATION("verify"),
        AUTHORITY_MANAGEMENT("authmgmt"),
        OTHER("other");
        private String dir;

        private LogType(String dir) {
            this.dir = dir;
        }

        public String getDir() {
            return dir;
        }
    }

    /**
     * Write content to the filestore.
     * 
     * @param {@link
     *            InputStream} The content to be stored.
     * @return {@link String} the fileId assigned to the content
     * @throws {@link
     *             IOException}
     */
    String store(FilestoreObjectType type, InputStream content, FileStoreFileProxy object) throws IOException;

    File getXmlRecordFile(FilestoreObjectType type, Long persistableId, String filename);

    Collection<File> listXmlRecordFiles(FilestoreObjectType type, Long persistableId);

    long getSizeInBytes();

    String getSizeAsReadableString();

    File getLogFile(LogType type, Integer year, String filename);

    List<File> listLogFiles(LogType type, Integer year);

    /**
     * Write content to the filestore.
     * 
     * @param {@link
     *            InputStream} The content to be stored.
     * @return {@link String} the fileId assigned to the content
     * @throws {@link
     *             IOException}
     */
    String storeAndRotate(FilestoreObjectType type, InputStream content, FileStoreFileProxy object, StorageMethod rotation) throws IOException;

    /**
     * Write a file to the filestore.
     * 
     * @param {@link
     *            File} The file to be stored in the filestore.
     * @return {@link String} the fileId assigned to the content
     * @throws {@link
     *             IOException}
     */
    String store(FilestoreObjectType type, File content, FileStoreFileProxy version) throws IOException;

    String storeAndRotate(FilestoreObjectType type, File content, FileStoreFileProxy object, StorageMethod rotation) throws IOException;

    void storeLog(LogType type, String filename, String message);

    /**
     * Retrieve the file with the given ID from the store.
     * 
     * @param fileId
     *            file identifier
     * @return {@link File} associated with the given ID.
     * @throws {@link
     *             FileNotFoundException }
     */
    File retrieveFile(FilestoreObjectType type, FileStoreFileProxy object) throws FileNotFoundException;

    /**
     * Delete the file with the given fileId.
     * 
     * @param fileId
     *            file identifier
     * @throws {@link
     *             IOException }
     */
    void purge(FilestoreObjectType type, FileStoreFileProxy object) throws IOException;

    /**
     * Get the current filestore location
     * 
     * @return
     */
    String getFilestoreLocation();

    /**
     * create a MD5
     * 
     * @param f
     * @return
     */
    MessageDigest createDigest(File f);

    /**
     * verify a file in the filestore
     * 
     * @param type
     * @param object
     * @return
     * @throws FileNotFoundException
     * @throws TaintedFileException
     */
    boolean verifyFile(FilestoreObjectType type, FileStoreFileProxy object) throws FileNotFoundException, TaintedFileException;

    /**
     * Mark a file as read only
     * 
     * @param type
     * @param filesToProcess
     */
    void markReadOnly(FilestoreObjectType type, List<FileStoreFileProxy> filesToProcess);

    /**
     * Returns the directory for a persistable (to list log files)
     * 
     * @param type
     * @param persistableId
     * @return
     */
    File getDirectory(FilestoreObjectType type, Long persistableId);

    /**
     * logs the write to the filestore
     * 
     * @param outFile
     */
    void logFilestoreWrite(File outFile);

    /**
     * logs the deletion of a file
     * 
     * @param file
     */
    void logFilestoreDelete(File file);
}