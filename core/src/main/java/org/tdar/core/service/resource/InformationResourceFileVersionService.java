package org.tdar.core.service.resource;

import java.util.Collection;

import org.tdar.core.bean.resource.file.InformationResourceFileVersion;

public interface InformationResourceFileVersionService {

    /**
     * Deletes this information resource file from the filestore, database. Also removes the
     * translated file if it exists. (by default does not purge)
     * 
     * @param file
     */
    void delete(InformationResourceFileVersion file);

    /**
     * Deletes this information resource file from the filestore, database. Also removes the
     * translated file if it exists.
     * 
     * @param file
     * @param purge
     *            Purge the File from the Filestore
     */
    void delete(InformationResourceFileVersion file, boolean purge);

    /**
     * Purge a set of @link InformationResourceFileVersion fiels
     */
    void delete(Collection<InformationResourceFileVersion> files);

    /**
     * Delete only the derivatives related to the @link InformationResourceFile that's referenced by the @link InformationResourceFileVersion
     */
    int deleteDerivatives(InformationResourceFileVersion version);

}