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
import java.util.Date;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarRuntimeException;

public interface Filestore {

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

    public abstract void storeLog(String filename, String message);

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
        // protected static final MimeTypes mimes = TikaConfig.getDefaultConfig().getMimeRepository();
        protected static final Logger logger = Logger.getLogger(BaseFilestore.class);

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
            logger.info("MIMETYPE:" + mimeType);
            if (StringUtils.isEmpty(version.getFilename()))
                version.setFilename(file.getName());
            if (StringUtils.isEmpty(version.getMimeType()))
                version.setMimeType(mimeType);
            String relative = getRelativePath(file);
            File parent = getParentDirectory(new File(relative));
            if (StringUtils.isEmpty(version.getPath()))
                version.setPath(parent.getPath());
            if (version.getSize() == null)
                version.setSize(file.length());
            if (StringUtils.isEmpty(version.getChecksum())) {
                MessageDigest digest = createDigest(file);
                version.setChecksumType(digest.getAlgorithm());
                version.setChecksum(formatDigest(digest));
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

        public void storeLog(String filename, String message) {
            File logdir = new File(FilenameUtils.concat(getFilestoreLocation(), "logs"));
            if (!logdir.exists()) {
                logdir.mkdirs();
            }
            File logFile = new File(logdir, filename);
            try {
                FileUtils.writeStringToFile(logFile, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean verifyFile(InformationResourceFileVersion version) throws FileNotFoundException, TaintedFileException {
            File toVerify = retrieveFile(version);
            MessageDigest newDigest = createDigest(toVerify);
            String hex = formatDigest(newDigest);
            logger.debug("Verifying file: " + version.getFilename());
            logger.trace("\told: " + version.getChecksum());
            logger.info(version.getChecksum());
            logger.trace("\tnew: " + hex + " (" + hex.trim().equalsIgnoreCase(version.getChecksum().trim()) + ")");
            if (hex.trim().equalsIgnoreCase(version.getChecksum().trim())) {
                return true;
            } else {
                throw new TaintedFileException("Digest for " + version.getFilename() + " does not match the checksum stored for it");
            }
        }

        public DigestInputStream appendMessageDigestStream(InputStream content) {
            DigestInputStream in = null;
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("MD5");
                in = new DigestInputStream(content, digest);
            } catch (NoSuchAlgorithmException e) {
                String error = "MD5 does not appear to be a valid digest format" + "in this environment.";
                logger.error(error, e);
                throw new TdarRuntimeException(error, e);
            }
            return in;
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