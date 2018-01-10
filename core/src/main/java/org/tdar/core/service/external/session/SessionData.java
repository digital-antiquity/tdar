package org.tdar.core.service.external.session;

import java.io.Serializable;

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

    private String[] parameters;
    private Long tdarUserId;
    private Long invoiceId;
    private String username;

    public void clearAuthenticationToken() {
        this.parameters = null;
        this.tdarUserId = null;
        this.invoiceId = null;
        this.username = null;
    }

    public boolean isAuthenticated() {
        return PersistableUtils.isNotNullOrTransient(tdarUserId);
    }

    @Override
    public String toString() {
        return String.format("Auth user: %s [object id: %s]", tdarUserId, super.toString());
    }


    public String[] getParameters() {
        return parameters;
    }

    public Long getTdarUserId() {
        return tdarUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setTdarUser(TdarUser user) {
        if (PersistableUtils.isNotNullOrTransient(user)) {
            this.tdarUserId = user.getId();
            this.username = user.getUsername();
        } else {
            this.tdarUserId = null;
            this.username = null;
        }
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

}
