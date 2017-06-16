package org.tdar.search.bean;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

public enum PersonSearchOption implements HasLabel, Localizable {
    
    ALL_FIELDS("allFields", "All Fields"),
    FIRST_NAME("firstName",  "First Name"),
    LAST_NAME("lastName",  "Last Name"),
    USERNAME("username",  "Username"),
    INSTITUTION("institution", "Institution"),
    EMAIL("email", "Email");
	//ID("descriptions", "ID");

	
    private String label = "";
    private String fieldName = "";

    private PersonSearchOption() {

    }

    private PersonSearchOption(String fieldName,  String label) {
        this.label = label;
        this.fieldName = fieldName;
    }


    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public String getFieldName() {
        return this.fieldName;
    }

}
