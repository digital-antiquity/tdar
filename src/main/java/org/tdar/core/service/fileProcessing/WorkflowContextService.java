/**
 * 
 */
package org.tdar.core.service.fileProcessing;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.InformationResourceFileVersionService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.filestore.WorkflowContext;

/**
 * @author Adam Brin
 * 
 */
@Service
public class WorkflowContextService {

    public static final Logger logger = Logger.getLogger(WorkflowContextService.class);
    @Autowired
    private InformationResourceFileVersionService informationResourceFileVersionService;
    @Autowired
    private InformationResourceService informationResourceService;
    @Autowired
    private InformationResourceFileService informationResourceFileService;

    /**
     * This method takes a workflow context once it's been generated and persists it back into tDAR. It will remove all existing derivatives, then
     * rehydrate all of the objects associated with the context, and then save them back into the database
     * 
     * @param ctx
     */
    @Transactional
    public void processContext(WorkflowContext ctx) {
        // Delete the old, existing derivatives on current IRFile. That is, any derivatives that have previously been persisted.
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("informationResourceFile.id", ctx.getInformationResourceFileId());
        // FIXME: only delete the derivatives for the CURRENT VERSON, not ALL VERSIONS
        for (InformationResourceFileVersion v : informationResourceFileVersionService.findByEqCriteria(props)) {
            if (v.isDerivative()) {
                informationResourceFileVersionService.delete(v, false);
            }
        }

        // If resource is a Document, set the total # of pages if Document
        // FIXME: why is this done in the workflow context and not in the workflow itself?
        InformationResource resource = informationResourceService.find(ctx.getInformationResourceId());
        if (ctx.getNumPages() > 0) {
            if (resource instanceof Document) {
                ((Document) resource).setNumberOfPages(ctx.getNumPages());
                // informationResourceService.saveOrUpdate(resource);
            }
        }

        // gets the uploaded IRFileVersion
        InformationResourceFileVersion orig = ctx.getOriginalFile();

        // Finds the irFile. We could call orig.getInformationResourceFile() but we need irFile associated w/ the current hibernate session
        InformationResourceFile irFile = informationResourceFileService.find(ctx.getInformationResourceFileId());
        orig.setInformationResourceFile(irFile);
        if (ctx.isProcessedSuccessfully()) {
            irFile.clearQueuedStatus();
        } else {
            irFile.setStatus(FileStatus.PROCESSING_ERROR);
        }
        logger.debug(irFile);

        // Grab the new derivatives from the context and persist them.
        for (InformationResourceFileVersion version : ctx.getVersions()) {
            version.setInformationResourceFile(irFile);
            // if the derivative's ID is null, we know that it hasn't been persisted yet, so we save.
            if (version.getId() == null) {
                informationResourceFileVersionService.save(version);
            }
            // if the derivative's ID is not null it has previously been saved, so we pull it onto the current hibernate session and update it.
            else {
                version = informationResourceFileVersionService.merge(version);
                informationResourceFileVersionService.saveOrUpdate(version);
            }
            irFile.addFileVersion(version);
        }

        logger.trace(ctx.toXML());
        orig = informationResourceFileVersionService.merge(orig);
        informationResourceFileService.saveOrUpdate(orig);
        informationResourceFileService.saveOrUpdate(irFile);
        // force update of content so it gets indexed
        resource.markUpdated(resource.getUpdatedBy());
        informationResourceService.saveOrUpdate(resource);
    }

    /*
     * given any InformationResourceFileVersion (for an uploaded file) this will create a workflow context
     */
    public WorkflowContext initializeWorkflowContext(InformationResourceFileVersion version) {
        WorkflowContext ctx = new WorkflowContext();
        ctx.setOriginalFile(version);
        ctx.setFilestore(TdarConfiguration.getInstance().getFilestore());
        ctx.setInformationResourceId(version.getInformationResourceId());
        ctx.setInformationResourceFileId(version.getInformationResourceFileId());
        ctx.setWorkingDirectory(new File(System.getProperty("java.io.tmpdir")));
        logger.trace(ctx.toXML());
        return ctx;
    }
}
