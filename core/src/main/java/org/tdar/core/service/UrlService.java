package org.tdar.core.service;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;

/*
 * This service attempts to centralize and support the creation of URL strings from within the java app. It's centralized here
 * to help manage changes, and because we can get the basename from the configuration class
 */
@Service
public class UrlService {

    private static String baseUrl;

    /*
     * Get the tDAR Base URL
     */
    public static String getBaseUrl() {
        if (baseUrl == null) {
            baseUrl = StringUtils.stripEnd(TdarConfiguration.getInstance().getBaseSecureUrl().trim(), "/");
        }
        return baseUrl;
    }

    /**
     * Generate an absolute URL for anything that's Addressable (has getUriPart()
     * 
     * @param resource
     * @return
     */
    public static String absoluteUrl(Addressable resource) {
        return String.format("%s%s", StringUtils.stripEnd(getBaseUrl(), "/"), relativeUrl(resource));
    }

    public static String absoluteSecureUrl(Addressable resource) {
        return String.format("%s%s", StringUtils.stripEnd(getBaseUrl(), "/"), relativeUrl(resource));
    }

    /**
     * When passing URLs around, need to reformat them to make sure we don't expose the id=
     * 
     * @param url
     * @return
     */
    public static String reformatViewUrl(String url_) {
        String url = url_;
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
    public static String relativeUrl(Addressable resource) {
        return resource.getDetailUrl();
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
    public static String downloadUrl(InformationResourceFileVersion version) {
        return downloadUrl(version.getInformationResourceFile().getInformationResource(), version);
    }

    /**
     * Generate a download URL
     * 
     * @param version
     * @return
     */
    public static String downloadUrl(InformationResource ir, InformationResourceFileVersion version) {
        return String.format("%s/filestore/get/%d/%d", StringUtils.stripEnd(getBaseUrl(), "/"), ir.getId(), version.getId());
    }

    /**
     * get the URL for a thumbnail image
     * 
     * @return
     */
    public static String thumbnailUrl(InformationResourceFileVersion version) {
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
    public static String getPairedSchemaUrl() {
        return String.format("%s/schema/current schema.xsd", getBaseUrl());
    }

    public static String getProductionSchemaUrl() {
        return "http://core.tdar.org/schema/current schema.xsd";
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

    public static String constructUnAPIFormatUrl(Resource r, String format) {
        String type = null;
        if (format.equalsIgnoreCase("oai_dc") || format.equalsIgnoreCase("dc")) {
            type = "dc";
        } else if (format.equalsIgnoreCase("mods")) {
            type = "mods";
        } else {
            return null;
        }
        return String.format("/unapi/%s/%s", type, r.getId());
    }

    public static String creatorLogoUrl(Creator<?> creator) {
        return String.format("%s/files/creator/sm/%s/logo", StringUtils.stripEnd(getBaseUrl(), "/"), creator.getId());
    }

}
