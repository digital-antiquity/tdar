package org.tdar.tag.config;

import javax.annotation.PostConstruct;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.search.config.TdarSearchIntegrationAppConfiguration;
import org.tdar.search.service.index.SearchIndexService;


@ImportResource(value = {"classpath:spring-local-settings.xml",
        "classpath:META-INF/cxf/cxf.xml",
        "classpath:META-INF/cxf/cxf-servlet.xml"})

@Configuration
public class TagAppConfiguration extends TdarSearchIntegrationAppConfiguration {

    private static final long serialVersionUID = -6963493881813578774L;

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    Environment env;

    @Autowired
    SolrClient solrClient;

    @Autowired
    GenericService genericService;

    @PostConstruct
    public void init() {
        TdarConfiguration instance = TdarConfiguration.getInstance();
        logger.debug("{}", solrClient.getClass());
        if ((solrClient instanceof EmbeddedSolrServer) &&
                env.getProperty("reindexOnStartup", Boolean.class,Boolean.TRUE)) {
            searchIndexService.indexAll(genericService.find(TdarUser.class,instance.getAdminUserId()));
        }
    }


    @Override
    public boolean disableHibernateSearch() {
        return false;
    }

}
