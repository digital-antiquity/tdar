/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.filestore.tasks;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.tasks.Task.AbstractTask;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.archive.tar.TarBZip2Driver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

/**
 * @author Adam Brin
 * 
 */
public class ListArchiveTask extends AbstractTask {

    private static final long serialVersionUID = 5392550508417818439L;

    private long effectiveSize = 0l;

    /**
     * @return The extensions that the wrapped instance of TrueZip is able understand.
     */
    public static String[] getUnderstoodExtensions() {
        return getArchiveDetector().toString().split("\\|");
    }

    /**
     * @return ALL + .bz2, because bizarrely, out of the box, only 'tar.bz2' is supported: <i>not</i> '.bz2'
     */
    private static TArchiveDetector getArchiveDetector() {
        return new TArchiveDetector(TArchiveDetector.ALL, "bz2", new TarBZip2Driver(IOPoolLocator.SINGLETON));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.tasks.Task#run(java.io.File)
     */
    @Override
    public void run() throws IOException {
        for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
            File f_ = version.getTransientFile();
            // take the file
            getLogger().debug("listing contents of: " + f_.getName());
            File f = new File(getWorkflowContext().getWorkingDirectory(), f_.getName() + ".contents.txt");
            StringBuilder archiveContents = new StringBuilder();

            // list all of the contents
            // http://blog.msbbc.co.uk/2011/09/java-getting-started-with-truezip-api.html
            TFile archiveFile = new TFile(f_, getArchiveDetector());
            if (!archiveFile.isDirectory()) {
                // logging an error means that this error is most likely never seen
                throw new TdarRecoverableRuntimeException("Could find files within the archive?" + archiveFile.getName());
            }

            listFiles(archiveContents, archiveFile, archiveFile);

            // write that to a file with a known format (one file per line)
            FileUtils.writeStringToFile(f, archiveContents.toString());
            InformationResourceFileVersion version_ = generateInformationResourceFileVersionFromOriginal(version, f, VersionType.TRANSLATED);
            version.setUncompressedSizeOnDisk(getEffectiveSize());
            getWorkflowContext().addVersion(version_);
        }
    }

    private void listFiles(StringBuilder archiveContents, File archiveFile, File originalFile) {
        for (File file : archiveFile.listFiles()) {
            getLogger().trace(file.getPath());
            setEffectiveSize(getEffectiveSize() + file.length());
            String uri = originalFile.toURI().relativize(file.toURI()).toString();
            archiveContents.append(uri).append(System.getProperty("line.separator"));
            if (file.isDirectory()) {
                listFiles(archiveContents, file, originalFile);
            }
        }
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
