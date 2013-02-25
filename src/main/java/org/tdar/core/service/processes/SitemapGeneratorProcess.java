package org.tdar.core.service.processes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.resource.ResourceService;

import com.redfin.sitemapgenerator.ChangeFreq;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;

@Component
public class SitemapGeneratorProcess extends ScheduledProcess.Base<HomepageGeographicKeywordCache> {

    private static final int BATCH_SIZE = 1000;

    private static final long serialVersionUID = 561910508692901053L;

    @Autowired
    private ResourceService resourceService;
    @Autowired
    private GenericService genericService;
    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private UrlService urlService;

    int batchCount = 0;

    @Override
    public void execute() {
        TdarConfiguration config = TdarConfiguration.getInstance();
        File file = new File(config.getSitemapDir());
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        file.mkdirs();
        WebSitemapGenerator wsg;
        int total = 0;
        try {
            wsg = WebSitemapGenerator.builder(config.getBaseUrl(), file).gzip(true).allowMultipleSitemaps(true).build();
            // wsg.set
            List<Resource> resources = resourceService.findAllSparseActiveResources();
            total += resources.size();
            logger.info("({}) resources in sitemap", resources.size());
            for (Resource resource : resources) {
                String url = urlService.absoluteUrl(resource);
                addUrl(wsg, url);
            }

            List<Long> people = genericService.findActiveIds(Person.class);
            total += people.size();
            logger.info("({}) people in sitemap", people.size());
            for (Long id : people) {
                String url = urlService.absoluteUrl(URLConstants.ENTITY_NAMESPACE, id);
                addUrl(wsg, url);
            }

            List<Long> institutions = genericService.findActiveIds(Institution.class);
            total += institutions.size();
            logger.info("({}) institutions in sitemap", institutions.size());
            for (Long id : institutions) {
                String url = urlService.absoluteUrl(URLConstants.ENTITY_NAMESPACE, id);
                addUrl(wsg, url);
            }

            List<Long> collections = resourceCollectionService.findAllPublicActiveCollectionIds();
            total += collections.size();
            logger.info("({}) collections in sitemap", collections.size());
            for (Long id : collections) {
                String url = urlService.absoluteUrl(URLConstants.ENTITY_NAMESPACE, id);
                addUrl(wsg, url);
            }
            // for (int i = 0; i < 60000; i++) wsg.addUrl(config.getBaseUrl() +"/doc"+i+".html");
            // total += 60000;

            wsg.write();
            if (total > WebSitemapGenerator.MAX_URLS_PER_SITEMAP) {
                wsg.writeSitemapsWithIndex();
            }
            // wsg.addUrl("http://www.example.com/index.html"); // repeat multiple times
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void addUrl(WebSitemapGenerator wsg, String url) throws MalformedURLException {
        wsg.addUrl(new WebSitemapUrl.Options(url).changeFreq(ChangeFreq.WEEKLY).build());
    }

    private void writeEvery(WebSitemapGenerator wsg, int i) {
        if (batchCount == BATCH_SIZE) {
            // wsg.write();
            batchCount = 0;
        }
        batchCount++;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public String getDisplayName() {
        return "SiteMap generator";
    }

    @Override
    public Class<HomepageGeographicKeywordCache> getPersistentClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCompleted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSingleRunProcess() {
        // TODO Auto-generated method stub
        return false;
    }

}
