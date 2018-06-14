package org.tdar.filestore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.exception.TdarRuntimeException;
import org.tdar.utils.MessageHelper;

public abstract class BaseFilestore implements Filestore {
    private static final String MD5 = "MD5";
    // protected static final MimeTypes mimes = TikaConfig.getDefaultConfig().getMimeRepository();
    protected static final Logger logger = LoggerFactory.getLogger(BaseFilestore.class);

    /**
     * This comes from the bad old days and was intended to make dataset filenames safe for postgres importing.
     * Dataset files are converted into tables in postgres and this method
     * was used to generate table names that were postgres-safe, e.g., starts with an alphabetic character and < 128 characters.
     * Now, DatabaseConverter should be responsible for that translation / sanitization internally,
     * and we should preserve the filename as it was originally sent in as best we can.
     * 
     * FIXME: Filestore should be responsible for sanitization of filenames instead
     * 
     * @param filename
     * @return
     */
    public static String sanitizeFilename(String filename) {
        String ext = FilenameUtils.getExtension(filename).toLowerCase();
        String basename = filename.toLowerCase();

        // // make sure that the total length does not exceed 255 characters
        if (filename.length() > 250) {
            basename = basename.substring(0, 250) + "." + ext;
        }

        /*
         * replace all whitespace with dashes
         * replace all characters that are not alphanumeric, underscore "_", or
         * dash "-" with a single dash "-".
         */
        basename = filename.replaceAll("[^\\w\\-\\.\\+\\_]", "-");
        basename = StringUtils.replace(basename, "-.", ".");

        return basename; // builder.toString();
    }

    /*
     * This method extracts out the MimeType from the file using Tika, the previous version tried to parse the file
     * but this doesn't need to be so complex.
     * http://stackoverflow.com/questions/7137634/getting-mimetype-subtype-with-apache-tika
     */
    public static String getContentType(File file, String overrideValue) {
        MediaType mediaType = null;
        Metadata md = new Metadata();
        md.set(TikaMetadataKeys.RESOURCE_NAME_KEY, file.getName());
        Detector detector = new DefaultDetector(TikaConfig.getDefaultConfig().getMimeRepository());
        try (
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis)) {

            mediaType = detector.detect(bis, md); // bufferedStream so we can move
            fis.close();
        } catch (IOException ioe) {
            logger.debug("error", ioe);
        }
        if (mediaType == null) {
            return overrideValue;
        }
        return mediaType.getType() + "/" + mediaType.getSubtype();
    }

    protected FileStoreFileProxy updateVersionInfo(File file, FileStoreFileProxy version) throws IOException {
        String mimeType = getContentType(file, "UNKNOWN/UNKNOWN");
        logger.trace("MIMETYPE: {}", mimeType);
        if (StringUtils.isEmpty(version.getFilename())) {
            version.setFilename(file.getName());
        }
        if (StringUtils.isEmpty(version.getMimeType())) {
            version.setMimeType(mimeType);
        }
        String relative = getRelativePath(file);
        File parent = getParentDirectory(new File(relative));
        if (StringUtils.isEmpty(version.getPath())) {
            version.setPath(parent.getPath());
        }
        if (version.getFileLength() == null) {
            version.setFileLength(file.length());
        }
        if (version.getUncompressedSizeOnDisk() == null) {
            version.setUncompressedSizeOnDisk(file.length());
        }
        if (StringUtils.isEmpty(version.getChecksum())) {
            MessageDigest digest = createDigest(file);
            version.setChecksumType(digest.getAlgorithm());
            version.setChecksum(formatDigest(digest));
            if (version.getVersionType().isArchival() || version.getVersionType().isUploaded()) {
                File checksum = new File(file.getParentFile(), String.format("%s.%s", file.getName(), digest.getAlgorithm()));
                FileUtils.write(checksum, version.getChecksum(), Charset.defaultCharset());
                logFilestoreWrite(checksum);
            }
        }
        if (version.getDateCreated() == null) {
            version.setDateCreated(new Date());
        }

        if (StringUtils.isEmpty(version.getExtension())) {
            version.setExtension(FilenameUtils.getExtension(file.getName()));
        }
        return version;
    }

    protected String formatDigest(MessageDigest digest) {
        return new String(Hex.encodeHex(digest.digest()));
    }

    @Override
    public void storeLog(LogType type, String filename, String message) {
        File logdir = new File(FilenameUtils.concat(getFilestoreLocation(),
                String.format("%s/%s/%s", FilestoreObjectType.LOG.getRootDir(), type.getDir(), Calendar.getInstance().get(Calendar.YEAR))));
        if (!logdir.exists()) {
            logdir.mkdirs();
        }
        File logFile = new File(logdir, filename);
        logFilestoreWrite(logFile);
        try {
            FileUtils.writeStringToFile(logFile, message, Charset.defaultCharset());
        } catch (IOException e) {
            logger.error("Unable to write logfile", e);
        }
    }

    @Override
    public List<File> listLogFiles(LogType type, Integer year) {
        String subdir = String.format("%s/%s", FilestoreObjectType.LOG.getRootDir(), type.getDir());
        if (year != null) {
            subdir = String.format("%s/%s/%s", FilestoreObjectType.LOG.getRootDir(), type.getDir(), year);
        }
        File logDir = new File(FilenameUtils.concat(getFilestoreLocation(), subdir));
        return Arrays.asList(logDir.listFiles());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<File> listXmlRecordFiles(FilestoreObjectType type, Long persistableId) {
        File dir = getDirectory(type, persistableId);
        if (dir.exists()) {
            return FileUtils.listFiles(dir, new String[] { "xml" }, false);
        }
        return Collections.emptyList();
    }

    @Override
    public File getXmlRecordFile(FilestoreObjectType type, Long persistableId, String filename) {
        File dir = getDirectory(type, persistableId);
        return new File(dir, filename);
    }

    @Override
    public File getLogFile(LogType type, Integer year, String filename) {
        String subdir = String.format("%s/%s/%s/%s", FilestoreObjectType.LOG.getRootDir(), type.getDir(), year, filename);
        File logDir = new File(FilenameUtils.concat(getFilestoreLocation(), subdir));
        return logDir;
    }

    @Override
    public boolean verifyFile(FilestoreObjectType type, FileStoreFileProxy object) throws FileNotFoundException {
        File toVerify = retrieveFile(type, object);
        MessageDigest newDigest = createDigest(toVerify);
        String hex = formatDigest(newDigest);
        logger.debug("Verifying file: {}", object.getFilename());
        logger.trace("\told: {} new: {}", object.getChecksum(), hex);
        return hex.trim().equalsIgnoreCase(object.getChecksum().trim());
    }

    public DigestInputStream appendMessageDigestStream(InputStream content) {
        DigestInputStream digestInputStream = null;
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(MD5);
            digestInputStream = new DigestInputStream(content, messageDigest);
        } catch (NoSuchAlgorithmException e) {
            String error = MessageHelper.getMessage("filestore.md5_doesnt_match");
            logger.error(error, e);
            throw new TdarRuntimeException(error, e);
        }
        return digestInputStream;
    }

    @Override
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

    @Override
    public long getSizeInBytes() {
        return FileUtils.sizeOfDirectory(new File(getFilestoreLocation()));
    }

    @Override
    public String getSizeAsReadableString() {
        return FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(new File(getFilestoreLocation())));
    }

    protected void rotate(File outFile, StorageMethod rotation) {
        logger.trace("rotating file: {}", outFile.getName());
        File parentDir = outFile.getParentFile();
        for (int i = rotation.getRotations(); i > 0; i--) {
            String baseName = FilenameUtils.getBaseName(outFile.getName());
            String ext = FilenameUtils.getExtension(outFile.getName());
            String rotationParent = String.format(".%s.", Integer.toString(i - 1));
            String rotationTarget = String.format(".%s.", Integer.toString(i));
            if (i == 1) {
                rotationParent = ".";
            }
            rotationTarget = String.format("%s%s%s", baseName, rotationTarget, ext);
            rotationParent = String.format("%s%s%s", baseName, rotationParent, ext);

            logger.trace("rotating from: {} to {}", rotationParent, rotationTarget);

            File parentFile = new File(parentDir, rotationParent);
            File targetFile = new File(parentDir, rotationTarget);
            logger.trace(parentFile.getAbsolutePath());

            if (parentFile.exists()) {
                try {
                    FileUtils.copyFile(parentFile, targetFile);
                    logFilestoreWrite(targetFile);
                } catch (IOException e) {
                    logger.warn("something happened when saving file", e);
                }
                logger.debug("rotating file {} to {}", parentFile.getAbsolutePath(), targetFile.getAbsolutePath());
            }
        }
    }

}