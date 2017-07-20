package org.tdar.core.dao;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.resource.Resource;

/**
 * A simple data wrapper for aggregate statistics. The goal of this class is to represent a row of data. We use this instead of a pair in case we want to use
 * JAXB to serialize.
 * 
 * @author abrin
 *
 */
public class ResourceStatWrapper implements Serializable {

    private static final long serialVersionUID = 4820345046589228017L;

    private Resource resource;
    private List<Number> data;

    public ResourceStatWrapper(Resource resource2, List<Number> numbers) {
        this.resource = resource2;
        this.data = numbers;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public List<Number> getData() {
        return data;
    }

    public void setData(List<Number> data) {
        this.data = data;
    }

}
