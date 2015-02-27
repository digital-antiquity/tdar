package org.tdar.search.query;

import java.io.Serializable;

/**
 * Abstraction to represent something that can be faceted. This allows us to store data in enums.
 * 
 * @author abrin
 * 
 * @param <F>
 */
public class FacetValue implements Serializable {

    private static final long serialVersionUID = 4542837723319045798L;
    private Integer count;
    private String key;
    private String value;
    private String pluralKey;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPluralKey() {
        return pluralKey;
    }

    public void setPluralKey(String pluralKey) {
        this.pluralKey = pluralKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
