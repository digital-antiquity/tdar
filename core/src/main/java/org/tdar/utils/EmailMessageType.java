package org.tdar.utils;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;

public enum EmailMessageType implements Localizable, HasLabel {

    CONTACT("email-form/contact.ftl"),
    REQUEST_ACCESS("email-form/access-request.ftl"),
    SUGGEST_CORRECTION("email-form/correction.ftl"),
    MERGE_PEOPLE("email-form/merge-people.ftl");

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
            if (type != REQUEST_ACCESS) {
                types.add(type);
            }
        }
        return types;
    }

}
