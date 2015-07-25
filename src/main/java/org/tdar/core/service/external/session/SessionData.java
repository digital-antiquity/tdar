package org.tdar.core.service.external.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.utils.PersistableUtils;

/**
 * $Id$
 * <p>
 * Stores type-safe data in a user's Session. Object creation is managed by Spring as a session-scoped bean, i.e., there should be one instance per http
 * session.
 * </p>
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class SessionData implements Serializable {

    private static final long serialVersionUID = 2786144717909265676L;

    private String returnUrl;
    private String[] parameters;
    private Long tdarUserId;
    private Long invoiceId;

    public void clearAuthenticationToken() {
        this.parameters = null;
        this.tdarUserId = null;
        this.invoiceId = null;
        clearPassthroughParameters();
    }

    public void clearPassthroughParameters() {
        this.returnUrl = null;
    }

    public boolean isAuthenticated() {
        return PersistableUtils.isNotNullOrTransient(tdarUserId);
    }

    @Override
    public String toString() {
        return String.format("Auth user: %s [object id: %s]", tdarUserId, super.toString());
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    /**
     * appends parameters if any to the url map.
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = new String[parameters.size() * 2];
        int index = 0;
        ArrayList<String> queryParams = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            String paramValue = ((String[]) entry.getValue())[0];
            this.parameters[index++] = paramName;
            this.parameters[index++] = paramValue;
            queryParams.add(paramName + "=" + paramValue);
        }
        if (!queryParams.isEmpty()) {
            String query = "?" + StringUtils.join(queryParams, '&');
            returnUrl += query;
        }
    }

    public String[] getParameters() {
        return parameters;
    }

    public Long getTdarUserId() {
        return tdarUserId;
    }

    public void setTdarUser(TdarUser user) {
        if (PersistableUtils.isNotNullOrTransient(user)) {
            this.tdarUserId = user.getId();
        } else {
            this.tdarUserId = null;
        }
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

}
