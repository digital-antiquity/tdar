package org.tdar.struts.action.api.search;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.SortOption;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.FeedSearchHelper;
import org.tdar.core.service.RssService;
import org.tdar.core.service.RssService.GeoRssMode;
import org.tdar.struts.action.AbstractAdvancedSearchController;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.HttpNotFoundErrorOnly;

@Namespaces(value = { 
        @Namespace("/search"),
        @Namespace("/api/search") })
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpOnlyIfUnauthenticated
@HttpNotFoundErrorOnly()
public class RSSSearchAction extends AbstractAdvancedSearchController {

    private static final long serialVersionUID = -1844450612633514072L;

    @Autowired
    private transient RssService rssService;

    private GeoRssMode geoMode = GeoRssMode.POINT;

    // contentLength for excel download requests
    private Long contentLength;
    private InputStream inputStream;
    private int statusCode = 200;
    @Action(value = "rss", results = { @Result(name = SUCCESS, type = "stream", params = {
            "documentName", "rssFeed", "formatOutput", "true", "inputName",
            "inputStream", "contentType", "application/rss+xml",
            "contentLength", "${contentLength}", "contentEncoding", "UTF-8" }),
            @Result(name=ERROR, type=HTTPHEADER, params= {"error", "${statusCode}"})})
    public String viewRss() throws TdarActionException {
        try {
            setDefaultSort(SortOption.ID_REVERSE);
            getLogger().trace("sort field {} ", getSortField());
            if (getSortField() == null) {
                setSecondarySortField(SortOption.TITLE);
            }
            setMode("rss");
            performResourceSearch();
            setSearchTitle(getSearchSubtitle() + ": " + StringEscapeUtils.escapeXml11(getSearchPhrase()));
            setSearchDescription(getText("advancedSearchController.rss_subtitle", TdarConfiguration.getInstance().getSiteAcronym(),
                    StringEscapeUtils.escapeXml11(getSearchPhrase())));
            if (!isReindexing()) {
                FeedSearchHelper feedSearchHelper = new FeedSearchHelper(getRssUrl(), this, getGeoMode(), getAuthenticatedUser());
                feedSearchHelper.setEnclosureIncluded(true);
                setInputStream(rssService.createRssFeedFromResourceList(this, feedSearchHelper));
            } else {
                setInputStream(new ByteArrayInputStream("".getBytes()));
            }
        } catch (TdarActionException tdae) {
            statusCode = tdae.getStatusCode();
            if (statusCode != 404) {
                getLogger().error("rss error(2)", tdae);
            }
            return ERROR;
        } catch (Exception e) {
            getLogger().error("rss error", e);
            setStatusCode(500);
            return ERROR;
        }
        return SUCCESS;
    }

    public GeoRssMode getGeoMode() {
        return geoMode;
    }

    public void setGeoMode(GeoRssMode geoMode) {
        this.geoMode = geoMode;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
