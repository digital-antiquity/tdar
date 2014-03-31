package org.tdar.core.configuration;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupHelper implements ApplicationListener<ContextRefreshedEvent> {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    public final String BAR = "\r\n*************************************************************************\r\n";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            TdarConfiguration.getInstance().initialize();
        } catch (Throwable t) {
            logger.error("\r\n\r\n" + BAR + StringUtils.upperCase(t.getMessage()) + BAR, t);
            ((ConfigurableApplicationContext) event.getApplicationContext()).stop();
        }
    }

 
}
