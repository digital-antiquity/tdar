package org.tdar.search.bean;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

public enum PersonSearchOption implements HasLabel, Localizable {
    
    ALL_FIELDS("allFields"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    USERNAME("username"),
    INSTITUTION("institution"),
    EMAIL("email");
	
    private String fieldName = "";

    private PersonSearchOption() {

    }

    private PersonSearchOption(String fieldName) {
        this.fieldName = fieldName;
    }


    @Override
    public String getLabel() {
        return MessageHelper.getMessage(getLocaleKey());
    }
    
    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public String getFieldName() {
        return this.fieldName;
    }

}
