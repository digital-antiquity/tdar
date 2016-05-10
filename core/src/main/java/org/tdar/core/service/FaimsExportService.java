package org.tdar.core.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.resource.ResourceExportService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.APIClient;
import org.tdar.utils.ApiClientResponse;

@Component
public class FaimsExportService {

    @Autowired
    SerializationService serializationService;

    @Autowired
    GenericService genericService;

    @Autowired
    ResourceExportService resourceExportService;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    @Transactional(readOnly = true)
    public void export(String username, String password, Long accountId) {
        genericService.markReadOnly();
        APIClient client = new APIClient(CONFIG.getBaseSecureUrl());
        try {
            client.apiLogin(username, password);
        } catch (Exception e) {
            logger.error("error logging in", e);
        }
        boolean skip = false;
        Map<Long, Long> projectIdMap = new HashMap<>();
        for (Long id : genericService.findAllIds(Project.class)) {
            Resource resource = genericService.find(Project.class, id);
            if (resource.getStatus() != Status.ACTIVE && resource.getStatus() != Status.DRAFT) {
                continue;
            }
            String output = export(resource, null);
            if (skip) {
                genericService.clearCurrentSession();
                continue;
            }

            try {
                ApiClientResponse uploadRecord = client.uploadRecord(output, null, accountId);
                projectIdMap.put(id, uploadRecord.getTdarId());
                logger.debug("status: {}", uploadRecord.getStatusLine());
                if (uploadRecord.getStatusCode() != StatusCode.CREATED.getHttpStatusCode()
                        && uploadRecord.getStatusCode() != StatusCode.UPDATED.getHttpStatusCode()) {
                    logger.warn(uploadRecord.getBody());
                }
            } catch (IOException e) {
                logger.error("error uploading", e);
            }
            genericService.clearCurrentSession();
        }
        logger.debug("done projects");
        Filestore filestore = TdarConfiguration.getInstance().getFilestore();
        for (Long id : genericService.findAllIds(InformationResource.class)) {
            InformationResource resource = genericService.find(InformationResource.class, id);
            if (resource.getStatus() != Status.ACTIVE && resource.getStatus() != Status.DRAFT) {
                continue;
            }

            String output = export(resource, projectIdMap.get(resource.getProjectId()));
            if (resource instanceof CodingSheet) {
                // logger.debug(output);
            }
            if (skip) {
                genericService.clearCurrentSession();
                continue;
            }
            List<File> files = new ArrayList<>();
            for (InformationResourceFile file : resource.getActiveInformationResourceFiles()) {
                InformationResourceFileVersion version = file.getLatestUploadedVersion();
                try {
                    File retrieveFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, version);
                    files.add(retrieveFile);
                } catch (FileNotFoundException e) {
                    logger.error("cannot find file: {}", e, e);
                }
            }
            try {
                ApiClientResponse uploadRecord = client.uploadRecord(output, null, accountId, files.toArray(new File[0]));
                projectIdMap.put(id, uploadRecord.getTdarId());
                logger.debug("status: {}", uploadRecord.getStatusLine());
                if (uploadRecord.getStatusCode() != StatusCode.CREATED.getHttpStatusCode()
                        && uploadRecord.getStatusCode() != StatusCode.UPDATED.getHttpStatusCode()) {
                    logger.warn(uploadRecord.getBody());
                }
            } catch (IOException e) {
                logger.error("error uploading", e);
            }

            genericService.clearCurrentSession();
        }
    }

    private String export(Resource resource, Long projectId) {
        Long id = resource.getId();
        resource.getInvestigationTypes().clear();
        // we mess with IDs, so just in case
        Resource r = resourceExportService.setupResourceForExport(resource);
        if (r instanceof InformationResource && projectId != null) {
            InformationResource informationResource = (InformationResource) r;
            informationResource.setProject(new Project(projectId, null));
            informationResource.setMappedDataKeyColumn(null);
        }

        if (resource instanceof Dataset) {
            Dataset dataset = (Dataset) resource;
            dataset.setDataTables(null);
            dataset.setRelationships(null);
        }

        if (resource instanceof CodingSheet) {
            CodingSheet codingSheet = (CodingSheet) resource;
            codingSheet.setCodingRules(null);
            codingSheet.setAssociatedDataTableColumns(null);
            codingSheet.setDefaultOntology(null);
        }

        if (resource instanceof Ontology) {
            ((Ontology) resource).setOntologyNodes(null);
        }
        try {
            String convertToXML = serializationService.convertToXML(r);
            genericService.detachFromSession(resource);
            r = null;
            resource.setId(id);
            File type = new File("target/export/" + resource.getResourceType().name());
            FileUtils.forceMkdir(type);
            File dir = new File(type, id.toString());
            FileUtils.forceMkdir(dir);

            File file = new File(dir, "record.xml");

            FileUtils.writeStringToFile(file, convertToXML);
            return convertToXML;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
