package org.tdar.core.service.processes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
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
import com.redfin.sitemapgenerator.GoogleImageSitemapGenerator;
import com.redfin.sitemapgenerator.SitemapIndexGenerator;
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
    boolean run = false;
    @Override
    public void execute() {
        run = true;
        TdarConfiguration config = TdarConfiguration.getInstance();
        File dir = new File(config.getSitemapDir());
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        dir.mkdirs();
        WebSitemapGenerator wsg;
        GoogleImageSitemapGenerator gisg;
        SitemapIndexGenerator sig;
        int total = 0;
        int totalImages = 0;
        boolean imageSitemapGeneratorEnabled = true;
        try {
            wsg = WebSitemapGenerator.builder(config.getBaseUrl(), dir).gzip(true).allowMultipleSitemaps(true).build();
            gisg = GoogleImageSitemapGenerator.builder(config.getBaseUrl(), dir).gzip(true).allowMultipleSitemaps(true).fileNamePrefix("image_sitemap").build();
            sig = new SitemapIndexGenerator(config.getBaseUrl(),new File(dir,"sitemap_index.xml"));
            // wsg.set
            List<Resource> resources = resourceService.findAllSparseActiveResources();
            total += resources.size();
            
            logger.info("({}) resources in sitemap", resources.size());
            for (Resource resource : resources) {
                String url = urlService.absoluteUrl(resource);
                addUrl(wsg, url);
            }

            if (imageSitemapGeneratorEnabled) {
                totalImages = resourceService.findAllResourcesWithPublicImagesForSitemap(gisg);
            }

            logger.info("({}) images in sitemap", totalImages);
            

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
            if (total > 0) {
                wsg.write();
            }
            if (totalImages > 0) {
                gisg.write();
            }

            Date date = new Date();
            for (File file : dir.listFiles()) {
                if (file.getName().equals("sitemap_index.xml")) {
                    continue;
                }
                File sitemap1 = new File(dir,"sitemap1.xml.gz");
                if (file.getName().equals("sitemap.xml.gz") && sitemap1.exists()) {
                    continue;
                }
                File imageSitemap1 = new File(dir,"image_sitemap1.xml.gz");
                if (file.getName().equals("image_sitemap.xml.gz") && imageSitemap1.exists()) {
                    continue;
                }
                
                sig.addUrl(String.format("%s/%s/%s",config.getBaseUrl(),"sitemap",file.getName()), date);
            }

            sig.write();
            // wsg.addUrl("http://www.example.com/index.html"); // repeat multiple times
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void addUrl(WebSitemapGenerator wsg, String url) throws MalformedURLException {
        wsg.addUrl(new WebSitemapUrl.Options(url).changeFreq(ChangeFreq.WEEKLY).build());
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
        return run;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

}
