package org.tdar.core.dao;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasImage;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ImageThumbnailTask;
import org.tdar.struts.data.FileProxy;

@Component
public class SimpleFileProcessingDao {

    public void processFileProxyForCreator(HasImage persistable, FileProxy fileProxy) {
        // techincally this should use the proxy version of an IRFV, but it's easier here to hack it
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.UPLOADED, fileProxy.getFilename(), null);
        version.setTransientFile(fileProxy.getFile());
        WorkflowContext context = new WorkflowContext();
        context.getOriginalFiles().add(version);
        context.setOkToStoreInFilestore(false);
        context.setResourceType(ResourceType.IMAGE);
        ImageThumbnailTask thumbnailTask = new ImageThumbnailTask();
        thumbnailTask.setWorkflowContext(context);
        try {
            thumbnailTask.run();
            Filestore filestore = TdarConfiguration.getInstance().getFilestore();
            version.setInformationResourceId(persistable.getId());
            filestore.store(ObjectType.CREATOR, version.getTransientFile(), version);
            for (InformationResourceFileVersion v : context.getVersions()) {
                v.setInformationResourceId(persistable.getId());
                filestore.store(ObjectType.CREATOR, v.getTransientFile(), v);
            }
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
