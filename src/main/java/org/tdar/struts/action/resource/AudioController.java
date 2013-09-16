package org.tdar.struts.action.resource;

import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Audio;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.TdarActionException;

/**
 * Makes audio file
 * 
 * @author Martin Paulo
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/audio")
public class AudioController extends AbstractInformationResourceController<Audio> {

    private static final long serialVersionUID = -6026630423934668323L;

    @Override
    protected String save(Audio persistable) throws TdarActionException {
        saveBasicResourceMetadata();
        saveInformationResourceProperties();
        handleUploadedFiles();
        return SUCCESS;
    }

    @Override
    public Class<Audio> getPersistableClass() {
        return Audio.class;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.AUDIO);
    }

    public void setAudio(final Audio audio) {
        setPersistable(audio);
    }

    public Audio getAudio() {
        return getPersistable();
    }

    @Override
    public Audio getResource() {
        if (getPersistable() == null)
            setPersistable(createPersistable());
        return getPersistable();
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        // explicitly set to 1: for if we have multiple files then we need to add some sort of bridging table to contain the info on each file uploaded.
        return false;
    }

}
