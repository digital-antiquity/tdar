package org.tdar.utils.jaxb;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IdList extends ArrayList<Long> {

    private static final long serialVersionUID = 1025919231860518950L;

    @XmlElementWrapper(name = "ids")
    public IdList getList() {
        return this;
    }
}