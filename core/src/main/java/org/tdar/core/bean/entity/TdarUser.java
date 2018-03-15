package org.tdar.core.bean.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.utils.json.JsonAdminLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
//@Indexed
@Table(name = "tdar_user")
@XmlRootElement(name = "user")
@Check(constraints = "username <> ''")
public class TdarUser extends Person {

    private static final long serialVersionUID = 6232922939044373880L;

    @Column(unique = true, nullable = true)
    @Length(min = 1, max = FieldLength.FIELD_LENGTH_255)
    private String username;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH }, optional = true)
    /* who to contact when owner is no longer 'reachable' */
    private Institution proxyInstitution;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "proxy_note")
    private String proxyNote;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    private Date lastLogin;

    @Enumerated(EnumType.STRING)
    @Column(name = "affiliation", length = FieldLength.FIELD_LENGTH_255)
    private UserAffiliation affiliation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "penultimate_login")
    private Date penultimateLogin;

    @Column(name = "total_login")
    private Long totalLogins = 0L;

    @Column(name = "total_downloads")
    private Long totalDownloads = 0L;

    // can this user contribute resources?
    @Column(name = "contributor", nullable = false, columnDefinition = "boolean default FALSE")
    private Boolean contributor = Boolean.FALSE;

    @Column(name = "contributor_reason", length = FieldLength.FIELD_LENGTH_512)
    @Length(max = FieldLength.FIELD_LENGTH_512)
    private String contributorReason;

    @Column(name = "user_agent", length = FieldLength.FIELD_LENGTH_512)
    @Length(max = FieldLength.FIELD_LENGTH_512)
    private String userAgent;

    // version of the latest TOS that the user has accepted
    @Column(name = "tos_version", nullable = false, columnDefinition = "int default 0")
    private Integer tosVersion = 0;

    // version of the latest Creator Agreement that the user has accepted
    @Column(name = "contributor_agreement_version", nullable = false, columnDefinition = "int default 0")
    private Integer contributorAgreementVersion = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dismissed_notifications_date", nullable = true)
    private Date dismissedNotificationsDate;

    @Column(name = "start_as_draft", nullable = false, columnDefinition = "boolean default false")
    private Boolean newResourceSavedAsDraft = Boolean.FALSE;

    public TdarUser() {
    }

    public TdarUser(String firstName, String lastName, String email, String username, Long id) {
        this(firstName, lastName, email, username);
        setId(id);
    }
    public TdarUser(String firstName, String lastName, String email) {
        super(firstName, lastName, email);
    }

    public TdarUser(String firstName, String lastName, String email, String username) {
        super(firstName, lastName, email);
        this.username = username;
    }

    public TdarUser(Person person, String username) {
        super(person.getFirstName(),person.getLastName(), person.getEmail());
        this.setUsername(username);
    }

    public Boolean isContributor() {
        return contributor;
    }

    public Boolean getContributor() {
        return contributor;
    }

    public void setContributor(Boolean contributor) {
        this.contributor = contributor;
    }

    @XmlTransient
    public String getContributorReason() {
        return contributorReason;
    }

    public void setContributorReason(String contributorReason) {
        this.contributorReason = contributorReason;
    }

    public Long getTotalLogins() {
        if (totalLogins == null) {
            return 0L;
        }
        return totalLogins;
    }

    public void setTotalLogins(Long totalLogins) {
        this.totalLogins = totalLogins;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.penultimateLogin = getLastLogin();
        this.lastLogin = lastLogin;
    }

    public void incrementLoginCount() {
        totalLogins = getTotalLogins() + 1;
    }

    public Date getPenultimateLogin() {
        return penultimateLogin;
    }

    public void setPenultimateLogin(Date penultimateLogin) {
        this.penultimateLogin = penultimateLogin;
    }

    @Override
    public Set<Obfuscatable> obfuscate() {
        Set<Obfuscatable> results = new HashSet<>();
        setObfuscated(true);
        results.addAll(super.obfuscate());
        setObfuscatedObjectDifferent(true);
        setContributor(false);
        setObfuscated(true);
        setLastLogin(null);
        setPenultimateLogin(null);
        setTotalLogins(null);
        return results;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonView(JsonAdminLookupFilter.class)
    @Override
    public boolean isRegistered() {
        return true;
    }

    @Override
    public boolean isDedupable() {
        return false;
    }

    public Institution getProxyInstitution() {
        return proxyInstitution;
    }

    public void setProxyInstitution(Institution proxyInstitution) {
        this.proxyInstitution = proxyInstitution;
    }

    public String getProxyNote() {
        return proxyNote;
    }

    public void setProxyNote(String proxyNote) {
        this.proxyNote = proxyNote;
    }

    public Integer getContributorAgreementVersion() {
        return contributorAgreementVersion;
    }

    public void setContributorAgreementVersion(Integer contributorAgreementVersion) {
        this.contributorAgreementVersion = contributorAgreementVersion;
    }

    public Integer getTosVersion() {
        return tosVersion;
    }

    public void setTosVersion(Integer tosVersion) {
        this.tosVersion = tosVersion;
    }

    public UserAffiliation getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(UserAffiliation affiliation) {
        this.affiliation = affiliation;
    }

    public Date getDismissedNotificationsDate() {
        return dismissedNotificationsDate;
    }

    public void updateDismissedNotificationsDate() {
        setDismissedNotificationsDate(new Date());
    }

    public void setDismissedNotificationsDate(Date dismissedNotificationsDate) {
        this.dismissedNotificationsDate = dismissedNotificationsDate;
    }

    public Boolean getNewResourceSavedAsDraft() {
        return newResourceSavedAsDraft;
    }

    public void setNewResourceSavedAsDraft(Boolean newResourceSavedAsDraft) {
        this.newResourceSavedAsDraft = newResourceSavedAsDraft;
    }

    public Long getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(Long totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Transient
    @Override
    public boolean hasNoPersistableValues() {
        return StringUtils.isBlank(username) && super.hasNoPersistableValues();
    }
}
