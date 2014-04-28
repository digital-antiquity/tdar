package org.tdar.core.bean.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;

@Entity
@Indexed
@Table(name = "tdar_user")
@XmlRootElement(name = "user")
public class TdarUser extends Person {

    private static final long serialVersionUID = 6232922939044373880L;

    public TdarUser() {
        // TODO Auto-generated constructor stub
    }
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn(name="user_id")
    @NotNull
    private UserInfo userInfo;
    
    public TdarUser(String firstName, String lastName, String email) {
        super(firstName, lastName, email);
    }
    
    @Column(unique = true, nullable = true)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public List<Obfuscatable> obfuscate() {
        List<Obfuscatable> results = new ArrayList<>();
        results.addAll(super.obfuscate());
        results.add(getUserInfo());
        return results;
    };
    
    @Field
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    public boolean isRegistered() {
        return true;
    }

    @Override
    public boolean isDedupable() {
        return false;
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

}
