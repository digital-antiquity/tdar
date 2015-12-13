package org.tdar.search.config;

import java.io.File;
import java.nio.file.Path;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@EnableSolrRepositories
@Configuration
@PropertySource(value = SolrConfig.SEARCH_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "classpath:" + SolrConfig.SEARCH_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "file://${TDAR_CONFIG_PATH}/" + SolrConfig.SEARCH_PROPERTIES, ignoreResourceNotFound = true)
public class SolrConfig {
    
    public static final String SEARCH_PROPERTIES = "search.properties";
    
    @Resource
    private Environment environment;
    
    @Bean
    public SolrClient solrServerFactoryBean() {
        // more configuration info
        //https://cwiki.apache.org/confluence/display/solr/Using+SolrJ
        String solrServerUrl = environment.getProperty("solr.server.url");
        if (StringUtils.isNotBlank(solrServerUrl)) {
            return new HttpSolrClient(solrServerUrl);
        }
        
        String solrServerPath = environment.getProperty("solr.server.path");
        Path path = new File("target/classes/solr/").toPath();
        if (StringUtils.isNotBlank(solrServerPath)) {
            File dir = new File(solrServerPath);
            if (dir.exists()) {
                path = dir.toPath();
            }
        }
        return new EmbeddedSolrServer(path, "resources");
    }
}