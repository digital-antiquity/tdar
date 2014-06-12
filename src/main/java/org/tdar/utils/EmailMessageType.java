package org.tdar.utils;

public enum EmailMessageType {

    CONTACT("email-form/contact.ftl"),
    REQUEST_ACCESS("email-form/request-access.ftl"),
    SUGGEST_CORRECTION("email-form/correction.ftl"),
    OTHER("email-form/other.ftl");
    
    private String templateName;
    
    private EmailMessageType(String templateName) {
        this.setTemplateName(templateName);
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
