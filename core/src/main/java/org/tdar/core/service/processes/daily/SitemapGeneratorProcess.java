package org.tdar.core.service.processes.daily;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.processes.AbstractScheduledProcess;
import org.tdar.core.service.resource.ResourceService;

import com.redfin.sitemapgenerator.ChangeFreq;
import com.redfin.sitemapgenerator.GoogleImageSitemapGenerator;
import com.redfin.sitemapgenerator.SitemapIndexGenerator;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;

@Component
@Scope("prototype")
public class SitemapGeneratorProcess extends AbstractScheduledProcess {

    private static final long serialVersionUID = 561910508692901053L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient GenericService genericService;
    private TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    private boolean run = false;

    @Override
    public void execute() {
        run = true;
        File dir = new File(CONFIG.getSitemapDir());
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
        String baseUrl = CONFIG.getBaseSecureUrl();
        try {
            wsg = WebSitemapGenerator.builder(baseUrl, dir).gzip(true).allowMultipleSitemaps(true).build();
            gisg = GoogleImageSitemapGenerator.builder(baseUrl, dir).gzip(true).allowMultipleSitemaps(true).fileNamePrefix("image_sitemap").build();
            sig = new SitemapIndexGenerator(baseUrl, new File(dir, "sitemap_index.xml"));

            Integer totalResource = genericService.countActive(Resource.class).intValue();
            total += totalResource;

            logger.info("({}) resources in sitemap", totalResource);
            ScrollableResults allScrollable = resourceService.findAllActiveScrollableForSitemap();
            while (allScrollable.next()) {
                Resource object = (Resource) allScrollable.get(0);
                String url = UrlService.absoluteSecureUrl(object);
                addUrlHighPriority(wsg, url);
            }

            if (imageSitemapGeneratorEnabled) {
                totalImages = resourceService.findAllResourcesWithPublicImagesForSitemap(gisg);
            }

            logger.info("({}) images in sitemap", totalImages);

            ScrollableResults activeCreators = genericService.findAllActiveScrollable(Creator.class);
            int totalCreator = 0;
            while (activeCreators.next()) {
                Creator<?> creator = (Creator<?>) activeCreators.get(0);
                if (!creator.isBrowsePageVisible()) {
                    continue;
                }
                if (creator.getId().equals(135028) || creator.getId().equals(12729)) {
                    continue;
                }
                String url = UrlService.absoluteSecureUrl(creator);
                addUrlDefaultPriority(wsg, url);
                totalCreator++;
                if (totalCreator % 500 == 0) {
                    genericService.clearCurrentSession();
                }
            }
            logger.info("({}) creators in sitemap", totalCreator);
            total += totalCreator;

            ScrollableResults activeCollections = genericService.findAllScrollable(ResourceCollection.class);
            int totalCollections = 0;
            total += processCollections(wsg, activeCollections);

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
                File sitemap1 = new File(dir, "sitemap1.xml.gz");
                if (file.getName().equals("sitemap.xml.gz") && sitemap1.exists()) {
                    continue;
                }
                File imageSitemap1 = new File(dir, "image_sitemap1.xml.gz");
                if (file.getName().equals("image_sitemap.xml.gz") && imageSitemap1.exists()) {
                    continue;
                }

                sig.addUrl(String.format("%s/%s/%s", baseUrl, "sitemap", file.getName()), date);
            }

            sig.write();
            // wsg.addUrl("http://www.example.com/index.html"); // repeat multiple times
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int processCollections(WebSitemapGenerator wsg, ScrollableResults activeCollections) throws MalformedURLException {
        int totalCollections = 0;
        while (activeCollections.next()) {
            ResourceCollection collection = (ResourceCollection) activeCollections.get(0);
            if (collection.isHidden()) {
                continue;
            }
            String url = UrlService.absoluteSecureUrl((ResourceCollection) collection);
            addUrlHighPriority(wsg, url);
            totalCollections++;
            if (totalCollections % 500 == 0) {
                genericService.clearCurrentSession();
            }

        }
        logger.info("({}) collections in sitemap", totalCollections);
        return totalCollections;
    }

    private void addUrlHighPriority(WebSitemapGenerator wsg, String url) throws MalformedURLException {
        wsg.addUrl(new WebSitemapUrl.Options(url).changeFreq(ChangeFreq.WEEKLY).priority(1.0).build());
    }
    private void addUrlDefaultPriority(WebSitemapGenerator wsg, String url) throws MalformedURLException {
        wsg.addUrl(new WebSitemapUrl.Options(url).changeFreq(ChangeFreq.MONTHLY).priority(.5).build());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "SiteMap generator";
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
