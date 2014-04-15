package org.tdar.core.service;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;

/*
 * This service attempts to centralize and support the creation of URL strings from within the java app. It's centralized here
 * to help manage changes, and because we can get the basename from the configuration class
 */
@Service
public class UrlService {

    public static final String TDAR_NAMESPACE_URL = "http://www.tdar.org/namespace";
    public static final String TDAR_NAMESPACE_PREFIX = "tdar";

    private static String baseUrl;

    /*
     * Get the tDAR Base URL
     */
    public static String getBaseUrl() {
        if (baseUrl == null) {
            baseUrl = StringUtils.stripEnd(TdarConfiguration.getInstance().getBaseUrl().trim(), "/");
        }
        return baseUrl;
    }

    /**
     * Generate an absolute URL for anything that's Addressable (has getUriPart()
     * 
     * @param resource
     * @return
     */
    public String absoluteUrl(Addressable resource) {
        return String.format("%s%s", StringUtils.stripEnd(getBaseUrl(), "/"), relativeUrl(resource));
    }

    /**
     * When passing URLs around, need to reformat them to make sure we don't expose the id=
     * 
     * @param url
     * @return
     */
    public static String reformatViewUrl(String url) {
        if (url.matches("(.+)/view\\?id=([0-9]+)&?$")) {
            url = url.replaceFirst("(.+)/view\\?id=(\\d+)", "$1/$2");
        } else if (url.matches("(.+)/view\\?id=([0-9]+)&(.+)$")) {
            url = url.replaceFirst("(.+)/view\\?id=(\\d+)&(.+)", "$1/$2?$3");
        } else if (url.matches("(.+)/creators\\?id=([0-9]+)&?$")) {
            url = url.replaceFirst("(.+)/creators\\?id=(\\d+)", "$1/creators/$2");
        } else if (url.matches("(.+)/creators\\?id=([0-9]+)&(.+)$")) {
            url = url.replaceFirst("(.+/creators)\\?id=(\\d+)&(.+)", "$1/$2?$3");
        }

        return url;
    }

    /**
     * generate a relative URL for a view
     * 
     * @param resource
     * @return
     */
    public String relativeUrl(Addressable resource) {
        return String.format("/%s/%s", resource.getUrlNamespace(), resource.getId());
    }

    /**
     * Generate an absolute URL for a view
     * 
     * @param namespace
     * @param id
     * @return
     */
    public static String absoluteUrl(String namespace, Long id) {
        return String.format("%s/%s/%s", StringUtils.stripEnd(getBaseUrl(), "/"), namespace, id);
    }

    /**
     * Generate a download URL
     * 
     * @param version
     * @return
     */
    public String downloadUrl(InformationResourceFileVersion version) {
        return String.format("%s/filestore/%d/get", StringUtils.stripEnd(getBaseUrl(), "/"), version.getId());
    }

    /**
     * get the URL for a thumbnail image
     * 
     * @return
     */
    public String thumbnailUrl(InformationResourceFileVersion version) {
        return String.format("%s/files/img/sm/%d", StringUtils.stripEnd(getBaseUrl(), "/"), version.getId());
    }

    /**
     * get the URL for a thumbnail image
     * 
     * @return
     */
    public static String thumbnailUrl(Long id) {
        return String.format("%s/files/img/sm/%d", StringUtils.stripEnd(getBaseUrl(), "/"), id);
    }

    /**
     * get the Schema URL
     * 
     * @return
     */
    public String getPairedSchemaUrl() {
        return String.format("%s/schema/current schema.xsd", getBaseUrl());
    }

    /**
     * return the path + queryString for the specified request as originally requested by the client
     * 
     * @param request
     * @return
     */
    // TODO: do we want 'servlet path' instead of 'request URI'?
    public static String getOriginalUrlPath(HttpServletRequest request) {
        String path = request.getServletPath();
        String queryString = request.getQueryString();

        String forwardPath = getAttribute(request, "javax.servlet.forward.request_uri");
        if (forwardPath != null) {
            path = forwardPath;
            queryString = getAttribute(request, "javax.servlet.forward.query_string");
        }

        StringBuffer sb = new StringBuffer(path);
        if (queryString != null) {
            sb.append("?").append(queryString);
        }

        return sb.toString();
    }

    /**
     * Parse the request for an attribute
     * 
     * @param servletRequest
     * @param attribute
     * @return
     */
    private static String getAttribute(HttpServletRequest servletRequest, String attribute) {
        Object attr = servletRequest.getAttribute(attribute);
        return (String) attr;
    }

}
