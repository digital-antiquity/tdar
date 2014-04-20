package org.tdar.core.bean.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
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
    
    public TdarUser(String firstName, String lastName, String email) {
        super(firstName, lastName, email);
    }
    
    @Column(unique = true, nullable = true)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String username;

    // did this user register with the system or were they entered by someone
    // else?
    @Field
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    private transient boolean registered = true;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
