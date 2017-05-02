package org.tdar.core.service.workflow;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.filestore.WorkflowContext;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@Service
public class MessageService {
    //
    // private Queue toProcess;
    // private Queue toPersist;

    private WorkflowContextService workflowContextService;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private SerializationService serializationService;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * ContextObjects are embedded in messages as objects and need to be casted back
     */
    @SuppressWarnings("unused")
    private WorkflowContext extractWorkflowContext(Object msg) throws Exception {
        if (msg instanceof String) {
            String strMessage = (String) msg;
            Object ctx_ = serializationService.parseXml(new StringReader(strMessage));
            if (ctx_ instanceof WorkflowContext) {
                return (WorkflowContext) ctx_;
            }
        }
        return null;
    }

    /**
     * FIXME: should this be transactional? If so, convert GenericService references to GenericDao.
     * 
     * This is the beginning of the message process, it takes files that have just been uploaded and creates a workflow context for them, and sends them
     * on the MessageQueue
     * 
     * @param informationResourceFileVersions
     * @param workflow2
     * @return Martin: given that at some future date this might be pushing stuff onto a queue, it shouldn't return anything?
     */
    public <W extends Workflow> boolean sendFileProcessingRequest(Workflow workflow, InformationResourceFileVersion... informationResourceFileVersions) {
        WorkflowContext ctx = workflowContextService.initializeWorkflowContext(workflow, informationResourceFileVersions);
        List<Long> irfIds = new ArrayList<>();
        Set<InformationResource> resources = new HashSet<>();
        for (InformationResourceFileVersion version : informationResourceFileVersions) {
            InformationResourceFile irf = version.getInformationResourceFile();
            if (!irfIds.contains(irf.getId())) {
                irf.setStatus(FileStatus.QUEUED);
                genericDao.saveOrUpdate(irf);
                // FIXME: when we reimplement the message queue, this will need to be adjusted to do a flush here, otherwise, we cannot guarantee that the save
                // will happen before the evict
                resources.add(irf.getInformationResource());
            }
        }
        // genericDao.detachFromSession(resources);
        resources = null;
        try {
            Workflow workflow_ = ctx.getWorkflowClass().newInstance();
            ctx.setSerializationService(serializationService);
            boolean success = workflow_.run(ctx);
            // Martin: the following mandates that we wait for run to complete.
            // Surely the plan is to immediately show the user a result page with "your request is being processed" and then
            // to use AJAX to poll the server for the result via a status bar? Or an email. And the following is to be moved to
            // be part of that call back process?
            workflowContextService.processContext(ctx);
            ctx.clear();
            if (ctx.isErrorFatal()) {
                return false;
            }
            return success;
        } catch (Exception e) {
            // trying to get a more useful debug message...
            logger.warn("Unhandled exception while processing file: " + Arrays.toString(informationResourceFileVersions), e);
            throw new TdarRecoverableRuntimeException("messageService.error_processing");
        }

    }

    /**
     * @param workflowContextService
     *            the workflowContextService to set
     */
    @Autowired
    public void setWorkflowContextService(WorkflowContextService workflowContextService) {
        this.workflowContextService = workflowContextService;
    }

}
