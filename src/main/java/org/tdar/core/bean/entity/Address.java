package org.tdar.core.bean.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.Validatable;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.core.exception.TdarValidationException;

@Entity
@Table(name = "creator_address")
public class Address extends Base implements Persistable, Validatable {

    public static final String ADDRESS_TYPE_IS_REQUIRED = "an address type is required";
    public static final String POSTAL_CODE_IS_REQUIRED = "a postal code is required";
    public static final String COUNTRY_IS_REQUIRED = "a country is required";
    public static final String STATE_IS_REQUIRED = "a state is required";
    public static final String CITY_IS_REQUIRED = "a city is required";
    public static final String STREET_ADDRESS_IS_REQUIRED = "a street address is required";

    private static final long serialVersionUID = 3179122792715811371L;

    @NotNull
    private String street1;
    private String street2;
    @NotNull
    private String city;
    @NotNull
    private String state;
    @NotNull
    private String postal;
    @NotNull
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
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
    @JSONTransient
    @XmlTransient
    public boolean isValidForController() {
        return isValid();
    }

    @Override
    @JSONTransient
    @XmlTransient
    public boolean isValid() {
        if (StringUtils.isBlank(street1)) {
            throw new TdarValidationException(STREET_ADDRESS_IS_REQUIRED);
        }
        if (StringUtils.isBlank(city)) {
            throw new TdarValidationException(CITY_IS_REQUIRED);
        }
        if (StringUtils.isBlank(state)) {
            throw new TdarValidationException(STATE_IS_REQUIRED);
        }
        if (StringUtils.isBlank(country)) {
            throw new TdarValidationException(COUNTRY_IS_REQUIRED);
        }
        if (StringUtils.isBlank(postal)) {
            throw new TdarValidationException(POSTAL_CODE_IS_REQUIRED);
        }
        if (type == null) {
            throw new TdarValidationException(ADDRESS_TYPE_IS_REQUIRED);
        }
        return true;
    }

}
