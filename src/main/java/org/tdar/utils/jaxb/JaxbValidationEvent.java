package org.tdar.utils.jaxb;

import java.io.Serializable;

import javax.xml.bind.ValidationEvent;

import org.apache.commons.lang.exception.ExceptionUtils;

public class JaxbValidationEvent implements Serializable {

    private static final long serialVersionUID = 718172786425901367L;

    private ValidationEvent event;

    private String line;

    public JaxbValidationEvent() {
    }

    public JaxbValidationEvent(ValidationEvent event, String line) {
        this.event = event;
        this.line = line;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public ValidationEvent getEvent() {
        return event;
    }

    public void setEvent(ValidationEvent event) {
        this.event = event;
    }

    public String getSeverity() {
        switch (this.event.getSeverity()) {
            case 0:
                return "WARNING";
            case 1:
                return "ERROR";
            case 2:
                return "FATAL ERROR";
            default:
                return "INFO";
        }
    }

    @Override
    public String toString() {
        String err = ExceptionUtils.getFullStackTrace(event.getLinkedException());

        return String.format("[%s] %s line: %s column: %s  { %s } %s ", getSeverity(), event.getMessage(), event.getLocator().getLineNumber(), event
                .getLocator().getColumnNumber(), line, err);
    }
}
