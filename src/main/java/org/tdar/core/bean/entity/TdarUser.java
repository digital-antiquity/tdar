package org.tdar.core.bean.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;

@Entity
@Indexed
@Table(name = "tdar_user")
@XmlRootElement(name = "user")
@Check(constraints="username <> ''")
public class TdarUser extends Person {

    private static final long serialVersionUID = 6232922939044373880L;

    public TdarUser() {}
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person")
    private Set<BookmarkedResource> bookmarkedResources = new LinkedHashSet<>();

    public TdarUser(String firstName, String lastName, String email) {
        super(firstName, lastName, email);
    }

    @Column(unique = true, nullable = true)
    @Length(min = 1, max = FieldLength.FIELD_LENGTH_255)
    private String username;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH }, optional = true)
    /* who to contact when owner is no longer 'reachable' */
    private Institution proxyInstitution;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name = "proxy_note")
    private String proxyNote;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    private Date lastLogin;

    @Enumerated(EnumType.STRING)
    @Column(name = "affilliation", length = FieldLength.FIELD_LENGTH_255)
    @Field(norms = Norms.NO, store = Store.YES)
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
    private UserAffiliation affilliation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "penultimate_login")
    private Date penultimateLogin;

    @Column(name = "total_login")
    private Long totalLogins = 0l;

    // can this user contribute resources?
    @Column(name = "contributor", nullable = false, columnDefinition = "boolean default FALSE")
    private Boolean contributor = Boolean.FALSE;

    @Column(name = "contributor_reason", length = FieldLength.FIELD_LENGTH_512)
    @Length(max = FieldLength.FIELD_LENGTH_512)
    private String contributorReason;

    // version of the latest TOS that the user has accepted
    @Column(name = "tos_version", nullable = false, columnDefinition = "int default 0")
    private Integer tosVersion = 0;

    // version of the latest Creator Agreement that the user has accepted
    @Column(name = "contributor_agreement_version", nullable = false, columnDefinition = "int default 0")
    private Integer contributorAgreementVersion = 0;

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
        this.penultimateLogin = this.lastLogin;
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
    
    @Field
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    public boolean isRegistered() {
        return true;
    }

    @Override
    public boolean isDedupable() {
        return false;
    }

    @XmlTransient
    public Set<BookmarkedResource> getBookmarkedResources() {
        return bookmarkedResources;
    }

    public void setBookmarkedResources(Set<BookmarkedResource> bookmarkedResources) {
        this.bookmarkedResources = bookmarkedResources;
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

    public UserAffiliation getAffilliation() {
        return affilliation;
    }

    public void setAffilliation(UserAffiliation affilliation) {
        this.affilliation = affilliation;
    }
}
