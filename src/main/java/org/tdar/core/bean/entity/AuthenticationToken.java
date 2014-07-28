package org.tdar.core.bean.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.tdar.core.bean.Persistable;
import org.tdar.utils.MessageHelper;

/**
 * Controller token for managing the session's logged-in user.
 * 
 * @author abrin
 * 
 */
@Entity
@Table(name = "user_session")
public class AuthenticationToken extends Persistable.Base {

    private static final long serialVersionUID = 5012002895141572341L;

    public final static AuthenticationToken INVALID = new AuthenticationToken();

    @Column(name = "tdar_user_id", nullable = false)
    private Long tdarUserId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name = "session_start")
    private Date sessionStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "session_end")
    private Date sessionEnd;

    public static AuthenticationToken create(TdarUser tdarUser) {
        if (tdarUser == null) {
            throw new NullPointerException(MessageHelper.getMessage("authenticationToken.undefined_person"));
        }
        AuthenticationToken token = new AuthenticationToken();
        token.setTdarUserId(tdarUser.getId());
        token.setSessionStart(new Date());
        return token;
    }


    public Date getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(Date timestamp) {
        this.sessionStart = timestamp;
    }

    /**
     * Returns true if this authentication token is valid. Current policy does not utilize timeouts or sessionStart timestamp.
     * 
     * @return true if the TdarUser wrapped by this AuthenticationToken is not null and a registered user of tDAR, false otherwise.
     */
    @Transient
    public boolean isValid() {
        return (Persistable.Base.isNotNullOrTransient(getTdarUserId()));
    }

    public Date getSessionEnd() {
        return sessionEnd;
    }

    public void setSessionEnd(Date sessionEnd) {
        this.sessionEnd = sessionEnd;
    }

    @Override
    public String toString() {
        return "auth token for UserId:" + getTdarUserId() + " with id: " + getId();
    }


    public Long getTdarUserId() {
        return tdarUserId;
    }


    public void setTdarUserId(Long tdarUserId) {
        this.tdarUserId = tdarUserId;
    }


    public void setTdarUser(TdarUser person) {
        setTdarUserId(person.getId());
    }
}
