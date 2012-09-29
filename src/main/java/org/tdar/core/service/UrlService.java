package org.tdar.core.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;

/*
 * This service attempts to centralize and support the creation of URL strings from within the java app. It's centralized here
 * to help manage changes, and because we can get the basename from the configuration class
 */
@Service
public class UrlService {

    private String baseUrl;
    public static final String TDAR_NAMESPACE_URL = "http://www.tdar.org/namespace";
    public static final String TDAR_NAMESPACE_PREFIX = "tdar";

    public String getBaseUrl() {
        if (baseUrl == null) {
            baseUrl = StringUtils.stripEnd(TdarConfiguration.getInstance().getBaseUrl().trim(), "/");
        }
        return baseUrl;
    }

    public String absoluteUrl(Resource resource) {
        return String.format("%s%s", StringUtils.stripEnd(getBaseUrl(), "/"),
                relativeUrl(resource));
    }

    public String relativeUrl(Resource resource) {
        return String.format("/%s/%s", resource.getUrlNamespace(),
                resource.getId());
    }

    public String relativeUrl(Creator creator) {
        return String.format("/browse/creators/%s", creator.getId());
    }

    public String downloadUrl(InformationResourceFileVersion version) {
        return String.format("%s/filestore/%d/get", StringUtils.stripEnd(getBaseUrl(), "/"), version.getId());
    }

    public String getSchemaUrl() {
        return String.format("%s/schema/current",getBaseUrl());
    }
}
