/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.filestore.tasks;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.filestore.tasks.Task.AbstractTask;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;

/**
 * @author Adam Brin
 * 
 */
public class ListArchiveTask extends AbstractTask {

    private static final long serialVersionUID = 5392550508417818439L;

    private long effectiveSize = 0l;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.tasks.Task#run(java.io.File)
     */
    @Override
    public void run() throws Exception {
        for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
            File f_ = version.getFile();
            // take the file
            getLogger().debug("listing contents of: " + f_.getName());
            File f = new File(getWorkflowContext().getWorkingDirectory(), f_.getName() + ".contents.txt");
            StringBuilder archiveContents = new StringBuilder();

            // list all of the contents
            // http://blog.msbbc.co.uk/2011/09/java-getting-started-with-truezip-api.html
            TConfig.get().setArchiveDetector(TArchiveDetector.ALL);
            TFile archiveFile = new TFile(f_, TArchiveDetector.ALL);

            listFiles(archiveContents, archiveFile, archiveFile);

            // write that to a file with a known format (one file per line)
            FileUtils.writeStringToFile(f, archiveContents.toString());
            InformationResourceFileVersion version_ = generateInformationResourceFileVersionFromOriginal(version, f, VersionType.TRANSLATED);
            version.setUncompressedSizeOnDisk(getEffectiveSize());
            getWorkflowContext().addVersion(version_);
        }
    }

    public void listFiles(StringBuilder archiveContents, File archiveFile, File originalFile) {
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
