package org.tdar.struts.action.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RequiredOptionalPairs implements Serializable {

    private static final long serialVersionUID = -7764737864438622589L;

    List<String> required = new ArrayList<>();
    List<String> optional = new ArrayList<>();

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public List<String> getOptional() {
        return optional;
    }

    public void setOptional(List<String> optional) {
        this.optional = optional;
    }

}
