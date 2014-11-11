package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;

/**
 * A downloadauth designates a collection of resources that allow file downloads from unauthenticated users.   Instead, the download request includes
 * an API key that effectively serves as an alternate form of authentication.
 */
//FIXME: come up with better class name
public class DownloadAuthorization extends Persistable.Base {
    private static final long serialVersionUID = 0x1;

    @ManyToOne
    @JoinColumn(name="resource_collection_id")
    private ResourceCollection resourceCollection;

    @ManyToOne
    @JoinColumn(name="institution_id")
    private Institution institution;

    @Column(name="api_key")
    private String apiKey;

    @Column(name="referer_regex")
    private String refererRegex;

    public ResourceCollection getResourceCollection() {
        return resourceCollection;
    }

    public void setResourceCollection(ResourceCollection resourceCollection) {
        this.resourceCollection = resourceCollection;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getRefererRegex() {
        return refererRegex;
    }

    public void setRefererRegex(String refererRegex) {
        this.refererRegex = refererRegex;
    }
}
