package org.tdar.core.bean.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
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

    @ManyToOne(optional = false)
    private TdarUser person;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name = "session_start")
    private Date sessionStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "session_end")
    private Date sessionEnd;

    public static AuthenticationToken create(TdarUser person) {
        if (person == null) {
            throw new NullPointerException(MessageHelper.getMessage("authenticationToken.undefined_person"));
        }
        AuthenticationToken token = new AuthenticationToken();
        token.setPerson(person);
        token.setSessionStart(new Date());
        return token;
    }

    public TdarUser getPerson() {
        return person;
    }

    public void setPerson(TdarUser person) {
        this.person = person;
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
     * @return true if the Person wrapped by this AuthenticationToken is not null and a registered user of tDAR, false otherwise.
     */
    @Transient
    public boolean isValid() {
        return (person != null) && person.isRegistered();
    }

    public Date getSessionEnd() {
        return sessionEnd;
    }

    public void setSessionEnd(Date sessionEnd) {
        this.sessionEnd = sessionEnd;
    }

    @Override
    public String toString() {
        return "auth token for " + person + " with id: " + getId();
    }
}
