/**
 * 
 */
package org.tdar.core.service;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.WorkflowContext;

/**
 * @author Adam Brin
 * 
 */
@Service
public class WorkflowContextService {

    public static final Logger log = Logger.getLogger(WorkflowContextService.class);
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
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("informationResourceFile.id", ctx.getInformationResourceFileId());
        for (InformationResourceFileVersion v : informationResourceFileVersionService.findByEqCriteria(props)) {
            if (v.isDerivative()) {
                informationResourceFileVersionService.delete(v, false);
            }
        }

        InformationResource resource = informationResourceService.find(ctx.getInformationResourceId());
        if (ctx.getNumPages() > 0) {
            if (resource instanceof Document) {
                ((Document) resource).setNumberOfPages(ctx.getNumPages());
                informationResourceService.saveOrUpdate(resource);
            }
        }
        InformationResourceFileVersion orig = ctx.getOriginalFile();
        InformationResourceFile irFile = informationResourceFileService.find(ctx.getInformationResourceFileId());
        orig.setInformationResourceFile(irFile);
        irFile.clearQueuedStatus();

        for (InformationResourceFileVersion version : ctx.getVersions()) {
            version.setInformationResourceFile(irFile);
            if (version.getId() == null) {
                informationResourceFileVersionService.save(version);                
            } else {
                version = informationResourceFileVersionService.merge(version);
                informationResourceFileVersionService.saveOrUpdate(version);
            }
            irFile.addFileVersion(version);
        }

        log.trace(ctx.toXML());
        orig = informationResourceFileVersionService.merge(orig);
        informationResourceFileService.saveOrUpdate(orig);
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
        return ctx;
    }
}
