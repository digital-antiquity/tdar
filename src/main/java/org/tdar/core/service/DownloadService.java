package org.tdar.core.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;

import de.schlichtherle.truezip.file.TFile;

/**
 * $Id$
 * 
 * 
 * @author Jim DeVos
 * @version $Rev$
 */
public class DownloadService {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private String slugify(InformationResource resource) {
        return "ir-archive";
    }
    
    public InputStream generateZipArchive(InformationResource resource, File outputFile) throws IOException {
        // FIXME: consider replacing this with simple ZipOutputStream usage:
        // e.g. http://viralpatel.net/blogs/creating-zip-and-jar-files-in-java/ 
        TFile archive = new TFile(outputFile);
        boolean success = archive.mkdir();
        if (! success) {
            logger.error("Unable to create archive file from resource {} and outputfile {}", resource, outputFile);
            return null;
        }
        // should test if successful
        // FIXME: this should probably be a service layer method for efficiency
        for (InformationResourceFileVersion version: resource.getLatestVersions()) {
            new TFile(version.getFile()).cp(archive);
        }
        return new FileInputStream(archive);        
    }
    
    public InputStream generateZipArchive(InformationResource resource) throws IOException {
        return generateZipArchive(resource, File.createTempFile(slugify(resource), ".zip"));
    }

}
