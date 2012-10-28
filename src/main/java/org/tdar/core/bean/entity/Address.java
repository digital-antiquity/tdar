package org.tdar.core.bean.entity;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Persistable.Base;

public class Address extends Base implements Persistable {

    /**
     * 
     */
    private static final long serialVersionUID = 3179122792715811371L;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String postal;
    private String phone;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AddressType getType() {
        return type;
    }

    public void setType(AddressType type) {
        this.type = type;
    }

}
