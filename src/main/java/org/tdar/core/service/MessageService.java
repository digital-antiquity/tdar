
package org.tdar.core.service;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.AbstractRabbitConfiguration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SingleConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.configuration.TdarConfiguration;
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
public class MessageService extends AbstractRabbitConfiguration {

    private static final String TO_PERSIST = "to.persist";
    private static final String TO_PROCESS = "to.process";
    private Queue toProcess;
    private Queue toPersist;

    @Autowired
    private WorkflowContextService workflowContextService;

    private transient final Logger logger = Logger.getLogger(getClass());

    /*
     * Supporting method to look in the RabbitMQ Queue for contextObjects that need persisting.
     */
    @Scheduled(fixedDelay = 10000)
    public void checkMessagesToPersist() {
        if (!TdarConfiguration.getInstance().useExternalMessageQueue())
            return;
        logger.trace("checking for message");
        Object msg = getRabbitTemplate(getPersistenceQueue()).receiveAndConvert();
        if (msg != null) {
            WorkflowContext ctx = extractWorkflowContext(msg);
            if (ctx != null) {
                logger.debug("saving entites in stored message");
                workflowContextService.processContext(ctx);
            }
        }
    }

    /*
     * ContextObjects are embedded in messages as objects and need to be casted back
     */
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
    @Scheduled(fixedDelay = 10000)
    public void checkFilesToProcess() {
        if (!TdarConfiguration.getInstance().useExternalMessageQueue())
            return;
        Object msg = getRabbitTemplate(getFilesToProcessQueue()).receiveAndConvert();
        WorkflowContext ctx = extractWorkflowContext(msg);
        if (ctx != null) {
            Workflow w = ctx.getWorkflow();
            if (w != null) {
                try {
                    w.run();
                    RabbitTemplate template = getRabbitTemplate(getPersistenceQueue());
                    template.setRequireAck(true);
                    template.convertAndSend(ctx.toXML());
                } catch (Exception e) {
                    logger.error(e);
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * This is the beginning of the message process, it takes files that have just been uploaded and creates a workflow context for them, and sends them
     * on the MessageQueue
     */
    public void sendFileProcessingRequest(InformationResourceFileVersion version, Workflow w) {
        WorkflowContext ctx = workflowContextService.initializeWorkflowContext(version);
        version.getInformationResourceFile().setStatus(FileStatus.QUEUED);
        ctx.setWorkflow(w);
        w.setWorkflowContext(ctx);
        if (TdarConfiguration.getInstance().useExternalMessageQueue()) {
            RabbitTemplate template = getRabbitTemplate(getFilesToProcessQueue());
            template.setRequireAck(true);
            template.convertAndSend(ctx.toXML());
        } else {
            try {
                w.run();
                workflowContextService.processContext(ctx);
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }

    }

    public ConnectionFactory getConnectionFactory() {
        SingleConnectionFactory connectionFactory = new SingleConnectionFactory(TdarConfiguration.getInstance().getMessageQueueURL());
        connectionFactory.setUsername(TdarConfiguration.getInstance().getMessageQueueUser());
        connectionFactory.setPassword(TdarConfiguration.getInstance().getMessageQueuePwd());
        return connectionFactory;
    }

    @Override
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(getConnectionFactory());
        return rabbitTemplate;
    }

    public RabbitTemplate getRabbitTemplate(Queue queue) {
        RabbitTemplate toRoute = rabbitTemplate();
        toRoute.setQueue(queue.getName());
        toRoute.setRoutingKey(queue.getName());
        return toRoute;
    }

    public synchronized Queue getFilesToProcessQueue() {
        if (toProcess == null) {
            toProcess = new Queue(TdarConfiguration.getInstance().getQueuePrefix() + TO_PROCESS);
            amqpAdmin().declareQueue(toProcess);
        }
        return toProcess;
    }

    public synchronized Queue getPersistenceQueue() {
        if (toPersist == null) {
            toPersist = new Queue(TdarConfiguration.getInstance().getQueuePrefix() + TO_PERSIST);
            amqpAdmin().declareQueue(toPersist);
        }
        return toPersist;
    }

}
