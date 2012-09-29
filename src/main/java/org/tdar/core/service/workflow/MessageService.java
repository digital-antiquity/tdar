package org.tdar.core.service.workflow;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.service.GenericService;
import org.tdar.filestore.WorkflowContext;
import org.tdar.utils.SimpleSerializer;

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

    @Autowired
    private WorkflowContextService workflowContextService;
    @Autowired
    private GenericService genericService;

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
    private WorkflowContext extractWorkflowContext(Object msg) {
        if (msg instanceof String) {
            String strMessage = (String) msg;
            SimpleSerializer ss = new SimpleSerializer();
            Object ctx_ = ss.fromXML(strMessage);
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

     * @param version
     * @param w
     * @return
     */
    public boolean sendFileProcessingRequest(InformationResourceFileVersion version, Workflow w) {
        WorkflowContext ctx = workflowContextService.initializeWorkflowContext(version, w);
        version.getInformationResourceFile().setStatus(FileStatus.QUEUED);
        genericService.saveOrUpdate(version);
        ctx.setWorkflow(w);
        genericService.detachFromSession(version);
        // w.setWorkflowContext(ctx);
        // if (TdarConfiguration.getInstance().useExternalMessageQueue()) {
        // RabbitTemplate template = getRabbitTemplate(getFilesToProcessQueue());
        // template.setRequireAck(true);
        // template.convertAndSend(ctx.toXML());
        // } else {
        boolean success = false;
        try {
            success = w.run(ctx);
            workflowContextService.processContext(ctx);
        } catch (Exception e) {
            // trying to get a more useful debug message...
            logger.warn("Unhandled exception while processing file: " + version, e);
            e.printStackTrace();
        }
        // }
        return success;
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
