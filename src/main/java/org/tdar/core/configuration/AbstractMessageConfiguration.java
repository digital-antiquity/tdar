/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.configuration;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SingleConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Adam Brin
 * 
 */
@Configuration
public class AbstractMessageConfiguration {
    protected static final String TO_PERSIST = "to.persist";
    protected static final String TO_PROCESS = "to.process";

    @Bean
    public ConnectionFactory getConnectionFactory() {
        SingleConnectionFactory connectionFactory = new SingleConnectionFactory(TdarConfiguration.getInstance().getMessageQueueURL());
        connectionFactory.setUsername(TdarConfiguration.getInstance().getMessageQueueUser());
        connectionFactory.setPassword(TdarConfiguration.getInstance().getMessageQueuePwd());
        return connectionFactory;
    }

    public RabbitTemplate getRabbitTemplate(Queue queue) {
        RabbitTemplate toRoute = rabbitTemplate();
        toRoute.setQueue(queue.getName());
        toRoute.setRoutingKey(queue.getName());
        return toRoute;
    }

    private RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(getConnectionFactory());
        return rabbitTemplate;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(getConnectionFactory());
    }

}
