package org.tdar.core.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.HasImage;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.fileprocessing.tasks.ImageThumbnailTask;
import org.tdar.fileprocessing.workflows.WorkflowContext;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.VersionType;

@Component
public class SimpleFileProcessingDao {

    private static final String LOGO = "logo.";

    /**
     * Imports a file through our upload process and creates the various image sizes.
     * 
     * @param persistable
     * @param fileProxy
     */
    public void processFileProxyForCreatorOrCollection(HasImage persistable, FileProxy fileProxy) {
        if (fileProxy == null) {
            return;
        }
        // techincally this should use the proxy version of an IRFV, but it's easier here to hack it
        String filename = LOGO + FilenameUtils.getExtension(fileProxy.getName());
        FilestoreObjectType type = FilestoreObjectType.CREATOR;
        if (persistable instanceof ResourceCollection) {
            type = FilestoreObjectType.COLLECTION;
        }
        FileStoreFile version = new FileStoreFile(type, VersionType.UPLOADED, null, filename);

        WorkflowContext context = new WorkflowContext();
        context.getOriginalFiles().add(version);
        context.setOkToStoreInFilestore(false);
        context.setHasDimensions(true);


        ImageThumbnailTask thumbnailTask = new ImageThumbnailTask();
        thumbnailTask.setWorkflowContext(context);
        try {
            // copying the file into the temporary directory and renaming the file from the "temp" version that's specified by struts absaksjfasld.tmp -->
            // uploadedFilename
            File file = new File(context.getWorkingDirectory(), filename);
            version.setTransientFile(file);
            IOUtils.copyLarge(new FileInputStream(fileProxy.getFile()), new FileOutputStream(file));
            thumbnailTask.run();
            Filestore filestore = TdarConfiguration.getInstance().getFilestore();
            version.setPersistableId(persistable.getId());
            filestore.store(type, version.getTransientFile(), version);
            for (FileStoreFile v : context.getVersions()) {
                v.setPersistableId(persistable.getId());
                filestore.store(type, v.getTransientFile(), v);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
