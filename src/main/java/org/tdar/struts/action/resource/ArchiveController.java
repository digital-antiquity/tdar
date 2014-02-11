package org.tdar.struts.action.resource;

import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Archive;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FileProxy;

/**
 * Wraps an archive file, such as a zip file or a tarball.
 * 
 * @author Martin Paulo
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/archive")
public class ArchiveController extends AbstractInformationResourceController<Archive> {

    private static final long serialVersionUID = -8015985036235809392L;

    @Override
    protected String save(Archive persistable) throws TdarActionException {
        List<FileProxy> fileProxies = getFileProxies();
        saveBasicResourceMetadata();
        saveInformationResourceProperties();
        handleUploadedFiles();
        // this is a little hacky, but we need to re-process the file to get the work flow to make a copy of the tarball if
        // the user has checked the 'do import content' flag. In the time frame available I can't think of another way to do this.
        boolean isOnlyMetadataChanged = false;
        for( FileProxy fp: fileProxies) {
            if (fp.getAction() == FileAction.MODIFY_METADATA) {
                isOnlyMetadataChanged = true;
                break;
            }
        }
        if (isOnlyMetadataChanged && getArchive().isDoImportContent()) {
            reprocess();
        }
        return SUCCESS;
    }

    @Override
    public Class<Archive> getPersistableClass() {
        return Archive.class;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return getAnalyzer().getExtensionsForType(ResourceType.ARCHIVE);
    }

    public void setArchive(final Archive archive) {
        setPersistable(archive);
    }

    public Archive getArchive() {
        return getPersistable();
    }

    @Override
    public Archive getResource() {
        if (getPersistable() == null)
            setPersistable(createPersistable());
        return getPersistable();
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

}
