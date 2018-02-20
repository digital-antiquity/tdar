package org.tdar.core.configuration.profile;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("!test")
@EnableAsync
@EnableScheduling
public class ProductionProfile {

    
}
