package org.tdar.core.service.fileProcessing;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.service.GenericService;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.workflows.Workflow;
import org.tdar.utils.SimpleSerializer;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@Service
@Configuration
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

    /*
     * This is the beginning of the message process, it takes files that have just been uploaded and creates a workflow context for them, and sends them
     * on the MessageQueue
     */
    public boolean sendFileProcessingRequest(InformationResourceFileVersion version, Workflow w) {
        genericService.detachFromSession(version);
        WorkflowContext ctx = workflowContextService.initializeWorkflowContext(version);
        version.getInformationResourceFile().setStatus(FileStatus.QUEUED);
        ctx.setWorkflow(w);
        // if (TdarConfiguration.getInstance().useExternalMessageQueue()) {
        // RabbitTemplate template = getRabbitTemplate(getFilesToProcessQueue());
        // template.setRequireAck(true);
        // template.convertAndSend(ctx.toXML());
        // } else {
        try {
            w.run(ctx);
            workflowContextService.processContext(ctx);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            return false;
        }
        return true;
        // }

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
