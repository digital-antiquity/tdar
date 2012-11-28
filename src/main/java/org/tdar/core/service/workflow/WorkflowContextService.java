package org.tdar.core.service.workflow;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.filestore.WorkflowContext;

/**
 * @author Adam Brin
 * 
 */
@Service
public class WorkflowContextService {

    public static final Logger logger = Logger.getLogger(WorkflowContextService.class);
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
        informationResourceFileVersionService.deleteDerivatives(ctx.getOriginalFile());
        // gets the uploaded IRFileVersion
        InformationResourceFileVersion orig = ctx.getOriginalFile();

        // Finds the irFile. We could call orig.getInformationResourceFile() but we need irFile associated w/ the current hibernate session
        InformationResourceFile irFile = genericDao.find(InformationResourceFile.class, ctx.getInformationResourceFileId());
        if (ctx.getNumPages() >= 0) {
            irFile.setNumberOfParts(ctx.getNumPages());
        }

        switch (ctx.getResourceType()) {
            case DATASET:
                Dataset dataset = (Dataset) genericDao.find(ctx.getResourceType().getResourceClass(), ctx.getInformationResourceId());
                if (ctx.getTransientResource() == null) {
                    break;
                }
                genericDao.detachFromSession(ctx.getTransientResource());
                logger.info(ctx.getTransientResource());
                logger.info(((Dataset) ctx.getTransientResource()).getDataTables());
                datasetService.reconcileDataset(irFile, dataset, (Dataset) ctx.getTransientResource());
                genericDao.saveOrUpdate(dataset);
                break;
            default:
                break;

        }
        // setting transient context for evaluation

        orig.setInformationResourceFile(irFile);
        if (ctx.isProcessedSuccessfully()) {
            irFile.clearQueuedStatus();
        } else {
            irFile.setStatus(FileStatus.PROCESSING_ERROR);
        }
        logger.debug(irFile);

        // Grab the new derivatives from the context and persist them.
        for (InformationResourceFileVersion version : ctx.getVersions()) {
            // if the derivative's ID is null, we know that it hasn't been persisted yet, so we save.
            version.setInformationResourceFile(irFile);
            irFile.addFileVersion(version);
        }

        try {
            logger.debug(ctx.toXML());
        } catch (Exception e) {
            logger.error(e);
        }
        orig.setInformationResourceFile(irFile);
        // genericDao.saveOrUpdate(orig);
        irFile.setInformationResource(genericDao.find(InformationResource.class, ctx.getInformationResourceId()));
        genericDao.merge(irFile);
        irFile.setWorkflowContext(ctx);
    }

    /*
     * given any InformationResourceFileVersion (for an uploaded file) this will create a workflow context
     */
    public WorkflowContext initializeWorkflowContext(InformationResourceFileVersion version, Workflow w) {
        WorkflowContext ctx = new WorkflowContext();
        ctx.setOriginalFile(version);
        ctx.setTargetDatabase(tdarDataImportDatabase);
        ctx.setResourceType(version.getInformationResourceFile().getInformationResource().getResourceType());
        ctx.setFilestore(TdarConfiguration.getInstance().getFilestore());
        ctx.setInformationResourceId(version.getInformationResourceId());
        ctx.setInformationResourceFileId(version.getInformationResourceFileId());
        ctx.setWorkingDirectory(new File(System.getProperty("java.io.tmpdir")));
        ctx.setXmlService(xmlService);
        w.initializeWorkflowContext(version, ctx); // handle any special bits here
        try {
            logger.trace(ctx.toXML());
        } catch (Exception e) {
            logger.error(e);
        }
        return ctx;
    }
}
