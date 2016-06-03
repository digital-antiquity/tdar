package org.tdar.core.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.RssService.GeoRssMode;
import org.tdar.search.query.SearchResultHandler;

import com.opensymphony.xwork2.TextProvider;

public class FeedSearchHelper implements Serializable {

    private static final long serialVersionUID = -2363981015159221672L;
    private String rssUrl;
    private GeoRssMode geoMode;
    private boolean overrideAndObfuscate = true;
    private Class<?> jsonFilter;
    private String jsonCallback;
    private Map<String, Object> searchParams = new HashMap<>();
    private SearchResultHandler<?> handler;
    private TdarUser authenticatedUser;
    private boolean enclosureIncluded;

    public FeedSearchHelper(String rssUrl, SearchResultHandler<?> handler, GeoRssMode geoMode, TdarUser authenticatedUser) {
        this.rssUrl = rssUrl;
        this.handler = handler;
        this.geoMode = geoMode;
        this.setAuthenticatedUser(authenticatedUser);
        getSearchParams().put("recordsPerPage", handler.getRecordsPerPage());
        getSearchParams().put("totalRecords", handler.getTotalRecords());
        getSearchParams().put("startRecord", handler.getStartRecord());
        getSearchParams().put("description", handler.getSearchDescription());
        getSearchParams().put("title", handler.getSearchTitle());
        getSearchParams().put("url", rssUrl);
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    public GeoRssMode getGeoMode() {
        return geoMode;
    }

    public void setGeoMode(GeoRssMode geoMode) {
        this.geoMode = geoMode;
    }

    public boolean isOverrideAndObfuscate() {
        return overrideAndObfuscate;
    }

    public void setOverrideAndObfuscate(boolean overrideAndObfuscate) {
        this.overrideAndObfuscate = overrideAndObfuscate;
    }

    public Class<?> getJsonFilter() {
        return jsonFilter;
    }

    public void setJsonFilter(Class<?> jsonFilter) {
        this.jsonFilter = jsonFilter;
    }

    public String getJsonCallback() {
        return jsonCallback;
    }

    public void setJsonCallback(String jsonCallback) {
        this.jsonCallback = jsonCallback;
    }

    public Map<String, Object> getSearchParams() {
        return searchParams;
    }

    public void setSearchParams(Map<String, Object> searchParams) {
        this.searchParams = searchParams;
    }

    public TextProvider getTextProvider() {
        return (TextProvider) handler;
    }

    public boolean isEnclosureIncluded() {
        return enclosureIncluded;
    }

    public void setEnclosureIncluded(boolean enclosureIncluded) {
        this.enclosureIncluded = enclosureIncluded;
    }

    public TdarUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(TdarUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public List<?> getResults() {
        return handler.getResults();
    }

}
