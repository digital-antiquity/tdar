package org.tdar.search.config;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.tdar.core.configuration.SimpleAppConfiguration;

@EnableSolrRepositories
@Configuration
@PropertySource(value = SolrConfig.SEARCH_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "classpath:" + SolrConfig.SEARCH_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "file://${TDAR_CONFIG_PATH}/" + SolrConfig.SEARCH_PROPERTIES, ignoreResourceNotFound = true)
public class SolrConfig {
    
    public static final String SEARCH_PROPERTIES = "search.properties";
    
    @Resource
    private Environment environment;

    private SolrClient solrServer;
    
    public transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    List<SimpleAppConfiguration> config;
    

    //FIXME: autowiring issue trying to get most detailed config
    public SimpleAppConfiguration resolveConfig() {
        if (config.size() == 0) {
            return null;
        }
        SimpleAppConfiguration current  = null;
        if (config.size() > 0) {
            current = config.get(0);
        }
        if (config.size() == 1) {
            return current;
        }
        
        // only really works with 2
        if (config.size() > 1) {
            for (SimpleAppConfiguration config_ : config) {
                Class<? extends SimpleAppConfiguration> currentClass = current.getClass();
                Class<? extends SimpleAppConfiguration> configClass = config_.getClass();
                if (configClass.isAssignableFrom(configClass) && configClass != currentClass) {
                    current = config_;
                }
            }
        }
        return current;
    }
    
    @Bean
    public SolrClient solrServerFactoryBean() {
        SimpleAppConfiguration resolveConfig = resolveConfig();
        logger.trace("config:({}) {}", resolveConfig, config);
        if (resolveConfig.disableHibernateSearch()) {
            return null;
        }
        // more configuration info
        //https://cwiki.apache.org/confluence/display/solr/Using+SolrJ
        String solrServerUrl = environment.getProperty("solr.server.url");
        if (StringUtils.isNotBlank(solrServerUrl)) {
            solrServer = new HttpSolrClient(solrServerUrl);
            
            logger.debug("initializing http Solr:{}", solrServer);
            return solrServer;
        }
        
        String solrServerPath = environment.getProperty("solr.server.path");
        File defaultTestPath = new File("target/classes/solr/");
        Path path = defaultTestPath.toPath();
        //fixme: brittle
        File globalTestPath = new File("web/target/classes/solr/");
        if (globalTestPath.exists()) {
            path = globalTestPath.toPath();
        }
        if (StringUtils.isNotBlank(solrServerPath)) {
            File dir = new File(solrServerPath);
            if (dir.exists()) {
                path = dir.toPath();
            }
        }
        solrServer = new EmbeddedSolrServer(path, "resources");
        logger.debug("initializing embedded Solr:{}", solrServer);
        return solrServer;
    }
    

    @PreDestroy
    public void close() {
        if (this.solrServer != null) {
            this.solrServer.shutdown();
        }
    }
    
}