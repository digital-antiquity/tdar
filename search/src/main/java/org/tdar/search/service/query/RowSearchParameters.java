package org.tdar.search.service.query;

import java.io.Serializable;

/**
 * key value pair for searching for a row in a tDAR dataset
 * @author abrin
 *
 */
public class RowSearchParameters implements Serializable {

    private static final long serialVersionUID = 3407412591573437580L;
    private String key;
    private Object value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
