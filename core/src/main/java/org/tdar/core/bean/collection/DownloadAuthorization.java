package org.tdar.core.bean.collection;

import java.beans.Transient;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.tdar.core.bean.AbstractPersistable;

/**
 * A downloadauth designates a collection of resources that allow file downloads from unauthenticated users. Instead, the download request includes
 * an API key that effectively serves as an alternate form of authentication.
 */
@Entity
@Table(name = "download_authorization")
@XmlRootElement(name = "DownloadAuthorization")
public class DownloadAuthorization extends AbstractPersistable {

    private static final long serialVersionUID = 2087845303008388117L;
    private static final int UUID_BEGIN_INDEX = 24;

    @Column(name = "api_key")
    private String apiKey;

    /**
     * Fully qualified host name
     */
    @ElementCollection()
    @CollectionTable(name = "referrer_hostnames", joinColumns = @JoinColumn(name = "download_authorization_id"))
    @Column(name = "hostname")
    private Set<String> refererHostnames = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "resource_collection_id")
    private SharedCollection sharedCollection;

    private static String generateSimpleKey() {
        return "d" + UUID.randomUUID().toString().substring(UUID_BEGIN_INDEX);
    };

    // zero-arg constructor for hibernate (does not generate key)
    public DownloadAuthorization() {
    }

    /**
     * Create new downloadAuthorization w/ default api key for the specified resourceCollection.
     * 
     * @param resourceCollection
     */
    public DownloadAuthorization(SharedCollection resourceCollection) {
        this(resourceCollection, generateSimpleKey());
    }

    /**
     * Create new downloadAuth with specified resourceCollection & key
     * 
     * @param resourceCollection
     * @param apiKey
     */
    public DownloadAuthorization(SharedCollection resourceCollection, String apiKey) {
        this.sharedCollection = resourceCollection;
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public SharedCollection getSharedCollection() {
        return sharedCollection;
    }

    public void setSharedCollection(SharedCollection resourceCollection) {
        this.sharedCollection = resourceCollection;
    }

    public Set<String> getRefererHostnames() {
        return refererHostnames;
    }

    public void setRefererHostnames(Set<String> refererHostnames) {
        this.refererHostnames = refererHostnames;
    }

    /**
     * Returns true if downloads are supported for requests from any referrer. The convention for this policy is for a
     * DownloadAuthorization object to have a single referrerHostname with a value of '*' (the asterisk symbol).
     * 
     * @return
     */
    // @Transient
    // public boolean isAnyReferrerAllowed() {
    // if(CollectionUtils.isEmpty(refererHostnames)) return false;
    // return refererHostnames.size() == 1 && refererHostnames.iterator().next().equals("*");
    // }

    @Transient
    public boolean isAnyReferrerAllowed() {
        // FIXME: needs implementation
        return false;
    }

    public boolean isReferrerAllowed(String referrerHostname) {
        return isAnyReferrerAllowed() || refererHostnames.contains(referrerHostname);
    }

}
