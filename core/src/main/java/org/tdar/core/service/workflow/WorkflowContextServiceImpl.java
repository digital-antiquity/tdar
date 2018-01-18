package org.tdar.core.service.workflow;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.DatasetImportService;
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
public class WorkflowContextServiceImpl implements WorkflowContextService {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private TargetDatabase tdarDataImportDatabase;
    private InformationResourceFileVersionService informationResourceFileVersionService;
    private GenericDao genericDao;
    private SerializationService serializationService;
    private OntologyService ontologyService;
    private CodingSheetService codingSheetService;
    private DatasetImportService datasetImportService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    public WorkflowContextServiceImpl(
            @Qualifier("target") TargetDatabase tdarDataImportDatabase, InformationResourceFileVersionService informationResourceFileVersionService,
            GenericDao genericDao, SerializationService serializationService, DatasetImportService datasetImportService,
            OntologyService ontologyService, CodingSheetService codingSheetService) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
        this.informationResourceFileVersionService = informationResourceFileVersionService;
        this.genericDao = genericDao;
        this.serializationService = serializationService;
        this.datasetImportService = datasetImportService;
        this.ontologyService = ontologyService;
        this.codingSheetService = codingSheetService;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.workflow.WorkflowContextService#processContext(org.tdar.filestore.WorkflowContext)
     */
    @Override
    @Transactional
    public void processContext(WorkflowContext ctx) {
        // Delete the old, existing derivatives on current IRFile. That is, any derivatives that have previously been persisted.
        // FIXME: only delete the derivatives for the CURRENT VERSON, not ALL VERSIONS

        int count = 0;
        for (InformationResourceFileVersion orig : ctx.getOriginalFiles()) {
            informationResourceFileVersionService.deleteDerivatives(orig);
            count++;
            // gets the uploaded IRFileVersion
            // InformationResourceFileVersion orig = ctx.getOriginalFile();

            // Finds the irFile. We could call orig.getInformationResourceFile() but we need irFile associated w/ the current hibernate session
            InformationResourceFile irFile = genericDao.find(InformationResourceFile.class, orig.getInformationResourceFileId());
            logger.info("IRFILE {}", irFile);
            if (ctx.getNumPages() >= 0) {
                irFile.setNumberOfParts(ctx.getNumPages());
            }

            Resource resource = genericDao.find(ctx.getResourceType().getResourceClass(), ctx.getInformationResourceId());
            switch (ctx.getResourceType()) {
                case GEOSPATIAL:
                case DATASET:
                case SENSORY_DATA:
                    Dataset dataset = (Dataset) resource;
                    if (ctx.getTransientResource() == null) {
                        break;
                    }

                    // This should only be done once; if it's a composite geospatial resource, it might be dangerous to do twice as you're merging and
                    // reconcilling with yourself over yourself
                    if (count == 1) {
                        genericDao.detachFromSessionAndWarn(ctx.getTransientResource());
                        logger.info("data tables: {}", ((Dataset) ctx.getTransientResource()).getDataTables());
                        datasetImportService.reconcileDataset(irFile, dataset, (Dataset) ctx.getTransientResource());
                        genericDao.saveOrUpdate(dataset);
                    }
                    break;
                case ONTOLOGY:
                    Ontology ontology = (Ontology) resource;
                    // should we pass in the context?
                    ontologyService.shred(ontology);
                    genericDao.saveOrUpdate(ontology);
                    break;
                case CODING_SHEET:
                    CodingSheet codingSheet = (CodingSheet) resource;
                    codingSheetService.ingestCodingSheet(codingSheet, ctx);
                    genericDao.saveOrUpdate(codingSheet);
                    datasetImportService.refreshAssociatedDataTables(codingSheet);
                    break;
                case ARCHIVE:
                case AUDIO:
                    ((InformationResource) resource).updateFromTransientResource((InformationResource) ctx.getTransientResource());
                    genericDao.saveOrUpdate(resource);
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
            InformationResource informationResource = genericDao.find(InformationResource.class, ctx.getInformationResourceId());
            irFile.setInformationResource(informationResource);
            irFile.setWorkflowContext(ctx);

            // logger.info("end status: {}", irFile.getStatus());
            irFile = genericDao.merge(irFile);
            // logger.info("end status: {}", irFile.getStatus());
            if (ctx.isProcessedSuccessfully()) {
                logger.info("clearing status?: {}", irFile.getStatus());
                irFile.setStatus(FileStatus.PROCESSED);
                irFile.setErrorMessage(null);
                publisher.publishEvent(new TdarEvent(irFile, EventType.CREATE_OR_UPDATE, informationResource.getId()));
            } else {
                if (ctx.isErrorFatal()) {
                    irFile.setStatus(FileStatus.PROCESSING_ERROR);
                } else {
                    irFile.setStatus(FileStatus.PROCESSING_WARNING);
                }
                irFile.setErrorMessage(ctx.getExceptionAsString());
            }
            genericDao.saveOrUpdate(irFile);
        }
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(ctx.toXML());
            }
        } catch (Exception e) {
            logger.error("XMLException: {}", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.workflow.WorkflowContextService#initializeWorkflowContext(org.tdar.core.service.workflow.workflows.Workflow,
     * org.tdar.core.bean.resource.file.InformationResourceFileVersion)
     */
    @Override
    public WorkflowContext initializeWorkflowContext(Workflow w, InformationResourceFileVersion... versions) {
        WorkflowContext ctx = new WorkflowContext();
        ctx.getOriginalFiles().addAll(Arrays.asList(versions));
        ctx.setTargetDatabase(tdarDataImportDatabase);
        final InformationResource informationResource = versions[0].getInformationResourceFile().getInformationResource();
        ctx.setResourceType(informationResource.getResourceType());
        ctx.setTransientResource(informationResource.getTransientCopyForWorkflow());
        ctx.setFilestore(TdarConfiguration.getInstance().getFilestore());
        ctx.setInformationResourceId(versions[0].getInformationResourceId());
        ctx.setWorkflowClass(w.getClass());
        ctx.setSerializationService(serializationService);
        w.initializeWorkflowContext(ctx, versions); // handle any special bits here
        try {
            if (logger.isTraceEnabled()) {
                logger.trace(ctx.toXML());
            }
        } catch (Exception e) {
            logger.error("XML Exception: {}", e);
        }
        return ctx;
    }

}
