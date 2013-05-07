package org.tdar.core.service.workflow;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.filestore.WorkflowContext;

/**
 * @author Adam Brin
 * 
 */
@Service
public class WorkflowContextService {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TargetDatabase tdarDataImportDatabase;
    @Autowired
    private InformationResourceFileVersionService informationResourceFileVersionService;
    @Autowired
    private GenericDao genericDao;
    @Autowired
    private XmlService xmlService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private OntologyService ontologyService;
    @Autowired
    private CodingSheetService codingSheetService;

    /**
     * This method takes a workflow context once it's been generated and persists it back into tDAR. It will remove all existing derivatives, then
     * rehydrate all of the objects associated with the context, and then save them back into the database
     * 
     * @param ctx
     */
    @Transactional
    public void processContext(WorkflowContext ctx) {
        // Delete the old, existing derivatives on current IRFile. That is, any derivatives that have previously been persisted.
        // FIXME: only delete the derivatives for the CURRENT VERSON, not ALL VERSIONS

        for (InformationResourceFileVersion orig : ctx.getOriginalFiles()) {
            informationResourceFileVersionService.deleteDerivatives(orig);
            // gets the uploaded IRFileVersion
            // InformationResourceFileVersion orig = ctx.getOriginalFile();

            // Finds the irFile. We could call orig.getInformationResourceFile() but we need irFile associated w/ the current hibernate session
            InformationResourceFile irFile = genericDao.find(InformationResourceFile.class, orig.getInformationResourceFileId());
logger.info("IRFILE {}",irFile);
            if (ctx.getNumPages() >= 0) {
                irFile.setNumberOfParts(ctx.getNumPages());
            }

            Resource resource = genericDao.find(ctx.getResourceType().getResourceClass(), ctx.getInformationResourceId());
            switch (ctx.getResourceType()) {
                case DATASET:
                    Dataset dataset = (Dataset) resource;
                    if (ctx.getTransientResource() == null) {
                        break;
                    }
                    genericDao.detachFromSession(ctx.getTransientResource());
                    logger.info("resource: ", ctx.getTransientResource());
                    logger.info("data tables: {}", ((Dataset) ctx.getTransientResource()).getDataTables());
                    datasetService.reconcileDataset(irFile, dataset, (Dataset) ctx.getTransientResource());
                    genericDao.saveOrUpdate(dataset);
                    break;
                case ONTOLOGY:
                    Ontology ontology = (Ontology) resource;
                    // should we pass in the context?
                    ontologyService.shred(ontology);
                    ontologyService.saveOrUpdate(ontology);
                    break;
                case CODING_SHEET:
                    CodingSheet codingSheet = (CodingSheet) resource;
                    codingSheetService.ingestCodingSheet(codingSheet, ctx);
                    ontologyService.saveOrUpdate(codingSheet);
                    datasetService.refreshAssociatedDataTables(codingSheet);
                    break;
                default:
                    break;

            }
            // setting transient context for evaluation

            orig.setInformationResourceFile(irFile);

            // Grab the new derivatives from the context and persist them.
            for (InformationResourceFileVersion version : ctx.getVersions()) {
                // if the derivative's ID is null, we know that it hasn't been persisted yet, so we save.
                if (version.getInformationResourceFileId().equals(irFile.getId())) {
                    version.setInformationResourceFile(irFile);
                    irFile.addFileVersion(version);
                }
            }
            logger.debug("irFile: {} ", irFile);
            // }
            orig.setInformationResourceFile(irFile);
            // genericDao.saveOrUpdate(orig);
            irFile.setInformationResource(genericDao.find(InformationResource.class, ctx.getInformationResourceId()));
            irFile.setWorkflowContext(ctx);

            if (ctx.isProcessedSuccessfully()) {
                logger.info("clearing status?: {}", irFile.getStatus());
                irFile.clearQueuedStatus();
            } else {
                if (ctx.isErrorFatal()) {
                    irFile.setStatus(FileStatus.PROCESSING_ERROR);
                } else {
                    irFile.setStatus(FileStatus.PROCESSING_WARNING);
                }
                irFile.setErrorMessage(ctx.getExceptionAsString());
            }
            genericDao.saveOrUpdate(irFile);
            logger.info("end status: {}", irFile.getStatus());
            irFile = genericDao.merge(irFile);
            logger.info("end status: {}", irFile.getStatus());
        }
        try {
            logger.debug(ctx.toXML());
        } catch (Exception e) {
            logger.error("XMLException: {}", e);
        }
    }

    /*
     * given any InformationResourceFileVersion (for an uploaded file) this will create a workflow context
     */
    public WorkflowContext initializeWorkflowContext(Workflow w, InformationResourceFileVersion... versions) {
        WorkflowContext ctx = new WorkflowContext();
        ctx.getOriginalFiles().addAll(Arrays.asList(versions));
        ctx.setTargetDatabase(tdarDataImportDatabase);
        ctx.setResourceType(versions[0].getInformationResourceFile().getInformationResource().getResourceType());
        ctx.setFilestore(TdarConfiguration.getInstance().getFilestore());
        ctx.setInformationResourceId(versions[0].getInformationResourceId());
        ctx.setWorkflowClass(w.getClass());
        // ctx.setInformationResourceFileId(version.getInformationResourceFileId());
        ctx.setWorkingDirectory(new File(System.getProperty("java.io.tmpdir")));
        ctx.setXmlService(xmlService);
        w.initializeWorkflowContext(ctx, versions); // handle any special bits here
        try {
            logger.trace(ctx.toXML());
        } catch (Exception e) {
            logger.error("XML Exception: {}", e);
        }
        return ctx;
    }
}
