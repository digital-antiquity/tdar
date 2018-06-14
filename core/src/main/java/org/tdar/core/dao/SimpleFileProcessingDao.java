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
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.VersionType;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ImageThumbnailTask;

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
        String filename = LOGO + FilenameUtils.getExtension(fileProxy.getFilename());
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.UPLOADED, filename, null);
        // this will be the "final" filename
        version.setFilename(filename);

        WorkflowContext context = new WorkflowContext();
        context.getOriginalFiles().add(version);
        context.setOkToStoreInFilestore(false);
        context.setResourceType(ResourceType.IMAGE);

        FilestoreObjectType type = FilestoreObjectType.CREATOR;
        if (persistable instanceof ResourceCollection) {
            type = FilestoreObjectType.COLLECTION;
        }

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
            version.setInformationResourceId(persistable.getId());
            filestore.store(type, version.getTransientFile(), version);
            for (InformationResourceFileVersion v : context.getVersions()) {
                v.setInformationResourceId(persistable.getId());
                filestore.store(type, v.getTransientFile(), v);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
