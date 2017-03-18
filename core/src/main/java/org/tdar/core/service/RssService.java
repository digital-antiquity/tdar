/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.cache.Caches;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.XmlEscapeHelper;

import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.georss.SimpleModuleImpl;
import com.rometools.modules.georss.geometries.Envelope;
import com.rometools.modules.georss.geometries.Position;
import com.rometools.modules.opensearch.OpenSearchModule;
import com.rometools.modules.opensearch.impl.OpenSearchModuleImpl;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndCategoryImpl;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEnclosureImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.feed.synd.SyndPersonImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.ParsingFeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;

/**
 * Handles most of the logic around creating RSS Feeds
 * 
 * @author Adam Brin
 * 
 */
@Service
public class RssService implements Serializable {

    private static final String ATOM_1_0 = "atom_1.0";

    private static final String INCLUDES_OPENSEARCH_XML = "/includes/opensearch.xml";

    private static final String APPLICATION_OPENSEARCHDESCRIPTION_XML = "application/opensearchdescription+xml";

    private static final long serialVersionUID = 8223380890944917677L;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    // \uDC00-\uDBFF -\uD7FF\uE000-\uFFFD

    /**
     * Types of spatial objects returned by GeoRSS extension;
     * 
     * @author abrin
     * 
     */
    public enum GeoRssMode {
        NONE, POINT, ENVELOPE;
    }

    @Autowired
    private transient ObfuscationService obfuscationService;

    @Autowired
    private transient AuthorizationService authenticationAndAuthorizationService;

    /**
     * Strip invalid characters from a string for XML (low-level ASCII)
     * 
     * @param input
     * @return
     */
    public static String cleanStringForXML(String input) {
        XmlEscapeHelper xse = new XmlEscapeHelper(-1L);
        String result = xse.stripNonValidXMLCharacters(input);
        return result;
    }


    @CacheEvict(allEntries = true, value = Caches.RSS_FEED)
    public void evictRssCache() {

    }

    /**
     * Parse a RSS feed
     * 
     * @param url
     * @return
     * @throws FeedException
     * @throws IOException
     */
    @Cacheable(value = Caches.RSS_FEED)
    public List<SyndEntry> parseFeed(URL url) throws FeedException, IOException {
        List<SyndEntry> result = new ArrayList<>();
        logger.debug("requesting rss");
        HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
        // Reading the feed
        SyndFeedInput input = new SyndFeedInput();
        XmlReader xmlReader = new XmlReader(httpcon);
        try {
            SyndFeed feed = input.build(xmlReader);
            result.addAll(feed.getEntries());
        } catch (ParsingFeedException pfe) {
            // faims are filling up the log files with stack traces that are very distracting,
            // simply because their feed isn't parsing correctly.
            logger.warn(pfe.getMessage());
        }
        return result;
    }

    /**
     * Generate a RSS feed based on a collection of @link Resource entities.
     * 
     * @param handler
     * @param rssUrl
     * @param mode
     * @param includeEnclosures
     * @return
     * @throws IOException
     * @throws FeedException
     */
    @SuppressWarnings("unused")
    public <I extends Indexable> ByteArrayInputStream createRssFeedFromResourceList(SearchResultHandler<I> handler, FeedSearchHelper helper) throws IOException, FeedException {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType(ATOM_1_0);

        List<Object> vals = new ArrayList<>();
        vals.add(TdarConfiguration.getInstance().getSiteAcronym());
        vals.add(cleanStringForXML(handler.getSearchTitle()));
        feed.setTitle(helper.getTextProvider().getText("rssService.title", vals));
        OpenSearchModule osm = new OpenSearchModuleImpl();
        osm.setItemsPerPage(handler.getRecordsPerPage());
        osm.setStartIndex(handler.getStartRecord());
        osm.setTotalResults(handler.getTotalRecords());

        Link link = new Link();
        link.setHref(UrlService.getBaseUrl() + INCLUDES_OPENSEARCH_XML);
        link.setType(APPLICATION_OPENSEARCHDESCRIPTION_XML);
        osm.setLink(link);
        List<Module> modules = feed.getModules();
        modules.add(osm);
        feed.setModules(modules);
        feed.setLink(helper.getRssUrl());
        feed.setDescription(handler.getSearchDescription());
        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        for (I resource_ : handler.getResults()) {
            createRssEntryForResource(handler, helper.getGeoMode(), helper.isEnclosureIncluded(), entries, resource_);
        }
        feed.setEntries(entries);
        feed.setPublishedDate(new Date());
        if (feed != null) {
            StringWriter writer = new StringWriter();
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            return new ByteArrayInputStream(writer.toString().getBytes());
        }
        return null;
    }

    /**
     * Create an Entry in the RSS Feed for a @link Resource
     * 
     * @param handler
     * @param mode
     * @param includeEnclosures
     * @param entries
     * @param resource_
     * @return
     */
    private <I extends Indexable> SyndEntry createRssEntryForResource(SearchResultHandler<I> handler, GeoRssMode mode, boolean includeEnclosures,
            List<SyndEntry> entries,
            I resource_) {
        if ((resource_ instanceof Viewable) && !((Viewable) resource_).isViewable()) {
            return null;
        }
        if (resource_ instanceof Obfuscatable) {
            obfuscationService.obfuscate((Obfuscatable) resource_, handler.getAuthenticatedUser());
        }
        SyndEntry entry = new SyndEntryImpl();
        if (resource_ instanceof OaiDcProvider) {
            OaiDcProvider oaiResource = (OaiDcProvider) resource_;
            entry.setTitle(cleanStringForXML(oaiResource.getTitle()));
            SyndContent description = new SyndContentImpl();
            if (StringUtils.isEmpty(oaiResource.getDescription())) {
                description.setValue(MessageHelper.getMessage("rssService.no_description"));
            } else {
                description.setValue(cleanStringForXML(oaiResource.getDescription()));
            }
            List<SyndPerson> authors = new ArrayList<SyndPerson>();
            if (resource_ instanceof Resource) {
                Resource resource = (Resource) resource_;
                for (ResourceCreator creator : resource.getPrimaryCreators()) {
                    SyndPerson person = new SyndPersonImpl();
                    String name = cleanStringForXML(creator.getCreator().getProperName());
                    if (StringUtils.isNotBlank(name)) {
                        person.setName(name);
                        authors.add(person);
                    }
                }
                if (authors.size() > 0) {
                    entry.setAuthors(authors);
                }

                // NOTE: this may not display info if we're using a ResourceProxy b/c it doesn't query keywords
                List<SyndCategory> categories = new ArrayList<>();
                for (Keyword kwd : resource.getAllActiveKeywords()) {
                    SyndCategory cat = new SyndCategoryImpl();
                    cat.setName(cleanStringForXML(kwd.getLabel()));
                    cat.setTaxonomyUri(kwd.getDetailUrl());
                    categories.add(cat);
                }
                entry.setCategories(categories);

                boolean hasRestrictions = false;
                if (includeEnclosures) {
                    hasRestrictions = addFileEnclosures(handler, resource_, entry, resource);
                }

                if (mode != GeoRssMode.NONE) {
                    addGeoRssLatLongBox(mode, entry, resource, hasRestrictions);
                }
            }
            entry.setDescription(description);
            entry.setUri(UrlService.absoluteUrl(oaiResource));
            entry.setLink(UrlService.absoluteUrl(oaiResource));
            entry.setPublishedDate(oaiResource.getDateCreated());
            entries.add(entry);
        } else if (resource_ != null) {
            logger.debug("resource: {} {}", resource_, resource_.getClass());
            throw new TdarRecoverableRuntimeException("rssService.cannot_generate_rss");
        }
        return entry;
    }

    /**
     * Add file Enclosures to a Entry for a @link Resource
     * 
     * @param handler
     * @param includeEnclosures
     * @param resource_
     * @param entry
     * @param resource
     * @return
     */
    private <I extends Indexable> boolean addFileEnclosures(SearchResultHandler<I> handler, I resource_, SyndEntry entry, Resource resource) {
        boolean hasRestrictions = resource.hasConfidentialFiles();
        if (resource_ instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) resource_;
            if (informationResource.getLatestUploadedVersions().size() > 0) {
                for (InformationResourceFile file : informationResource.getVisibleFiles()) {
                    addEnclosure(handler.getAuthenticatedUser(), entry, informationResource, file.getLatestUploadedVersion());
                    InformationResourceFileVersion thumb = file.getLatestThumbnail();
                    if (thumb != null) {
                        addEnclosure(handler.getAuthenticatedUser(), entry, informationResource, thumb);
                    }
                }
            }
        }
        return hasRestrictions;
    }

    /**
     * Add the GeoRss Extension to the Entry for a given @link Resource if it has a @link LatitudeLongitideBox and appropriate user permissions.
     * 
     * @param mode
     * @param entry
     * @param resource
     * @param hasRestrictions
     */
    private void addGeoRssLatLongBox(GeoRssMode mode, SyndEntry entry, Resource resource, boolean hasRestrictions) {
        LatitudeLongitudeBox latLong = resource.getFirstActiveLatitudeLongitudeBox();
        /*
         * If LatLong is not purposefully Obfuscated and we don't have confidential files then ...
         */
        if ((latLong != null) && latLong.isObfuscatedObjectDifferent() == false && hasRestrictions == false) {
            GeoRSSModule geoRss = new SimpleModuleImpl();
            if (mode == GeoRssMode.ENVELOPE) {
                geoRss.setGeometry(new Envelope(latLong.getObfuscatedSouth(), latLong.getObfuscatedWest(),
                        latLong.getObfuscatedNorth(), latLong.getObfuscatedEast()));
            }
            if (mode == GeoRssMode.POINT) {
                geoRss.setPosition(new Position(latLong.getObfuscatedCenterLatitude(), latLong.getObfuscatedCenterLongitude()));
            }
            entry.getModules().add(geoRss);
        }
    }

    /**
     * Add an enclosure
     * 
     * @param user
     * @param entry
     * @param version
     */
    private void addEnclosure(TdarUser user, SyndEntry entry, InformationResource ir, InformationResourceFileVersion version) {
        if (version == null) {
            return;
        }
        if ((user != null) && authenticationAndAuthorizationService.canDownload(version, user)) {
            logger.trace("allowed: {}", version);
            SyndEnclosure enclosure = new SyndEnclosureImpl();
            enclosure.setLength(version.getFileLength());
            enclosure.setType(version.getMimeType());
            enclosure.setUrl(UrlService.downloadUrl(ir, version));
            entry.getEnclosures().add(enclosure);
        }
    }

}
