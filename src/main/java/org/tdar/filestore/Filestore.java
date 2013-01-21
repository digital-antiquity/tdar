package org.tdar.filestore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarRuntimeException;

public interface Filestore {

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
     * @param {@link InputStream} The content to be stored.
     * @return {@link String} the fileId assigned to the content
     * @throws {@link IOException}
     */
    public abstract String store(InputStream content, InformationResourceFileVersion version) throws IOException;

    long getSizeInBytes();

    String getSizeAsReadableString();

    File getLogFile(LogType type, Integer year, String filename);

    List<File> listLogFiles(LogType type, Integer year);

    /**
     * Write content to the filestore.
     * 
     * @param {@link InputStream} The content to be stored.
     * @return {@link String} the fileId assigned to the content
     * @throws {@link IOException}
     */
    public abstract String storeAndRotate(InputStream content, InformationResourceFileVersion version, int maxRotations) throws IOException;

    /**
     * Write a file to the filestore.
     * 
     * @param {@link File} The file to be stored in the filestore.
     * @return {@link String} the fileId assigned to the content
     * @throws {@link IOException}
     */
    public abstract String store(File content, InformationResourceFileVersion version) throws IOException;

    public abstract String storeAndRotate(File content, InformationResourceFileVersion version, int maxRotations) throws IOException;

    public abstract void storeLog(LogType type, String filename, String message);

    /**
     * Retrieve the file with the given ID from the store.
     * 
     * @param fileId
     *            file identifier
     * @return {@link File} associated with the given ID.
     * @throws {@link FileNotFoundException }
     */
    public abstract File retrieveFile(InformationResourceFileVersion version) throws FileNotFoundException;

    /**
     * Delete the file with the given fileId.
     * 
     * @param fileId
     *            file identifier
     * @throws {@link IOException }
     */
    public abstract void purge(InformationResourceFileVersion version) throws IOException;

    public abstract String getFilestoreLocation();

    public abstract MessageDigest createDigest(File f);

    public abstract boolean verifyFile(InformationResourceFileVersion version) throws FileNotFoundException, TaintedFileException;

    public abstract static class BaseFilestore implements Filestore {
        private static final String LOG_DIR = "logs";
        // protected static final MimeTypes mimes = TikaConfig.getDefaultConfig().getMimeRepository();
        protected static final Logger logger = LoggerFactory.getLogger(BaseFilestore.class);

        /*
         * This method extracts out the MimeType from the file using Tika, the previous version tried to parse the file
         * but this doesn't need to be so complex.
         * http://stackoverflow.com/questions/7137634/getting-mimetype-subtype-with-apache-tika
         */
        public static String getContentType(File file, String overrideValue) {
            MediaType mediaType = null;
            Metadata md = new Metadata();
            md.set(Metadata.RESOURCE_NAME_KEY, file.getName());
            Detector detector = new DefaultDetector(TikaConfig.getDefaultConfig().getMimeRepository());
            try {
                FileInputStream fis = new FileInputStream(file);
                mediaType = detector.detect(new BufferedInputStream(fis), md); // bufferedStream so we can move
                fis.close();
            } catch (IOException ioe) {
                logger.debug("error", ioe);
            }
            if (mediaType == null) {
                return overrideValue;
            }
            return mediaType.getType() + "/" + mediaType.getSubtype();
        }

        protected InformationResourceFileVersion updateVersionInfo(File file, InformationResourceFileVersion version) throws IOException {
            String mimeType = getContentType(file, "UNKNOWN/UNKNOWN");
            logger.trace("MIMETYPE: {}", mimeType);
            if (StringUtils.isEmpty(version.getFilename()))
                version.setFilename(file.getName());
            if (StringUtils.isEmpty(version.getMimeType()))
                version.setMimeType(mimeType);
            String relative = getRelativePath(file);
            File parent = getParentDirectory(new File(relative));
            if (StringUtils.isEmpty(version.getPath()))
                version.setPath(parent.getPath());
            if (version.getFileLength() == null)
                version.setFileLength(file.length());
            if (version.getUncompressedSizeOnDisk() == null)
                version.setUncompressedSizeOnDisk(file.length());
            if (StringUtils.isEmpty(version.getChecksum())) {
                MessageDigest digest = createDigest(file);
                version.setChecksumType(digest.getAlgorithm());
                version.setChecksum(formatDigest(digest));
                if (version.isArchival() || version.isUploaded()) {
                    File checksum = new File(file.getParentFile(), String.format("%s.%s", file.getName(), digest.getAlgorithm()));
                    FileUtils.write(checksum, version.getChecksum());
                }
            }
            if (version.getDateCreated() == null)
                version.setDateCreated(new Date());

            if (StringUtils.isEmpty(version.getExtension()))
                version.setExtension(FilenameUtils.getExtension(file.getName()));
            return version;
        }

        protected String formatDigest(MessageDigest digest) {
            return Hex.encodeHexString(digest.digest());
        }

        public void storeLog(LogType type, String filename, String message) {
            File logdir = new File(FilenameUtils.concat(getFilestoreLocation(),
                    String.format("%s/%s/%s", LOG_DIR, type.getDir(), Calendar.getInstance().get(Calendar.YEAR))));
            if (!logdir.exists()) {
                logdir.mkdirs();
            }
            File logFile = new File(logdir, filename);
            try {
                FileUtils.writeStringToFile(logFile, message);
            } catch (IOException e) {
                logger.error("Unable to write logfile", e);
            }
        }

        public List<File> listLogFiles(LogType type, Integer year) {
            String subdir = String.format("%s/%s", LOG_DIR, type.getDir());
            if (year != null) {
                subdir = String.format("%s/%s/%s", LOG_DIR, type.getDir(), year);
            }
            File logDir = new File(FilenameUtils.concat(getFilestoreLocation(), subdir));
            return Arrays.asList(logDir.listFiles());
        }

        public File getLogFile(LogType type, Integer year, String filename) {
            String subdir = String.format("%s/%s/%s/%s", LOG_DIR, type.getDir(), year, filename);
            File logDir = new File(FilenameUtils.concat(getFilestoreLocation(), subdir));
            return logDir;
        }

        public boolean verifyFile(InformationResourceFileVersion version) throws FileNotFoundException {
            File toVerify = retrieveFile(version);
            MessageDigest newDigest = createDigest(toVerify);
            String hex = formatDigest(newDigest);
            logger.debug("Verifying file: {}", version.getFilename());
            logger.trace("\told: {} new: {}", version.getChecksum(), hex);
            return hex.trim().equalsIgnoreCase(version.getChecksum().trim());
        }

        public DigestInputStream appendMessageDigestStream(InputStream content) {
            DigestInputStream digestInputStream = null;
            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance("MD5");
                digestInputStream = new DigestInputStream(content, messageDigest);
            } catch (NoSuchAlgorithmException e) {
                String error = "MD5 does not appear to be a valid digest format in this environment.";
                logger.error(error, e);
                throw new TdarRuntimeException(error, e);
            }
            return digestInputStream;
        }

        public MessageDigest createDigest(File f) {
            DigestInputStream digestInputStream = null;
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(f);
                digestInputStream = appendMessageDigestStream(stream);
                byte[] b = new byte[1000];
                while (digestInputStream.read(b) != -1) {
                    // read file
                }
                return digestInputStream.getMessageDigest();
            } catch (IOException e) {
                throw new TdarRecoverableRuntimeException(e);
            } finally {
                IOUtils.closeQuietly(stream);
                IOUtils.closeQuietly(digestInputStream);
            }

        }

        File getParentDirectory(File outputFile) {
            return new File(outputFile.getParent());
        }

        private String getRelativePath(File f) {
            return new File(getFilestoreLocation()).toURI().relativize(f.toURI()).getPath();
        }

        public long getSizeInBytes() {
            return FileUtils.sizeOfDirectory(new File(getFilestoreLocation()));
        }

        public String getSizeAsReadableString() {
            return FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(new File(getFilestoreLocation())));
        }
}

}