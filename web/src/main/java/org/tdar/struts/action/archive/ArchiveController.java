package org.tdar.struts.action.archive;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.Archive;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts_base.action.TdarActionException;

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

    @Autowired
    private InformationResourceService informationResourceService;

    @Override
    protected String save(Archive persistable) throws TdarActionException {
        List<FileProxy> fileProxies = getFileProxies();
        String result = super.save(persistable);
        if (!SUCCESS.equals(result)) {
            return result;
        }
        // this is a little hacky, but we need to re-process the file to get the work flow to make a copy of the tarball if
        // the user has checked the 'do import content' flag. In the time frame available I can't think of another way to do this.
        boolean isOnlyMetadataChanged = false;
        for (FileProxy fp : fileProxies) {
            if (fp.getAction() == FileAction.MODIFY_METADATA) {
                isOnlyMetadataChanged = true;
                break;
            }
        }
        if (isOnlyMetadataChanged && getArchive().isDoImportContent()) {
            try {
                ErrorTransferObject errors = informationResourceService.reprocessInformationResourceFiles(getResource());
                processErrorObject(errors);
            } catch (Exception e) {
                // consider removing the "sorry we were unable to ... just showing error message"
                // addActionErrorWithException(null, e);
                addActionErrorWithException(getText("abstractResourceController.we_were_unable_to_process_the_uploaded_content"), e);
            }
            if (hasActionErrors()) {
                return ERROR;
            }
        }
        return result;
    }

    @Override
    public Class<Archive> getPersistableClass() {
        return Archive.class;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        // The following used to be the returned value. I'm leaving it as dead code so
        // any refactoring that might happen (? unlikely, but..) will still affect it.
        @SuppressWarnings("unused")
        Set<String> usedToBe = getAnalyzer().getExtensionsForType(ResourceType.ARCHIVE);
        // But due to user confusion on the interface, we have a choice of limiting the file archive
        // type to bz2 or of changing the user interface. So we limit it here for the time being.
        Set<String> toReturn = new HashSet<>();
        toReturn.add("bz2");
        return toReturn;
    }

    public void setArchive(final Archive archive) {
        setPersistable(archive);
    }

    public Archive getArchive() {
        return getPersistable();
    }

    @Override
    public Archive getResource() {
        if (getPersistable() == null) {
            setPersistable(createPersistable());
        }
        return getPersistable();
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

}
