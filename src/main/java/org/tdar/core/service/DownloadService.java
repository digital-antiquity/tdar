package org.tdar.core.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;

/**
 * $Id$
 * 
 * 
 * @author Jim deVos
 * @version $Rev$
 */
@Service
public class DownloadService {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    //TODO
    private String slugify(InformationResource resource) {
        return "ir-archive";
    }
    
    public void generateZipArchive(Collection<File> files, File destinationFile) throws IOException {
        FileOutputStream fout = new FileOutputStream(destinationFile);
        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout));  //what is apache ZipOutputStream? It's probably better.
        for(File file : files) {
            ZipEntry entry = new ZipEntry(file.getName());
            zout.putNextEntry(entry);
            FileInputStream fin  = new FileInputStream(file);
            logger.debug("adding to archive: {}", file);
            IOUtils.copy(fin, zout);
            IOUtils.closeQuietly(fin);
        }
        IOUtils.closeQuietly(zout);
    }
    
    public void generateZipArchive(InformationResource resource, File destinationFile) throws IOException {
        Collection<File> files = new LinkedList<File>();
        
        for (InformationResourceFileVersion version: resource.getLatestVersions()) {
            files.add(version.getFile());
        }
    }
    
    public void generateZipArchive(InformationResource resource) throws IOException {
        generateZipArchive(resource, File.createTempFile(slugify(resource), ".zip"));
    }

}