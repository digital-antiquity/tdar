package org.tdar.core.service.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;

public interface ResourceExportService {

    String ZIP = ".zip";

    File export(ResourceExportProxy rep, boolean forReImport) throws Exception;

    File export(String filename, boolean forReImport, Set<Resource> resources) throws Exception;

    <R extends Resource> R setupResourceForReImport(R resource);

    void clearId(Persistable p);

    void exportAsync(ResourceExportProxy resourceExportProxy, boolean forReImport, TdarUser authenticatedUser);

    File retrieveFile(String filename) throws FileNotFoundException;

    void sendEmail(ResourceExportProxy resourceExportProxy, TdarUser authenticatedUser);

}