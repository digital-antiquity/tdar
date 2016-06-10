package org.tdar.struts.action.geospatial;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.struts.action.dataset.AbstractDatasetViewAction;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/geospatial")
public class GeospatialViewAction extends AbstractDatasetViewAction<Geospatial> {

    private static final long serialVersionUID = 6518833514525728322L;

    @Override
    public Class<Geospatial> getPersistableClass() {
        return Geospatial.class;
    }
    

    protected InformationResourceFileVersion getLatestUploadedVersion() {
        InformationResourceFileVersion version = null;
        Collection<InformationResourceFileVersion> versions = getPersistable().getLatestVersions(VersionType.GEOJSON);
        if (!versions.isEmpty()) {
            version = getPersistable().getLatestVersions(VersionType.GEOJSON).iterator().next();

        }
        return version;
    }

    public String getGeoJson() {
        // in order for this to work we need to be generating text versions
        // of these files for both text input and file uploads
        String versionText = "";
        InformationResourceFileVersion version = getLatestUploadedVersion();
        if (version != null) {
            try {
                versionText = FileUtils.readFileToString(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version));
            } catch (IOException e) {
                getLogger().debug("an error occurred when trying to load the text version of a file", e);
            }
        }
//        return versionText;
        return null;
    }

}
