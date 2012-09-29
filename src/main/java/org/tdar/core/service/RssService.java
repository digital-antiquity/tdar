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
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;

import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.module.Module;
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
import com.sun.syndication.io.SyndFeedOutput;

/**
 * @author Adam Brin
 * 
 */
@Service
public class RssService implements Serializable {

    private static final long serialVersionUID = 8223380890944917677L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    public static final Pattern INVALID_XML_CHARS = Pattern.compile("[\u0001\u0009\\u000A\\u000D\uD800\uDFFF]");
    //\uDC00-\uDBFF\u0020-\uD7FF\uE000-\uFFFD

    @Autowired
    private UrlService urlService;

    @Autowired
    private EntityService entityService;


    public static String cleanStringForXML(String input) {
        return INVALID_XML_CHARS.matcher(input).replaceAll("");
    }
    
    @SuppressWarnings("unused")
    public ByteArrayInputStream createRssFeedFromResourceList(Person user, String subtitle, String searchPhrase, List<Resource> results,
            Integer recordsPerPage, Integer startRecord, Integer totalRecords, String rssUrl) throws IOException, FeedException {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom_1.0");
        feed.setTitle("tDAR Search Results: " + cleanStringForXML(subtitle));
        OpenSearchModule osm = new OpenSearchModuleImpl();
        osm.setItemsPerPage(recordsPerPage);
        osm.setStartIndex(startRecord);
        osm.setTotalResults(totalRecords);

        Link link = new Link();
        link.setHref(urlService.getBaseUrl() + "/includes/opensearch.xml");
        link.setType("application/opensearchdescription+xml");
        osm.setLink(link);
        @SuppressWarnings("unchecked")
        List<Module> modules = feed.getModules();
        modules.add(osm);
        feed.setModules(modules);
        feed.setLink(rssUrl);
        feed.setDescription(searchPhrase);
        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        for (Resource resource : results) {
            SyndEntry entry = new SyndEntryImpl();
            entry.setTitle(cleanStringForXML(resource.getTitle()));
            SyndContent description = new SyndContentImpl();

            if (StringUtils.isEmpty(resource.getDescription())) {
                description.setValue("no description");
            } else {
                description.setValue(cleanStringForXML(resource.getDescription()));
            }
            List<SyndPerson> authors = new ArrayList<SyndPerson>();
            for (ResourceCreator creator : resource.getPrimaryCreators()) {
                SyndPerson person = new SyndPersonImpl();
                person.setName(cleanStringForXML(creator.getCreator().getProperName()));
                authors.add(person);
            }
            if (authors.size() > 0) {
                entry.setAuthors(authors);
            }
            if (resource instanceof InformationResource && ((InformationResource) resource).getLatestUploadedVersions().size() > 0) {
                for (InformationResourceFileVersion version : ((InformationResource) resource).getLatestUploadedVersions()) {
                    logger.trace("enclosure:" + version);
                    addEnclosure(user, entry, version);
                    addEnclosure(user, entry, version.getInformationResourceFile().getLatestThumbnail());
                }
            }

            entry.setDescription(description);
            entry.setLink(urlService.absoluteUrl(resource));
            entry.setPublishedDate(resource.getDateCreated());
            entries.add(entry);
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
        Pattern INVALID_XML_CHARS = Pattern.compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\uD800\uDC00-\uDBFF\uDFFF]");
        return INVALID_XML_CHARS.matcher(text).replaceAll("");
    }
    
    @SuppressWarnings("unchecked")
    private void addEnclosure(Person user, SyndEntry entry, InformationResourceFileVersion version) {
        if(version==null) return;
        if (user != null && entityService.canDownload(version, user)) {
            logger.info("allowed:" + version);
            SyndEnclosure enclosure = new SyndEnclosureImpl();
            enclosure.setLength(version.getSize());
            enclosure.setType(version.getMimeType());
            enclosure.setUrl(urlService.downloadUrl(version));
            entry.getEnclosures().add(enclosure);
        }
    }

}
