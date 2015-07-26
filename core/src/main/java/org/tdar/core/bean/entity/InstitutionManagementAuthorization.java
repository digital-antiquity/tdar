package org.tdar.core.bean.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;
import org.tdar.core.bean.Persistable;

@Entity
@Table(name = "institution_authorization")
@XmlRootElement(name = "institution_authorization")
/**
 * Class to manage users who can edit institutions.  The goal here is to allow users to "apply" to own an institution, and then add a workflow for staff to 
 * authorize editing.
 *  
 * @author abrin
 *
 */
public class InstitutionManagementAuthorization extends Persistable.Base {

    private static final long serialVersionUID = -5841183738497500709L;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH }, optional = false)
    private TdarUser user;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH }, optional = false)
    private Institution institution;

    @Column(name = "authorized", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean authorized = false;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @NotNull
    private String reason;

    public InstitutionManagementAuthorization() {

    }

    public InstitutionManagementAuthorization(Institution institution, TdarUser user) {
        this.institution = institution;
        this.user = user;
    }

    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
