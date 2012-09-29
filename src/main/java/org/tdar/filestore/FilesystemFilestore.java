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
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.filestore.Filestore.BaseFilestore;

/**
 * $Id$
 * 
 * Implementation of {@link Filestore} on a locally accessible directory location.
 *
 * @author <a href="matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 */
@Deprecated
public class FilesystemFilestore extends BaseFilestore {
	
	private final File baseStoreDirectory;
	private final String fileStoreLocation;
	
	/**
	 * how many characters of the fileId to use per directory name
	 */
	private final static int charactersPerLevel = 2; 
	
	/**
	 * how many directories to create under the base directory for each item
	 */
	private final static int directoryLevels = 3;
	
	/**
	 * @param pathToFilestore: The base directory on the filesystem where files
	 * are stored.
	 * @throws IOException 
	 */
	public FilesystemFilestore(String pathToFilestore) {
		baseStoreDirectory = new File(pathToFilestore);
		String error = "Can not initialize " + pathToFilestore + " as the filestore location.";
		if (!baseStoreDirectory.isDirectory()) {
			logger.fatal(error);
			throw new IllegalArgumentException(error);
		}
		try {
			fileStoreLocation = baseStoreDirectory.getCanonicalPath();
		} 
		catch (IOException e) {
			logger.fatal(error, e);
			throw new TdarRuntimeException(error, e);
		}
	}
	
	/**
	 * @see org.tdar.filestore.Filestore#store(java.io.InputStream)
	 */
	public String store(InputStream content,InformationResourceFileVersion version) throws IOException {
		OutputStream outstream = null;
		String fileId = generateFileId();
		String dirPath = getAbsoluteDirPath(fileId);
		String errorMessage = "Unable to write content to filestore.";
		DigestInputStream in = appendMessageDigestStream(content);
		try {
			FileUtils.forceMkdir(new File(dirPath));
			File outFile = new File(FilenameUtils.concat(dirPath, fileId));
			if (outFile.canWrite() || outFile.createNewFile()) {
				outstream = new FileOutputStream(outFile);
				IOUtils.copy(in, outstream);
			} else {
				logger.error(errorMessage);
				throw new TdarRuntimeException(errorMessage + 
					"Can't write to: " + outFile.getAbsolutePath());
			}
			
			updateVersionInfo(outFile, version);
		} 
		finally {
			IOUtils.closeQuietly(content);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(outstream);
		}
		MessageDigest digest = in.getMessageDigest();
		if (StringUtils.isEmpty(version.getChecksum())) {
			version.setChecksumType(digest.getAlgorithm());
			version.setChecksum(formatDigest(digest));
		}

		version.setFilestoreId(fileId);
		return fileId;
	}
	
	/**
	 * @see org.tdar.filestore.Filestore#store(File)
	 */
	public String store(File content,InformationResourceFileVersion version) throws IOException {
	    if (content == null || ! content.isFile()) {
	        logger.warn("Trying to store null or empty content: " + content);
	        return "";
	    }
	    return store(new FileInputStream(content),version);
	}
	
	/**
	 * @see org.tdar.filestore.Filestore#retrieveFile(java.lang.String)
	 */
	public File retrieveFile(InformationResourceFileVersion version) throws FileNotFoundException {
	    logger.debug("file requested:" + getAbsoluteFilePath(version.getFilestoreId()));
		File file = new File(getAbsoluteFilePath(version.getFilestoreId()));
		if (!file.isFile()) throw new FileNotFoundException(); 
		return file;
    }
	
	private String generateFileId() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	/**
	 * Constructs the absolute path on the filesystem to the directory for a file
	 * with the given fileId
	 * 
	 * Given a fileId of:
	 * 
	 * 363b29f92662a6c94273a46351c5f50
	 * 
	 * charactersPerLevel = 2, directoryLevels = 3, and  baseStoreDirectory = 
	 * '/home/datastore' this would return:
	 * 
	 * '/home/datastore/36/3b/29/363b29f92662a6c94273a46351c5f50'
	 * 
	 * @param fileId
	 * @return
	 */
	public String getAbsoluteDirPath(String fileId) {
		String filePath = getFilestoreLocation();
		filePath = FilenameUtils.concat(filePath, getRelativeDirPath(fileId));
		return filePath;
	}
	
	/**
	 * Constructs the absolute path to the file in the filestore with a given fileId.  
	 * 
	 * Given a fileId of:
	 * 
	 * 363b29f92662a6c94273a46351c5f50
	 * 
	 * and charactersPerLevel = 2 and directoryLevels = 3 and and  baseStoreDirectory = 
	 * '/home/datastore' this would return:
	 * 
	 * /home/datastore/36/3b/29/363b29f92662a6c94273a46351c5f50
	 * 
	 * @param fileId
	 * @return String 
	 */
	public String getAbsoluteFilePath(String fileId) {	
		return FilenameUtils.concat(getAbsoluteDirPath(fileId), fileId);
	}
	
	/**
	 * Determines the correct subdirectories under baseStoreDirectory where
	 * the file with filename (fileId) should live given the charactersPerLevel
	 * and directoryLevels.  
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
	public String getRelativeDirPath(String fileId) {	
		String path = "";
		for (int i = 0; i < directoryLevels; i++) {
			int start = i * charactersPerLevel;
			int end = start + charactersPerLevel;
			if (fileId.length() > end)
				path = FilenameUtils.concat(path, fileId.substring(start, end));
		}
		return path;
	}
	
	/**
	 * Constructs the relative path to the file in the filestore with a given fileId.  
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
	public String getRelativeFilePath(String fileId) {	
		return FilenameUtils.concat(getRelativeDirPath(fileId), fileId);
	}
	
	/**
	 * @return Canonical path to the base filestore directory on the filesystem
	 * as a string.
	 */
	public String getFilestoreLocation() {
		return fileStoreLocation;
    }
	
	
	/**
	 * @see org.tdar.filestore.Filestore#purge(java.lang.String)
	 */
	public void purge(InformationResourceFileVersion version) throws IOException {
		File file = new File(getAbsoluteFilePath(version.getFilestoreId()));
		delete(file);
	}
	
	private void delete(File file) throws IOException {
		FileUtils.deleteQuietly(file);
		cleanEmptyParents(file.getParentFile());
	}
	
	/**
	 * Recursively check to see if the parent directories are empty and, if so, 
	 * delete them.
	 * 
	 * @param {@link File} representing the directory to clean.
	 * @throws {@link IOException}
	 */
	private void cleanEmptyParents(File dir) throws IOException {
		if (dir == null) return;
		if (dir.exists() && dir.list().length == 0) {
			FileUtils.deleteDirectory(dir);
			cleanEmptyParents(dir.getParentFile());
		}
	}

}
