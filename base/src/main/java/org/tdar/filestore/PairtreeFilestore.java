package org.tdar.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.exception.TdarRuntimeException;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * Implementation of {@link Filestore} on a locally accessible directory
 * location.
 * 
 * @author <a href="matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 */
public class PairtreeFilestore extends BaseFilestore {

    private static final String V = "v";
    private static final String XML = "xml";
    private static final String SUPPORT = "support";
    public static final String CONTAINER_NAME = "rec";
    public static final String DERIV = "deriv";
    public static final String ARCHIVAL = "archival";
    private static final Logger logger = LoggerFactory.getLogger(PairtreeFilestore.class);
    private final File baseStoreDirectory;
    private final String fileStoreLocation;

    /**
     * how many characters of the fileId to use per directory name
     */
    private final static int CHARACTERS_PER_LEVEL = 2;
    public static final String DELETED_SUFFIX = ".deleted";

    /**
     * @param pathToFilestore
     *            : The base directory on the filesystem where files are stored.
     * @throws IOException
     */
    public PairtreeFilestore(String pathToFilestore) {
        baseStoreDirectory = new File(pathToFilestore);
        String error = "Can not initialize " + pathToFilestore + " as the filestore location.";
        if (!baseStoreDirectory.exists()) {
            baseStoreDirectory.mkdirs();
        }
        if (!baseStoreDirectory.isDirectory()) {
            logger.error(error);
            throw new IllegalArgumentException(error);
        }
        try {
            fileStoreLocation = baseStoreDirectory.getCanonicalPath();
        } catch (IOException e) {
            logger.error(error, e);
            throw new TdarRuntimeException(error, e);
        }
    }

    @Override
    public String store(FilestoreObjectType type, InputStream content, FileStoreFileProxy version) throws IOException {
        StorageMethod rotate = StorageMethod.NO_ROTATION;
        return storeAndRotate(type, content, version, rotate);
    }

    /**
     * @see org.tdar.filestore.Filestore#store(java.io.InputStream)
     */
    @Override
    public String storeAndRotate(FilestoreObjectType type, InputStream content, FileStoreFileProxy version, StorageMethod rotate) throws IOException {
        OutputStream outputStream = null;
        String path = getAbsoluteFilePath(type, version);
        if (TdarConfiguration.getInstance().isFilestoreReadOnly()) {
            return null;
        }
        File outFile = new File(path);
        outFile = rotateFileIfNeeded(rotate, outFile);
        logFilestoreWrite(outFile);
        logger.info("storing at: {}", outFile.getAbsolutePath());
        String errorMessage = MessageHelper.getMessage("pairtreeFilestore.cannot_write", Arrays.asList(outFile.getAbsolutePath()));
        DigestInputStream digestInputStream = appendMessageDigestStream(content);
        try {
            FileUtils.forceMkdir(outFile.getParentFile());
            if (outFile.canWrite() || outFile.createNewFile()) {
                outputStream = new FileOutputStream(outFile);
                IOUtils.copy(digestInputStream, outputStream);
            } else {
                logger.error(errorMessage);
                throw new TdarRuntimeException(errorMessage);
            }

            if (version.getType() == FilestoreObjectType.RESOURCE) {
                updateVersionInfo(outFile, version);
            }

            MessageDigest digest = digestInputStream.getMessageDigest();
            if (StringUtils.isEmpty(version.getChecksum())) {
                version.setChecksumType(digest.getAlgorithm());
                version.setChecksum(formatDigest(digest));
            }
            version.setTransientFile(outFile);
            return outFile.getCanonicalPath();
        } catch (IOException iox) {
            // this exception may be swallowed if our finally block itself throws an exception, so we log it here first
            logger.error(errorMessage, iox);
            throw iox;
        } finally {
            IOUtils.closeQuietly(content);
            IOUtils.closeQuietly(digestInputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    public void logFilestoreWrite(File outFile) {
        logAction(outFile, "WRITE");
    }

    private void logAction(File outFile, String action) {
        File logFile = new File(baseStoreDirectory, FilestoreObjectType.LOG.getRootDir() + "/write.log");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
        String logLine = String.format("%s\t%s\t%s\n", sdf.format(new Date()), outFile.getAbsolutePath(), action);
        synchronized (logFile) {
            try {
                FileUtils.writeStringToFile(logFile, logLine, Charset.defaultCharset(), true);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    private File rotateFileIfNeeded(StorageMethod rotate, File outFile_) {
        File outFile = outFile_;
        if (outFile.exists() && (rotate.getRotations() > 0)) {
            rotate(outFile, rotate);
        }

        // this isn't really rotation
        if (rotate == StorageMethod.DATE) {
            String baseName = FilenameUtils.getBaseName(outFile.getName());
            String ext = FilenameUtils.getExtension(outFile.getName());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
            String rotationTarget = String.format("%s.%s.%s", baseName, sdf.format(new Date()), ext);
            outFile = new File(outFile.getParentFile(), rotationTarget);
        }
        return outFile;
    }

    public static String toPairTree(Number val) {
        String s = Long.toString(val.longValue());
        return toPairTree(s);
    }

    public static String toPairTree(String identifier) {
        String s = identifier;
        int i = 0;
        StringBuffer out = new StringBuffer(File.separator);
        while ((i + CHARACTERS_PER_LEVEL) < s.length()) {
            out.append(s.substring(i, i + CHARACTERS_PER_LEVEL));
            out.append(File.separator);
            i += CHARACTERS_PER_LEVEL;
        }

        if (i < s.length()) {
            out.append(s.substring(i));
            out.append(File.separator);
        }
        out.append(PairtreeFilestore.CONTAINER_NAME);
        out.append(File.separator);
        return out.toString();
    }

    /**
     * @see org.tdar.filestore.Filestore#store(File)
     */
    @Override
    public String store(FilestoreObjectType type, File content, FileStoreFileProxy version) throws IOException {
        return storeAndRotate(type, content, version, StorageMethod.NO_ROTATION);
    }

    /**
     * @see org.tdar.filestore.Filestore#store(File)
     */
    @Override
    public String storeAndRotate(FilestoreObjectType type, File content, FileStoreFileProxy version, StorageMethod rotations) throws IOException {
        if ((content == null) || !content.isFile()) {
            logger.warn("Trying to store null or non-file content: {}", content);
            return "";
        }
        return storeAndRotate(type, new FileInputStream(content), version, rotations);
    }

    /**
     * @see org.tdar.filestore.Filestore#retrieveFile(java.lang.String)
     */
    @Override
    public File retrieveFile(FilestoreObjectType type, FileStoreFileProxy version) throws FileNotFoundException {
        String absoluteFilePath = getAbsoluteFilePath(type, version);
        File file = new File(absoluteFilePath);
        logger.trace("file requested: {}", file);
        if (!file.isFile()) {
            throw new FileNotFoundException(MessageHelper.getMessage("error.file_not_found", Arrays.asList(file.getAbsolutePath())));
        }

        // version.setTransientFile(file);
        return file;
    }

    /**
     * Constructs the absolute path on the filesystem to the directory for a
     * file with the given fileId
     * 
     * Given a fileId of:
     * 
     * 363b29f92662a6c94273a46351c5f50
     * 
     * charactersPerLevel = 2, directoryLevels = 3, and baseStoreDirectory =
     * '/home/datastore' this would return:
     * 
     * '/home/datastore/36/3b/29/363b29f92662a6c94273a46351c5f50'
     * 
     * @param fileId
     * @return
     */
    public String getResourceDirPath(FilestoreObjectType type, Number fileId) {
        String filePath = getFilestoreLocation() + File.separator + type.getRootDir() + File.separator;
        filePath = FilenameUtils.normalize(filePath + toPairTree(fileId));
        return filePath;
    }

    /**
     * Constructs the absolute path to the file in the filestore with a given
     * fileId.
     * 
     * Given a fileId of:
     * 
     * 363b29f92662a6c94273a46351c5f50
     * 
     * and charactersPerLevel = 2 and directoryLevels = 3 and and
     * baseStoreDirectory = '/home/datastore' this would return:
     * 
     * /home/datastore/36/3b/29/363b29f92662a6c94273a46351c5f50
     * 
     * @param fileId
     * @return String
     */
    public String getAbsoluteFilePath(FilestoreObjectType type, FileStoreFileProxy version) {
        Long irID = version.getPersistableId();
        StringBuffer base = new StringBuffer();
        base.append(getResourceDirPath(type, irID));
        if (version.getType() == FilestoreObjectType.RESOURCE) {
            if (version.getInformationResourceFileId() != null && version.getInformationResourceFileId() != -1 ) {
                append(base, version.getInformationResourceFileId());
                append(base, V + version.getVersion());
                if (version.getVersionType().isArchival()) {
                    append(base, ARCHIVAL);
                } else if (!version.getVersionType().isUploaded()) {
                    append(base, DERIV);
                }
            }
        } else {
            if (version.getVersionType() == VersionType.METADATA && XML.equalsIgnoreCase(version.getExtension())) {
                append(base, SUPPORT);
            }
        }
        logger.trace("{} ({} {})", base, type, version);
        return FilenameUtils.concat(FilenameUtils.normalize(base.toString()), version.getFilename());
    }

    private void append(StringBuffer base, Object obj) {
        base.append(obj);
        base.append(File.separator);
    }

    /**
     * Constructs the relative path to the file in the filestore with a given
     * fileId.
     * 
     * Given a fileId of:
     * 
     * 363b29f92662a6c94273a46351c5f50
     * 
     * and charactersPerLevel = 2 and directoryLevels = 3 this would return:
     * 
     * 36/3b/29/363b29f92662a6c94273a46351c5f50
     * 
     * @param fileId
     * @return String
     */
    public String getRelativeFilePath(Number fileId) {
        return FilenameUtils.concat(toPairTree(fileId), Integer.toString(fileId.intValue()));
    }

    /**
     * @return Canonical path to the base filestore directory on the filesystem
     *         as a string.
     */
    @Override
    public String getFilestoreLocation() {
        return fileStoreLocation;
    }

    /**
     * @see org.tdar.filestore.Filestore#purge(java.lang.String)
     */
    @Override
    public void purge(FilestoreObjectType type, FileStoreFileProxy version) throws IOException {
        File file = new File(getAbsoluteFilePath(type, version));
        logFilestoreDelete(file);
        if (version.getType() == FilestoreObjectType.RESOURCE) {
            if (version.getVersionType().isDerivative() || version.getVersionType() == VersionType.TRANSLATED) {
                FileUtils.deleteQuietly(file);
                cleanEmptyParents(file.getParentFile());
            } else {
                try {
                    // if archival, need to go up one more
                    if (version.getVersionType().isArchival()) {
                        file = file.getParentFile();
                    }
                    File parentFile = file.getParentFile();
                    String canonicalPath = parentFile.getCanonicalPath();
                    File deletedFile = new File(canonicalPath + DELETED_SUFFIX);

                    logger.debug("renaming: {} ==> {}", parentFile.getAbsolutePath(), deletedFile.getAbsoluteFile());
                    FileUtils.moveDirectory(parentFile, deletedFile);
                    return;
                } catch (Exception e) {
                    logger.warn("cannot purge file", e);
                    return;
                }
            }
        } else {
            FileUtils.deleteQuietly(file);
            cleanEmptyParents(file.getParentFile());
        }
    }

    @Override
    public void logFilestoreDelete(File file) {
        logAction(file, "DELETE");
    }

    /**
     * Recursively check to see if the parent directories are empty and, if so,
     * delete them.
     * 
     * @param {@link
     *            File} representing the directory to clean.
     * @throws {@link
     *             IOException}
     */
    private void cleanEmptyParents(File dir) throws IOException {
        if (dir == null) {
            return;
        }
        if (dir.exists() && (dir.list().length == 0)) {
            FileUtils.deleteDirectory(dir);
            cleanEmptyParents(dir.getParentFile());
        }
    }

    @Override
    public void markReadOnly(FilestoreObjectType type, List<FileStoreFileProxy> filesToProcess) {
        for (FileStoreFileProxy version : filesToProcess) {
            String absoluteFilePath = getAbsoluteFilePath(type, version);
            File file = new File(absoluteFilePath);
            if (version.getVersionType().isUploaded() || version.getVersionType().isArchival()) {
                file.setWritable(false);
            }
        }
    }

    @Override
    public File getDirectory(FilestoreObjectType type, Long persistableId) {
        return new File(getResourceDirPath(type, persistableId));
    }

}
