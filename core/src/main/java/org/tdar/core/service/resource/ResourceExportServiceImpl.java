package org.tdar.core.service.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

import com.google.common.base.Objects;

@Service
public class ResourceExportServiceImpl implements ResourceExportService  {

    private static final String EXPORT = "export";
    private static final String RESOURCE_XML = "resource.xml";
    private static final String UPLOADED = "files/";
    private static final String ARCHIVAL = "archival/";
    private static final String PROJECT_XML = "project.xml";
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    Filestore FILESTORE = TdarConfiguration.getInstance().getFilestore();

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private DatasetDao datasetDao;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private SerializationService serializationService;

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ResourceExportService#export(org.tdar.core.service.resource.ResourceExportProxy, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public File export(ResourceExportProxy rep, boolean forReImport) throws Exception {
        List<Resource> resources = new ArrayList<>();
        if (PersistableUtils.isNotNullOrTransient(rep.getAccount())) {
            resources.addAll(rep.getAccount().getResources());
        }
        if (PersistableUtils.isNotNullOrTransient(rep.getCollection())) {
            resources.addAll(rep.getCollection().getManagedResources());
        }
        if (CollectionUtils.isNotEmpty(rep.getResources())) {
            resources.addAll(rep.getResources());
        }
        if (CollectionUtils.isEmpty(resources)) {
            throw new TdarRecoverableRuntimeException("resourceExportService.nothing_selected");
        }
        return export(rep.getFilename(), forReImport, resources);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ResourceExportService#export(java.lang.String, boolean, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public File export(String filename, boolean forReImport, final List<Resource> resources) throws Exception {
        File dir = new File(FileUtils.getTempDirectory(), EXPORT);
        dir.mkdir();
        File zipFile = new File(dir, filename);
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
        File schema = serializationService.generateSchema();
        writeToZip(zout, schema, "tdar.xsd");
        for (Resource r : resources) {
            String base = String.format("%s/%s/", r.getResourceType(), r.getId());
            if (r instanceof InformationResource) {
                InformationResource ir = ((InformationResource) r);
                if (((InformationResource) r).getProject() != Project.NULL) {
                    Project p = ir.getProject();
                    if (forReImport) {
                        p = setupResourceForReImport(ir.getProject());
                    }
                    File file = writeToFile(dir, p, PROJECT_XML);
                    writeToZip(zout, file, base + PROJECT_XML);
                }
                for (InformationResourceFile irf : ir.getActiveInformationResourceFiles()) {
                    InformationResourceFileVersion uploaded = irf.getLatestUploadedVersion();
                    InformationResourceFileVersion archival = irf.getLatestUploadedVersion();
                    File retrieveFile = FILESTORE.retrieveFile(FilestoreObjectType.RESOURCE, uploaded);
                    writeToZip(zout, retrieveFile, base + UPLOADED + retrieveFile.getName());
                    if (archival != null && Objects.equal(archival, uploaded)) {
                        File archicalFile = FILESTORE.retrieveFile(FilestoreObjectType.RESOURCE, archival);
                        writeToZip(zout, archicalFile, base + ARCHIVAL + retrieveFile.getName());
                    }
                }
            }
            Resource r_ = r;
            if (forReImport) {
                r_ = setupResourceForReImport(r);
            }
            File file = writeToFile(dir, r_, RESOURCE_XML);
            writeToZip(zout, file, base + RESOURCE_XML);
        }
        IOUtils.closeQuietly(zout);
        return zipFile;
    }

    private void writeToZip(ZipOutputStream zout, File file, String name) throws IOException, FileNotFoundException {
        ZipEntry zentry = new ZipEntry(name);
        zout.putNextEntry(zentry);
        logger.debug("adding to archive: {}", name);
        FileInputStream fin = new FileInputStream(file);
        IOUtils.copy(fin, zout);
        IOUtils.closeQuietly(fin);
        zout.closeEntry();
    }

    @Transactional(readOnly = true)
    private File writeToFile(File dir, Resource resource, String filename) throws Exception {
        String convertToXML = serializationService.convertToXML(resource);
//        File type = new File("target/export/" + resource.getResourceType().name());
        File file = new File(dir, filename);

        FileUtils.writeStringToFile(file, convertToXML);
        return file;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ResourceExportService#setupResourceForReImport(R)
     */
    @Override
    @Transactional(readOnly = true)
    public <R extends Resource> R setupResourceForReImport(final R resource) {
        genericDao.markReadOnly(resource);

        Long id = resource.getId();
        if (id == null) {
            logger.debug("ID NULL: {}", resource);
        }

        for (Keyword kwd : resource.getAllActiveKeywords()) {
            clearId(kwd);
            if (kwd instanceof HierarchicalKeyword) {
                ((HierarchicalKeyword<?>) kwd).setParent(null);
            }
        }


        // remove internal
        resource.getAuthorizedUsers().clear();
        resource.getLatitudeLongitudeBoxes().forEach(llb -> clearId(llb));
        resource.getManagedResourceCollections().forEach(rc -> {
            clearId(rc);
            rc.setResourceIds(null);
            rc.getManagedResources().clear();
        });

        datasetDao.clearOneToManyIds(resource, true);
        resource.getResourceAnnotations().add(new ResourceAnnotation(new ResourceAnnotationKey("TDAR ID"), id.toString()));

        if (resource instanceof InformationResource) {
            InformationResource ir = (InformationResource) resource;
            nullifyCreator(ir.getPublisher());
            nullifyCreator(ir.getResourceProviderInstitution());
            nullifyCreator(ir.getCopyrightHolder());

            List<FileProxy> proxies = new ArrayList<>();
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                if (irf.isDeleted()) {
                    continue;
                }
                FileProxy fileProxy = new FileProxy(irf);
                fileProxy.setFileId(null);
                fileProxy.setAction(FileAction.ADD);
                fileProxy.setOriginalFileVersionId(-1L);
                proxies.add(fileProxy);
                
            }
            ir.getInformationResourceFiles().clear();
            ir.setFileProxies(proxies);

            if (PersistableUtils.isNotNullOrTransient(ir.getProjectId())) {
                ir.setProject(new Project(ir.getProjectId(), null));
                ir.setMappedDataKeyColumn(null);
            }

            if (resource instanceof Dataset) {
                Dataset dataset = (Dataset) resource;
//                dataset.setDataTables(null);
                dataset.setRelationships(null);
            }

            if (resource instanceof CodingSheet) {
                CodingSheet codingSheet = (CodingSheet) resource;
                codingSheet.setCodingRules(null);
                codingSheet.setAssociatedDataTableColumns(null);
//                codingSheet.setDefaultOntology(null);
            }

            if (resource instanceof Ontology) {
                ((Ontology) resource).setOntologyNodes(null);
            }

        }

        resource.setId(null);
        return resource;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ResourceExportService#clearId(org.tdar.core.bean.Persistable)
     */
    @Override
    @Transactional(readOnly=true)
    public void clearId(Persistable p) {
        datasetDao.clearId(p);
    }


    private void nullifyCreator(Creator<?> creator) {
        datasetDao.nullifyCreator(creator);
    }

    
    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ResourceExportService#exportAsync(org.tdar.core.service.resource.ResourceExportProxy, boolean, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Async
    @Transactional(readOnly = false)
    public void exportAsync(ResourceExportProxy resourceExportProxy, boolean forReImport, TdarUser authenticatedUser) {
        try {
            if (PersistableUtils.isNotNullOrTransient(resourceExportProxy.getAccount())) {
                resourceExportProxy.setAccount(genericDao.merge(resourceExportProxy.getAccount()));
            }
            if (PersistableUtils.isNotNullOrTransient(resourceExportProxy.getCollection())) {
                resourceExportProxy.setCollection(genericDao.merge(resourceExportProxy.getCollection()));
            }
            if (CollectionUtils.isNotEmpty(resourceExportProxy.getResources())) {
                List<Resource> resources = new ArrayList<>();
                for (Resource r : resourceExportProxy.getResources()) {
                    resources.add(genericDao.merge(r));
                }
                resourceExportProxy.setResources(resources);
            }

            export(resourceExportProxy, forReImport);
            sendEmail(resourceExportProxy, authenticatedUser);
        } catch (Exception e) {
            logger.error("error in export", e);
        }
    }

    @Transactional(readOnly=false)
    public void sendEmail(ResourceExportProxy resourceExportProxy, TdarUser authenticatedUser) {
        Email email = new Email();
        email.setTo(authenticatedUser.getEmail());
        email.setFrom(TdarConfiguration.getInstance().getSystemAdminEmail());
        email.setSubject(MessageHelper.getMessage("resourceExportService.email_subject"));
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("resources", resourceExportProxy);
        dataModel.put("file", resourceExportProxy.getFilename());
        String url = String.format("%s/export/download?filename=%s", TdarConfiguration.getInstance().getBaseSecureUrl(), resourceExportProxy.getFilename());
        dataModel.put("url", url);
        dataModel.put("authenticatedUser", authenticatedUser);
        emailService.queueWithFreemarkerTemplate("resource-export-email.ftl", dataModel, email);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ResourceExportService#retrieveFile(java.lang.String)
     */
    @Override
    @Transactional(readOnly=true)
    public File retrieveFile(String filename) throws FileNotFoundException {
        if (StringUtils.isBlank(filename)) {
            throw new FileNotFoundException();
        }
        File dir = new File(FileUtils.getTempDirectory(), EXPORT);
        File zipFile = new File(dir, filename);
        if (!zipFile.exists()) {
            throw new FileNotFoundException(filename + "does not exist");
        }
        return zipFile;
        // TODO Auto-generated method stub
        
    }

}
