package org.tdar.search.config;

import java.io.File;
import java.nio.file.Path;

import javax.annotation.Resource;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@EnableSolrRepositories
@Configuration
//@PropertySource("classpath:search.properties")
public class SolrConfig {
    
    @Resource
    private Environment environment;
    
    @Bean
    public SolrClient solrServerFactoryBean() {
        // alternately https://taidevcouk.wordpress.com/2013/07/01/testing-solr-4-3-x-within-spring-an-update-to-zoominfos-inprocesssolrserver/
        Path path = new File("target/classes/solr/").toPath();
        //environment.getRequiredProperty("solr.solr.home")
        EmbeddedSolrServer embed = new EmbeddedSolrServer(path, "resources");
//        embed.getCoreContainer().
        return embed;
    }
}