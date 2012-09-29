package org.tdar.filestore;

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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeTypes;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
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

    /**
     * Write a file to the filestore.
     * 
     * @param {@link File} The file to be stored in the filestore.
     * @return {@link String} the fileId assigned to the content
     * @throws {@link IOException}
     */
    public abstract String store(File content, InformationResourceFileVersion version) throws IOException;

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
        protected static final MimeTypes mimes = TikaConfig.getDefaultConfig().getMimeRepository();
        protected static final Logger logger = Logger.getLogger(BaseFilestore.class);

        protected InformationResourceFileVersion updateVersionInfo(File f, InformationResourceFileVersion version) {
            @SuppressWarnings("deprecation")
            String mimeType = mimes.getMimeType(f).toString();
            if (StringUtils.isEmpty(version.getFilename()))
                version.setFilename(f.getName());
            if (StringUtils.isEmpty(version.getMimeType()))
                version.setMimeType(mimeType);
            String relative = getRelativePath(f);
            File parent = getParentDirectory(new File(relative));
            if (StringUtils.isEmpty(version.getPath()))
                version.setPath(parent.getPath());
            if (version.getSize() == null)
                version.setSize(f.length());
            if (StringUtils.isEmpty(version.getChecksum())) {
                MessageDigest digest = createDigest(f);
                version.setChecksumType(digest.getAlgorithm());
                version.setChecksum(formatDigest(digest));
            }
            if (version.getDateCreated() == null)
                version.setDateCreated(new Date());

            if (StringUtils.isEmpty(version.getExtension()))
                version.setExtension(FilenameUtils.getExtension(f.getName()));
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
            DigestInputStream in = null;
            try {
                in = appendMessageDigestStream(new FileInputStream(f));
                byte[] b = new byte[1000];
                while (in.read(b) != -1) {
                    // read file
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return in.getMessageDigest();
        }

        File getParentDirectory(File outputFile) {
            return new File(outputFile.getParent());
        }

        private String getRelativePath(File f) {
            return new File(getFilestoreLocation()).toURI().relativize(f.toURI()).getPath();
        }

    }

}