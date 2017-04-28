package org.tdar.utils;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;

public enum EmailMessageType implements Localizable, HasLabel {

    CONTACT("email-form/contact.ftl"),
    REQUEST_ACCESS("email-form/access-request.ftl"),
    SUGGEST_CORRECTION("email-form/correction.ftl"),
    MERGE_PEOPLE("email-form/merge-people.ftl"),
    CUSTOM("email-form/custom-request.ftl");

    private String templateName;

    public boolean requiresResource() {
        return true;
    }

    private EmailMessageType(String templateName) {
        this.setTemplateName(templateName);
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public String getLabel() {
        return MessageHelper.getMessage(getLocaleKey());
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public static List<EmailMessageType> valuesWithoutConfidentialFiles() {
        ArrayList<EmailMessageType> types = new ArrayList<EmailMessageType>();
        for (EmailMessageType type : values()) {
            switch (type) {
                case REQUEST_ACCESS:
                case CUSTOM:
                case MERGE_PEOPLE:
                    break;
                default:
                    types.add(type);
            }
        }
        return types;
    }

	public static List<EmailMessageType> valuesWithoutCustom() {
        ArrayList<EmailMessageType> types = new ArrayList<EmailMessageType>();
        for (EmailMessageType type : values()) {
            if (type != CUSTOM && type != MERGE_PEOPLE) {
                types.add(type);
            }
        }
        return types;
	}

}
