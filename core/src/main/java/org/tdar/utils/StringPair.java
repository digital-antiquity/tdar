package org.tdar.utils;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.Validatable;

/**
 * For tracking KeyValuePairs for ResourceAnnotatiosn
 * 
 * @author Abrin
 * 
 */
public class StringPair extends Pair<String, String> implements Validatable {

    private static final long serialVersionUID = -737014414854726705L;

    public StringPair() {
        super(null, null);
    }

    public StringPair(String first, String second) {
        super(first, second);
    }

    public void setKey(String key) {
        setFirst(key);
    }

    public void setValue(String value) {
        setSecond(value);
    }

    public String getKey() {
        return getFirst();
    }

    public String getValue() {
        return getSecond();
    }

    @Override
    public boolean isValid() {
        if (StringUtils.isBlank(getFirst()) && StringUtils.isBlank(getSecond())) {
            return false;
        }

        return StringUtils.isNotBlank(getFirst()) || StringUtils.isNotBlank(getSecond());
    }

    public boolean isInitialized() {
        if (StringUtils.isBlank(getFirst()) && StringUtils.isBlank(getSecond())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isValidForController() {
        return isValid();
    }

}
