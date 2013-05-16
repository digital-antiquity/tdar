package org.tdar.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.filestore.Filestore.BaseFilestore;

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
    private static final String FILENAME_SANITIZE_REGEX = "([\\W&&[^\\s\\-\\+\\.]])";

    /**
     * @param pathToFilestore
     *            : The base directory on the filesystem where files are stored.
     * @throws IOException
     */
    public PairtreeFilestore(String pathToFilestore) {
        baseStoreDirectory = new File(pathToFilestore);
        String error = "Can not initialize " + pathToFilestore + " as the filestore location.";
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
    
    public String store(InputStream content, InformationResourceFileVersion version) throws IOException {
        StorageMethod rotate = StorageMethod.NO_ROTATION;
        return storeAndRotate(content, version, rotate);
    }

    /**
     * @see org.tdar.filestore.Filestore#store(java.io.InputStream)
     */
    public String storeAndRotate(InputStream content, InformationResourceFileVersion version, StorageMethod rotate) throws IOException {
        OutputStream outputStream = null;
        String path = getAbsoluteFilePath(version);

        File outFile = new File(path);
        if (outFile.exists() && rotate.getRotations() > 0) {
            rotate(outFile, rotate);
        }
        
        if (rotate == StorageMethod.DATE) {
            String baseName = FilenameUtils.getBaseName(outFile.getName());
            String ext = FilenameUtils.getExtension(outFile.getName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
            String rotationTarget = String.format("%s.%s.%s", baseName, sdf.format(new Date()), ext);
            outFile = new File(outFile.getParentFile(), rotationTarget);

        }
        logger.info("storing at: {}", outFile.getAbsolutePath());
        String errorMessage = "Unable to write content to filestore.";
        DigestInputStream digestInputStream = appendMessageDigestStream(content);
        try {
            FileUtils.forceMkdir(outFile.getParentFile());
            if (outFile.canWrite() || outFile.createNewFile()) {
                outputStream = new FileOutputStream(outFile);
                IOUtils.copy(digestInputStream, outputStream);
            } else {
                logger.error(errorMessage);
                throw new TdarRuntimeException(errorMessage + "Can't write to: " + outFile.getAbsolutePath());
            }
            updateVersionInfo(outFile, version);
            MessageDigest digest = digestInputStream.getMessageDigest();
            if (StringUtils.isEmpty(version.getChecksum())) {
                version.setChecksumType(digest.getAlgorithm());
                version.setChecksum(formatDigest(digest));
            }
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

    public static String toPairTree(Number val) {
        String s = Long.toString(val.longValue());
        int i = 0;
        StringBuffer out = new StringBuffer(File.separator);
        while (i + CHARACTERS_PER_LEVEL < s.length()) {
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
    public String store(File content, InformationResourceFileVersion version) throws IOException {
        return storeAndRotate(content, version, StorageMethod.NO_ROTATION);
    }

    /**
     * @see org.tdar.filestore.Filestore#store(File)
     */
    public String storeAndRotate(File content, InformationResourceFileVersion version, StorageMethod rotations) throws IOException {
        if (content == null || !content.isFile()) {
            logger.warn("Trying to store null or non-file content: {}", content);
            return "";
        }
        return storeAndRotate(new FileInputStream(content), version, rotations);
    }

    /**
     * @see org.tdar.filestore.Filestore#retrieveFile(java.lang.String)
     */
    public File retrieveFile(InformationResourceFileVersion version) throws FileNotFoundException {
        File file = new File(getAbsoluteFilePath(version));
        logger.trace("file requested: {}", file);
        if (!file.isFile())
            throw new FileNotFoundException("Could not find file: " + file.getAbsolutePath());
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
    public String getResourceDirPath(Number fileId) {
        String filePath = getFilestoreLocation() + File.separator;
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
    public String getAbsoluteFilePath(InformationResourceFileVersion version) {
        Long irID = version.getInformationResourceId();
        StringBuffer base = new StringBuffer();
        base.append(getResourceDirPath(irID));
        if (version.getInformationResourceFileId() != null) {
            base.append(version.getInformationResourceFileId());
            base.append(File.separator);
            base.append("v" + version.getVersion());
            base.append(File.separator);
            if (version.isArchival()) {
                base.append(ARCHIVAL);
                base.append(File.separator);
            } else if (!version.isUploaded()) {
                base.append(DERIV);
                base.append(File.separator);
            }
        }
        logger.trace("{}", base);
        return FilenameUtils.concat(FilenameUtils.normalize(base.toString()), version.getFilename());
    }

    /**
     * Determines the correct subdirectories under baseStoreDirectory where the
     * file with filename (fileId) should live given the charactersPerLevel and
     * directoryLevels.
     * 
     * Given a fileId of:
     * 
     * 363b29f92662a6c94273a46351c5f50
     * 
     * and charactersPerLevel = 2 and directoryLevels = 3 this would return:
     * 
     * 36/3b/29
     * 
     * @param fileId
     * @return String
     */

    public static String sanitizeFilename(String filename) {
        return filename.replaceAll(FILENAME_SANITIZE_REGEX, "_");
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
    public String getFilestoreLocation() {
        return fileStoreLocation;
    }

    /**
     * @see org.tdar.filestore.Filestore#purge(java.lang.String)
     */
    public void purge(InformationResourceFileVersion version) throws IOException {
        File file = new File(getAbsoluteFilePath(version));
        if (!version.isDerivative() && !version.isTranslated()) {
            try {
                // if archival, need to go up one more
                if (version.isArchival())
                    file = file.getParentFile();
                logger.debug("renaming: {} -> {}", 
                        file.getParentFile().getAbsolutePath(),
                        new File(file.getParentFile().getCanonicalPath() + DELETED_SUFFIX).getAbsoluteFile());
                FileUtils.moveDirectory(file.getParentFile(), new File(file.getParentFile().getCanonicalPath() + DELETED_SUFFIX));
            } catch (Exception e) {
                logger.warn("cannot purge file", e);
            }
        } else {
            FileUtils.deleteQuietly(file);
            cleanEmptyParents(file.getParentFile());
        }
    }

    /**
     * Recursively check to see if the parent directories are empty and, if so,
     * delete them.
     * 
     * @param {@link File} representing the directory to clean.
     * @throws {@link IOException}
     */
    private void cleanEmptyParents(File dir) throws IOException {
        if (dir == null)
            return;
        if (dir.exists() && dir.list().length == 0) {
            FileUtils.deleteDirectory(dir);
            cleanEmptyParents(dir.getParentFile());
        }
    }

}
