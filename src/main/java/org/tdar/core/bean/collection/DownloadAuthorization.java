package org.tdar.core.bean.collection;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.tdar.core.bean.Persistable;

/**
 * A downloadauth designates a collection of resources that allow file downloads from unauthenticated users. Instead, the download request includes
 * an API key that effectively serves as an alternate form of authentication.
 */
@Entity
@Table(name = "download_authorization")
@XmlRootElement(name = "DownloadAuthorization")
public class DownloadAuthorization extends Persistable.Base {

    private static final long serialVersionUID = 2087845303008388117L;

    @Column(name = "api_key")
    private String apiKey;

    @ElementCollection()
    @CollectionTable(name = "referrer_hostnames", joinColumns = @JoinColumn(name = "download_authorization_id"))
    @Column(name = "hostname")
    private Set<String> refererHostnames = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "resource_collection_id")
    private ResourceCollection resourceCollection;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public ResourceCollection getResourceCollection() {
        return resourceCollection;
    }

    public void setResourceCollection(ResourceCollection resourceCollection) {
        this.resourceCollection = resourceCollection;
    }

    public Set<String> getRefererHostnames() {
        return refererHostnames;
    }

    public void setRefererHostnames(Set<String> refererHostnames) {
        this.refererHostnames = refererHostnames;
    }

}
