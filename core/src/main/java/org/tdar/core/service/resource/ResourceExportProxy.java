package org.tdar.core.service.resource;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;

public class ResourceExportProxy implements Serializable {
    private static final long serialVersionUID = -1788611236953747372L;

    private BillingAccount account;
    private RightsBasedResourceCollection collection;
    private List<Resource> resources;

    private TdarUser requestor;

    public ResourceExportProxy(TdarUser requestor) {
        this.setRequestor(requestor);
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    public RightsBasedResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(RightsBasedResourceCollection collection) {
        this.collection = collection;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public String getFilename() {
        return String.format("%s-%s.zip", requestor.getId(), hashCode());
    }

    public TdarUser getRequestor() {
        return requestor;
    }

    public void setRequestor(TdarUser requestor) {
        this.requestor = requestor;
    }

}
