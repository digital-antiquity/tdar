package org.tdar.core.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.tdar.core.bean.Indexable;
import org.tdar.core.cache.Caches;
import org.tdar.search.query.SearchResultHandler;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;

public interface RssService {

    void evictRssCache();

    /**
     * Parse a RSS feed
     * 
     * @param url
     * @return
     * @throws FeedException
     * @throws IOException
     */
    List<SyndEntry> parseFeed(URL url) throws FeedException, IOException;

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
    <I extends Indexable> ByteArrayInputStream createRssFeedFromResourceList(SearchResultHandler<I> handler, FeedSearchHelper helper)
            throws IOException, FeedException;

}