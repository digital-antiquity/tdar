package org.tdar.core.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
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
        Map<Long, Long> projectIdMap = uploadProjects(accountId, client, skip);
        logger.debug("done projects");
        final File out = new File("project-map.txt");
        projectIdMap.entrySet().forEach(e -> {
            try {
                FileUtils.writeStringToFile(out, String.format("%s\t%s\n", e.getKey(), e.getValue()));
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        skip = false;
        Map<Long, Long> codingSheetMap = new HashMap<>();
        Map<Long, Long> ontologyMap = new HashMap<>();

        for (Long id : genericService.findAllIds(Ontology.class)) {
            try {
                Long newId = processResource(id, projectIdMap, null, null, skip, client, accountId);
                ontologyMap.put(id, newId);
            } catch (Throwable t) {
                logger.error("error processing FAIMS resource: {}", t, t);
            }
            genericService.clearCurrentSession();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        for (Long id : genericService.findAllIds(CodingSheet.class)) {
            try {
                Long newId = processResource(id, projectIdMap, null, ontologyMap, skip, client, accountId);
                codingSheetMap.put(id, newId);
            } catch (Throwable t) {
                logger.error("error processing FAIMS resource: {}", t, t);
            }
            genericService.clearCurrentSession();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        for (Long id : genericService.findAllIds(InformationResource.class)) {
            if (codingSheetMap.containsKey(id) || ontologyMap.containsKey(id)) {
                continue;
            }

            try {
                processResource(id, projectIdMap, codingSheetMap, ontologyMap, skip, client, accountId);
            } catch (Throwable t) {
                logger.error("error processing FAIMS resource: {}", t, t);
            }
            genericService.clearCurrentSession();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private Long processResource(Long id, Map<Long, Long> projectIdMap, Map<Long, Long> codingSheetMap, Map<Long, Long> ontologyMap, boolean skip,
            APIClient client, Long accountId) {
        Filestore filestore = TdarConfiguration.getInstance().getFilestore();

        InformationResource resource = genericService.find(InformationResource.class, id);
        if (resource.getStatus() != Status.ACTIVE && resource.getStatus() != Status.DRAFT) {
            return null;
        }
        List<File> files = new ArrayList<>();
        logger.debug("{} -- {} ({})", id, resource.getTitle(), (resource instanceof CodingSheet));

        addFaimsId(resource);

        // only pull images from the two projects that have galleries in them.
        if (resource.getActiveInformationResourceFiles().size() > 5 && (resource.getProjectId() == 7292 || resource.getProjectId() == 7293)) {
            // break groups of images into single images
            resource.getResourceNotes().add(new ResourceNote(ResourceNoteType.GENERAL, resource.getTitle()));
            List<InformationResourceFile> irfs = new ArrayList<>(resource.getActiveInformationResourceFiles());
            Map<String,InformationResourceFile> filenameMap = new HashMap<>();
            for (InformationResourceFile irf : irfs) {
                filenameMap.put(irf.getFilename(), irf);
            }
            List<String> toSkip = new ArrayList<>();
            for (InformationResourceFile file : irfs) {
                resource.getActiveInformationResourceFiles().clear();
                if (toSkip.contains(file.getFilename())) {
                    continue;
                }
                try {
                    resource.getInformationResourceFiles().add(file);
                    List<File> fileList = new ArrayList<>();
                    File retrieveFile = getFile(filestore, file);
                    fileList.add(retrieveFile);
                    // try to add the r/f together
                    if (file.getFilename().contains("_f.jpg")) {
                        String prefix = StringUtils.substringBefore(file.getFilename(), "_f.jpg");
                        InformationResourceFile rear = filenameMap.get(prefix + "_r.jpg");
                        if (rear != null) {
                            toSkip.add(rear.getFilename());
                            fileList.add(getFile(filestore, rear));
                        }
                    }
                    resource.setTitle(retrieveFile.getName());
                    // logger.debug(" --> {}", files);
                    String output = export(resource, projectIdMap.get(resource.getProjectId()));

                    ApiClientResponse uploadRecord = client.uploadRecord(output, null, accountId, fileList.toArray(new File[0]));
                    if (uploadRecord != null) {
                        projectIdMap.put(id, uploadRecord.getTdarId());
                        if (uploadRecord.getStatusCode() != StatusCode.CREATED.getHttpStatusCode()
                                && uploadRecord.getStatusCode() != StatusCode.UPDATED.getHttpStatusCode()) {
                            logger.warn(uploadRecord.getBody());
                        }
                        logger.debug("status: {}", uploadRecord.getStatusLine());
                        return uploadRecord.getTdarId();
                    }
                } catch (IOException e) {
                    logger.error("error uploading", e);
                }
            }
            return null;
        }
        for (InformationResourceFile file : resource.getActiveInformationResourceFiles()) {
            InformationResourceFileVersion version = file.getLatestUploadedVersion();
            // logger.debug(" - ({}/{}) --> {}", version.getPath(), version.getFilename(), version);
            File retrieveFile = null;
            try {
                retrieveFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, version);
            } catch (FileNotFoundException e) {
                logger.error("cannot find file: {}", e, e);
            }

            if (resource instanceof Dataset) {
                Dataset ds = (Dataset) resource;
                for (DataTable dt : ds.getDataTables()) {
                    dt.setId(null);
                    for (DataTableColumn dtc : dt.getDataTableColumns()) {
                        dtc.setId(null);
                        CodingSheet cs = dtc.getDefaultCodingSheet();
                        if (cs != null) {
                            // set the coding sheet to a reference to the new coding sheet
                            CodingSheet cs_ = new CodingSheet();
                            makeFake(codingSheetMap, cs, cs_);
                            dtc.setDefaultCodingSheet(cs_);
                        }
                    }
                }
            }

            if (resource instanceof CodingSheet) {
                if (file.getCurrentVersion(VersionType.UPLOADED_TEXT) != null) {
                    version = file.getCurrentVersion(VersionType.UPLOADED_TEXT);
                    if (resource instanceof CodingSheet) {
                        CodingSheet cs = (CodingSheet) resource;
                        Ontology defaultOntology = cs.getDefaultOntology();
                        if (defaultOntology != null) {
                            Ontology ont_ = new Ontology();
                            makeFake(ontologyMap, defaultOntology, ont_);
                            cs.setDefaultOntology(ont_);
                        }
                        try {
                            retrieveFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, version);
                            File tmp = new File(CONFIG.getTempDirectory(), retrieveFile.getName().replace(".txt", ".csv"));
                            version.setTransientFile(tmp);
                            file.setFilename(tmp.getName());
                            try {
                                FileUtils.copyFile(retrieveFile, tmp);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            retrieveFile = tmp;
                        } catch (FileNotFoundException e1) {
                            logger.error("exception renaming file:", e1);
                        }
                    }
                }
            }
            files.add(retrieveFile);

        }

        // logger.debug(" --> {}", files);
        String output = export(resource, projectIdMap.get(resource.getProjectId()));

        if (skip) {
            genericService.clearCurrentSession();
            return null;
        }
        try {
            ApiClientResponse uploadRecord = client.uploadRecord(output, null, accountId, files.toArray(new File[0]));
            if (uploadRecord != null) {
                projectIdMap.put(id, uploadRecord.getTdarId());
                if (uploadRecord.getStatusCode() != StatusCode.CREATED.getHttpStatusCode()
                        && uploadRecord.getStatusCode() != StatusCode.UPDATED.getHttpStatusCode()) {
                    logger.warn(uploadRecord.getBody());
                }
                logger.debug("status: {}", uploadRecord.getStatusLine());
                return uploadRecord.getTdarId();
            }
        } catch (IOException e) {
            logger.error("error uploading", e);
        }
        return null;

    }

    private File getFile(Filestore filestore, InformationResourceFile file) {
        File retrieveFile = null;
        InformationResourceFileVersion version = file.getLatestUploadedVersion();
        try {
            retrieveFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, version);
        } catch (FileNotFoundException e) {
            logger.error("cannot find file: {}", e, e);
        }
        return retrieveFile;
    }

    private void makeFake(Map<Long, Long> map, Resource cs, Resource cs_) {
        Long csid = map.get(cs.getId());
        cs_.setId(csid);
        cs_.setTitle(cs.getTitle());
        cs_.setStatus(cs.getStatus());
    }

    private Map<Long, Long> uploadProjects(Long accountId, APIClient client, boolean skip) {
        Map<Long, Long> projectIdMap = new HashMap<>();
        for (Long id : genericService.findAllIds(Project.class)) {
            Resource resource = genericService.find(Project.class, id);
            if (resource.getStatus() != Status.ACTIVE && resource.getStatus() != Status.DRAFT) {
                continue;
            }
            resource.setStatus(Status.DRAFT);
            addFaimsId(resource);

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
        return projectIdMap;
    }

    private void addFaimsId(Resource resource) {
        resource.getResourceAnnotations()
                .add(new ResourceAnnotation(new ResourceAnnotationKey("FAIMS ID"), "repo.fedarch.org/" + resource.getUrlNamespace() + "/" + resource.getId()));
    }

    private String export(Resource resource, Long projectId) {
        Long id = resource.getId();
        resource.getInvestigationTypes().clear();
        // we mess with IDs, so just in case
        Resource r = resourceExportService.setupResourceForReImport(resource);

        if (r instanceof InformationResource && projectId != null) {
            InformationResource informationResource = (InformationResource) r;
            informationResource.setProject(new Project(projectId, null));
            informationResource.setMappedDataKeyColumn(null);
        }

        if (resource instanceof Dataset) {
            Dataset dataset = (Dataset) resource;
            // dataset.setDataTables(null);
            dataset.setRelationships(null);
        }

        if (resource instanceof CodingSheet) {
            CodingSheet codingSheet = (CodingSheet) resource;
            codingSheet.setCodingRules(null);
            codingSheet.setAssociatedDataTableColumns(null);
            // codingSheet.setDefaultOntology(null);
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
