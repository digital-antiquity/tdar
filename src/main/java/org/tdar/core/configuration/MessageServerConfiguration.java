/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Adam Brin
 * 
 */
@Configuration
public class MessageServerConfiguration extends AbstractMessageConfiguration {

    @Bean
    public Queue persistenceQueue() {
        return new Queue(TdarConfiguration.getInstance().getQueuePrefix() + TO_PERSIST);
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return getRabbitTemplate(persistenceQueue());
    }

}
