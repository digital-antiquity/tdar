/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.filestore.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.exception.NonFatalWorkflowException;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.VersionType;

/**
 * @author Adam Brin
 * 
 */
public class ListArchiveTask extends AbstractTask {

    private static final String INDEX_TXT = ".index.txt";
    private static final String CONTENTS_TXT = ".contents.txt";

    private static final long serialVersionUID = 5392550508417818439L;

    private long effectiveSize = 0l;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.tasks.Task#run(java.io.File)
     */
    @Override
    public void run() throws IOException {
        for (FileStoreFile version : getWorkflowContext().getOriginalFiles()) {
            File f_ = version.getTransientFile();
            // take the file
            getLogger().debug("listing contents of: " + f_.getName());
            File f = new File(getWorkflowContext().getWorkingDirectory(), f_.getName() + CONTENTS_TXT);
            File f2 = new File(getWorkflowContext().getWorkingDirectory(), f_.getName() + INDEX_TXT);
            StringBuilder archiveContents = new StringBuilder();

            ArchiveInputStream ais = null;
            int seenFiles = 0;
            boolean validEntries = false;
            try {
                ArchiveStreamFactory factory = new ArchiveStreamFactory();
                InputStream stream = null;
                String filename = f_.getName().toLowerCase();
                if (filename.endsWith(".tgz") || filename.endsWith("tar.gz")) {
                    stream = new GzipCompressorInputStream(new FileInputStream(f_));
                } else if (filename.endsWith(".bz2")) {
                    stream = new BZip2CompressorInputStream(new FileInputStream(f_));
                } else {
                    stream = new FileInputStream(f_);
                }
                ais = factory.createArchiveInputStream(new BufferedInputStream(stream));
                getLogger().info(ais.getClass().toString());
                ArchiveEntry entry = ais.getNextEntry();
                while (entry != null) {
                    if ((entry.getSize() > 0) && (entry.getLastModifiedDate().getTime() > 1)) {
                        validEntries = true;
                    }
                    writeToFile(archiveContents, entry.getName());
                    seenFiles++;
                    entry = ais.getNextEntry();
                }
            } catch (ArchiveException e) {
                throw new NonFatalWorkflowException("listArchiveTask.couldn_not_find_file", Arrays.asList(f_));
            } finally {
                if (ais != null) {
                    IOUtils.closeQuietly(ais);
                }
            }

            if ((seenFiles < 2) && !validEntries) {
                throw new NonFatalWorkflowException("listArchiveTask.invalid");
            }

            // write that to a file with a known format (one file per line)
            FileUtils.writeStringToFile(f, archiveContents.toString());
            FileStoreFile version_ = generateInformationResourceFileVersionFromOriginal(version, f, VersionType.TRANSLATED);
            FileUtils.writeStringToFile(f2, archiveContents.toString());
            FileStoreFile version2_ = generateInformationResourceFileVersionFromOriginal(version, f2, VersionType.INDEXABLE_TEXT);
            version.setUncompressedSizeOnDisk(getEffectiveSize());
            getWorkflowContext().addVersion(version_);
            getWorkflowContext().addVersion(version2_);
        }
    }

    private void writeToFile(StringBuilder archiveContents, String uri) {
        archiveContents.append(uri).append(System.getProperty("line.separator"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.tasks.Task#getName()
     */
    @Override
    public String getName() {
        return "list archive task";
    }

    public long getEffectiveSize() {
        return effectiveSize;
    }

    public void setEffectiveSize(long effectiveSize) {
        this.effectiveSize = effectiveSize;
    }

}
