package org.tdar.core.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;

@Service
public class UrlService {

    private String baseUrl;

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

    public String downloadUrl(InformationResourceFileVersion version) {
        return String.format("%s/filestore/%d/get", StringUtils.stripEnd(getBaseUrl(), "/"), version.getId());
    }
}
