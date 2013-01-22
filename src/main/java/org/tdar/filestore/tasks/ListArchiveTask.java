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

import de.schlichtherle.io.ArchiveDetector;

/**
 * @author Adam Brin
 * 
 */
public class ListArchiveTask extends AbstractTask {

    private static final long serialVersionUID = 5392550508417818439L;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.tasks.Task#run(java.io.File)
     */
    @Override
    public void run() throws Exception {
        File f_ = getWorkflowContext().getOriginalFile().getFile();
        // take the file
        getLogger().debug("listing contents of: " + f_.getName());
        File f = new File(getWorkflowContext().getWorkingDirectory(), f_.getName() + ".contents.txt");
        StringBuilder archiveContents = new StringBuilder();

        // list all of the contents
        // NOTE: using fully qualified class names to ensure no confusion between packages
        // v7 of truezip moves to it's own dedicated namespace and resolves this issue
        de.schlichtherle.io.File.setDefaultArchiveDetector(ArchiveDetector.ALL);
        de.schlichtherle.io.File archiveFile = new de.schlichtherle.io.File(f_, ArchiveDetector.ALL);

        listFiles(archiveContents, archiveFile, archiveFile);

        // write that to a file with a known format (one file per line)
        FileUtils.writeStringToFile(f, archiveContents.toString());
        InformationResourceFileVersion version = generateInformationResourceFileVersion(f, VersionType.TRANSLATED);
        getWorkflowContext().addVersion(version);
    }

    public void listFiles(StringBuilder archiveContents, File archiveFile, File originalFile) {
        for (File file : archiveFile.listFiles()) {
            getLogger().trace(file.getPath());
            
            archiveContents.append(
                    originalFile.toURI().relativize(file.toURI()).toString()
                    ).append(System.getProperty("line.separator"));
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

}
