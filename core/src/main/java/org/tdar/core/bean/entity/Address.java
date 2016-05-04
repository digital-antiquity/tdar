package org.tdar.core.bean.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.exception.TdarValidationException;

/**
 * Represents a physical address for a person or institution.
 * 
 * @author abrin
 * 
 */
@Entity
@Table(name = "creator_address")
public class Address extends AbstractPersistable implements Persistable, Validatable {

    private static final long serialVersionUID = 3179122792715811371L;

    @NotNull
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String street1;
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String street2;
    @NotNull
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String city;
    @NotNull
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String state;
    @NotNull
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String postal;
    @NotNull
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = FieldLength.FIELD_LENGTH_255)
    @NotNull
    private AddressType type;

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    @Override
    public String toString() {
        return String.format("# %s : %s, %s [%s, %s, %s, %s] %s", getId(), street1, street2, city, state, postal, country, type);
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostal() {
        return postal;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public AddressType getType() {
        return type;
    }

    public void setType(AddressType type) {
        this.type = type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Address() {
    }

    public Address(AddressType type, String street1, String city, String state, String postal, String country) {
        this.type = type;
        this.street1 = street1;
        this.state = state;
        this.city = city;
        this.postal = postal;
        this.country = country;
    }

    @Override
    @XmlTransient
    public boolean isValidForController() {
        return isValid();
    }

    @Override
    @XmlTransient
    public boolean isValid() {
        if (StringUtils.isBlank(street1)) {
            throw new TdarValidationException("address.street_required");
        }
        if (StringUtils.isBlank(city)) {
            throw new TdarValidationException("address.city_required");
        }
        if (StringUtils.isBlank(state)) {
            throw new TdarValidationException("address.state_required");
        }
        if (StringUtils.isBlank(country)) {
            throw new TdarValidationException("address.country_required");
        }
        if (StringUtils.isBlank(postal)) {
            throw new TdarValidationException("address.postal_required");
        }
        if (type == null) {
            throw new TdarValidationException("address.type_required");
        }
        return true;
    }

    public String getAddressSingleLine() {
        StringBuilder sb = new StringBuilder(getStreet1());
        if ((sb.length() > 0) && StringUtils.isNotBlank(getStreet2())) {
            sb.append(" ").append(getStreet2());
        }
        if (sb.length() > 0) {
            sb.append(". ");
        }
        sb.append(getCity()).append(", ").append(getState()).append(" ").append(getPostal());
        if (StringUtils.isNotBlank(getCountry())) {
            sb.append(". ").append(getCountry());
        }
        sb.append("(").append(getType().getLabel()).append(")");
        return sb.toString();
    }

    public boolean isSameAs(Address address) {
        return getHashCodeForComparison() == address.getHashCodeForComparison();
    }

    private int getHashCodeForComparison() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 7);
        builder.append(getStreet1()).append(getStreet2()).append(getCity()).append(getState()).append(getPostal()).append(getCountry());
        return builder.toHashCode();
    }
}
