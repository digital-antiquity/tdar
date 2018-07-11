/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.fileprocessing.tasks;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.tdar.filestore.VersionType;

/**
 * @author Adam Brin
 * 
 */
public class LoggingTask extends AbstractTask {

    private static final String LOG_XML = "log.xml";
    private static final long serialVersionUID = 8593717052108347438L;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.tasks.Task#run(java.io.File)
     */
    @Override
    public void run() throws Exception {
        File f = new File(getWorkflowContext().getWorkingDirectory(), LOG_XML);
        FileUtils.writeStringToFile(f, getWorkflowContext().toXML());
        generateInformationResourceFileVersionFromOriginal(getWorkflowContext().getOriginalFile(), f, VersionType.LOG);
        // don't add to context, just write to filesystem
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.tasks.Task#getName()
     */
    @Override
    public String getName() {
        return "logging task";
    }

}
