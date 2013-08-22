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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.search.query.SearchResultHandler;

import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.georss.GMLModuleImpl;
import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.geometries.Envelope;
import com.sun.syndication.feed.module.georss.geometries.Position;
import com.sun.syndication.feed.module.opensearch.OpenSearchModule;
import com.sun.syndication.feed.module.opensearch.impl.OpenSearchModuleImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

/**
 * @author Adam Brin
 * 
 */
@Service
public class RssService implements Serializable {

    private static final long serialVersionUID = 8223380890944917677L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    public static final Pattern INVALID_XML_CHARS = Pattern.compile("[\u0001\u0009\u0018\\u000A\\u000D\uD800\uDFFF]");

    // \uDC00-\uDBFF -\uD7FF\uE000-\uFFFD

    public enum GeoRssMode {
        NONE, POINT, ENVELOPE;
    }

    @Autowired
    private UrlService urlService;

    @Autowired
    private AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    public static String cleanStringForXML(String input) {
        return INVALID_XML_CHARS.matcher(input).replaceAll("");
    }

    @SuppressWarnings("unchecked")
    public List<SyndEntry> parseFeed(URL url) throws IllegalArgumentException, FeedException, IOException {
        HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
        // Reading the feed
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(httpcon));
        return feed.getEntries();
    }

    @SuppressWarnings({ "unused", "unchecked" })
    public <I extends Indexable> ByteArrayInputStream createRssFeedFromResourceList(SearchResultHandler<I> handler, String rssUrl, GeoRssMode mode,
            boolean includeEnclosures) throws IOException, FeedException {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom_1.0");

        feed.setTitle(TdarConfiguration.getInstance().getSiteAcronym() + " Search Results: " + cleanStringForXML(handler.getSearchTitle()));
        OpenSearchModule osm = new OpenSearchModuleImpl();
        osm.setItemsPerPage(handler.getRecordsPerPage());
        osm.setStartIndex(handler.getStartRecord());
        osm.setTotalResults(handler.getTotalRecords());

        Link link = new Link();
        link.setHref(urlService.getBaseUrl() + "/includes/opensearch.xml");
        link.setType("application/opensearchdescription+xml");
        osm.setLink(link);
        List<Module> modules = feed.getModules();
        modules.add(osm);
        feed.setModules(modules);
        feed.setLink(rssUrl);
        feed.setDescription(handler.getSearchDescription());
        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        for (I resource_ : handler.getResults()) {
            if (resource_ instanceof Viewable && !((Viewable) resource_).isViewable())
                continue;

            SyndEntry entry = new SyndEntryImpl();
            if (resource_ instanceof OaiDcProvider) {
                OaiDcProvider oaiResource = (OaiDcProvider) resource_;
                entry.setTitle(cleanStringForXML(oaiResource.getTitle()));
                SyndContent description = new SyndContentImpl();

                if (StringUtils.isEmpty(oaiResource.getDescription())) {
                    description.setValue("no description");
                } else {
                    description.setValue(cleanStringForXML(oaiResource.getDescription()));
                }
                List<SyndPerson> authors = new ArrayList<SyndPerson>();
                if (resource_ instanceof Resource) {
                    Resource resource = (Resource) resource_;
                    for (ResourceCreator creator : resource.getPrimaryCreators()) {
                        SyndPerson person = new SyndPersonImpl();
                        person.setName(cleanStringForXML(creator.getCreator().getProperName()));
                        authors.add(person);
                    }
                    if (authors.size() > 0) {
                        entry.setAuthors(authors);
                    }

                    boolean hasRestrictions = resource.hasConfidentialFiles();
                    if (resource_ instanceof InformationResource) {
                        InformationResource informationResource = (InformationResource) resource_;
                        if (informationResource.getLatestUploadedVersions().size() > 0 && includeEnclosures) {
                            for (InformationResourceFile file : informationResource.getVisibleFiles()) {
                                addEnclosure(handler.getAuthenticatedUser(), entry, file.getLatestUploadedVersion());
                                InformationResourceFileVersion thumb = file.getLatestThumbnail();
                                if (thumb != null) {
                                    addEnclosure(handler.getAuthenticatedUser(), entry, thumb);
                                }
                            }
                        }
                    }

                    LatitudeLongitudeBox latLong = resource.getFirstActiveLatitudeLongitudeBox();
                    /*
                     * If LatLong is not Obfuscated and we don't have confidential files then ...
                     */
                    if (latLong != null && mode != GeoRssMode.NONE && !latLong.isObfuscated() && !hasRestrictions) {
                        GeoRSSModule geoRss = new GMLModuleImpl();
                        if (mode == GeoRssMode.ENVELOPE) {
                            geoRss.setGeometry(new Envelope(latLong.getMinObfuscatedLatitude(), latLong.getMinObfuscatedLongitude(), latLong
                                    .getMaxObfuscatedLatitude(), latLong.getMaxObfuscatedLongitude()));
                        }
                        if (mode == GeoRssMode.POINT) {
                            geoRss.setPosition(new Position(latLong.getCenterLatitude(), latLong.getCenterLongitude()));
                        }
                        entry.getModules().add(geoRss);
                    }

                }
                entry.setDescription(description);
                entry.setLink(urlService.absoluteUrl(oaiResource));
                entry.setPublishedDate(oaiResource.getDateCreated());
                entries.add(entry);
            } else {
                throw new TdarRecoverableRuntimeException("Can't handle rss for this");
            }
        }
        feed.setEntries(entries);
        feed.setPublishedDate(new Date());
        if (feed != null) {
            StringWriter writer = new StringWriter();
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            return new ByteArrayInputStream(stripInvalidXMLCharacters(writer.toString()).getBytes());
        }
        return null;
    }

    public static String stripInvalidXMLCharacters(String text) {
        Pattern VALID_XML_CHARS = Pattern.compile("[^\\u0009\\u0018\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\uD800\uDC00-\uDBFF\uDFFF]");
        return VALID_XML_CHARS.matcher(text).replaceAll("");
    }

    @SuppressWarnings("unchecked")
    private void addEnclosure(Person user, SyndEntry entry, InformationResourceFileVersion version) {
        if (version == null)
            return;
        if (user != null && authenticationAndAuthorizationService.canDownload(version, user)) {
            logger.info("allowed:" + version);
            SyndEnclosure enclosure = new SyndEnclosureImpl();
            enclosure.setLength(version.getFileLength());
            enclosure.setType(version.getMimeType());
            enclosure.setUrl(urlService.downloadUrl(version));
            entry.getEnclosures().add(enclosure);
        }
    }

}
