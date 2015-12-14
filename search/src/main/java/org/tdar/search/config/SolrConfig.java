package org.tdar.search.config;

import java.io.File;
import java.nio.file.Path;

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
import org.tdar.core.configuration.TdarAppConfiguration;

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
    SimpleAppConfiguration config;
    
    @Bean
    public SolrClient solrServerFactoryBean() {
        logger.debug("config:{}", config);
        if (config.disableHibernateSearch()) {
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