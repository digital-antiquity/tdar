package org.tdar.core.bean.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

@Entity
@Table(name="user_info")

public class UserInfo extends Persistable.Base implements Obfuscatable {

    private static final long serialVersionUID = 7278077914180784872L;

//    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH }, fetch = FetchType.EAGER)
//    private Person user;

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

    private transient boolean obfuscated;

    private transient Boolean obfuscatedDifferent;

    public Boolean getContributor() {
        return contributor;
    }

    public void setContributor(Boolean contributor) {
        this.contributor = contributor;
    }

    @XmlElement(name = "userRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Person getUser() {
        return null;
    }

    public void setUser(Person user) {
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
    public boolean isObfuscated() {
        return obfuscated;
    }

    @Override
    public List<Obfuscatable> obfuscate() {
        setObfuscated(true);
        setObfuscatedObjectDifferent(true);
        setContributor(false);
        setObfuscated(true);
        setLastLogin(null);
        setPenultimateLogin(null);
        setTotalLogins(null);
        return null;
    }

    @Override
    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    @Override
    public Boolean getObfuscatedObjectDifferent() {
        return obfuscatedDifferent;
    }

    @Override
    public void setObfuscatedObjectDifferent(Boolean value) {
        this.obfuscatedDifferent = value;
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
