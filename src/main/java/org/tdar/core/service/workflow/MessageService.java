package org.tdar.core.service.workflow;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.XmlService;
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
    private XmlService xmlService;

    private transient final Logger logger = Logger.getLogger(getClass());

    /*
     * Supporting method to look in the RabbitMQ Queue for contextObjects that need persisting.
     */
    // @Scheduled(fixedDelay = 10000)
    // public void checkMessagesToPersist() {
    // if (!TdarConfiguration.getInstance().useExternalMessageQueue())
    // return;
    // logger.trace("checking for message");
    // Object msg = getRabbitTemplate(getPersistenceQueue()).receiveAndConvert();
    // if (msg != null) {
    // WorkflowContext ctx = extractWorkflowContext(msg);
    // if (ctx != null) {
    // logger.debug("saving entites in stored message");
    // workflowContextService.processContext(ctx);
    // }
    // }
    // }

    /*
     * ContextObjects are embedded in messages as objects and need to be casted back
     */
    @SuppressWarnings("unused")
    private WorkflowContext extractWorkflowContext(Object msg) throws Exception {
        if (msg instanceof String) {
            String strMessage = (String) msg;
            Object ctx_ = xmlService.parseXml(new StringReader(strMessage));
            if (ctx_ instanceof WorkflowContext) {
                return (WorkflowContext) ctx_;
            }
        }
        return null;
    }

    /*
     * This method is designed to be run on a separate machine or process from tDAR (though it can run locally too). It is database independent and will
     * run with only knowledge of the filesystem. It looks for files to process and when done adds them to the "toPersist" queue.
     */
    // @Scheduled(fixedDelay = 10000)
    // public void checkFilesToProcess() {
    // if (!TdarConfiguration.getInstance().useExternalMessageQueue())
    // return;
    // Object msg = getRabbitTemplate(getFilesToProcessQueue()).receiveAndConvert();
    // WorkflowContext ctx = extractWorkflowContext(msg);
    // if (ctx != null) {
    // Workflow w = ctx.getWorkflow();
    // if (w != null) {
    // try {
    // w.run();
    // RabbitTemplate template = getRabbitTemplate(getPersistenceQueue());
    // template.setRequireAck(true);
    // template.convertAndSend(ctx.toXML());
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // }

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
//        genericDao.detachFromSession(resources);
        resources = null;
        try {
            Workflow workflow_ = ctx.getWorkflowClass().newInstance();
            ctx.setXmlService(xmlService);
            boolean success = workflow_.run(ctx);
            // Martin: the following mandates that we wait for run to complete.
            // Surely the plan is to immediately show the user a result page with "your request is being processed" and then
            // to use AJAX to poll the server for the result via a status bar? Or an email. And the following is to be moved to
            // be part of that call back process?
            workflowContextService.processContext(ctx);
            ctx.clear();
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

    //
    //
    // public synchronized Queue getFilesToProcessQueue() {
    // if (toProcess == null) {
    // toProcess = new Queue(TdarConfiguration.getInstance().getQueuePrefix() + TO_PROCESS);
    // amqpAdmin().declareQueue(toProcess);
    // }
    // return toProcess;
    // }
    //
    // public synchronized Queue getPersistenceQueue() {
    // if (toPersist == null) {
    // toPersist = new Queue(TdarConfiguration.getInstance().getQueuePrefix() + TO_PERSIST);
    // amqpAdmin().declareQueue(toPersist);
    // }
    // return toPersist;
    // }

}
