package org.tdar.utils.jaxb;

import java.util.List;

import javax.xml.bind.ValidationEvent;

public class JaxbParsingException extends Exception {

    private static final long serialVersionUID = 7526380541101762764L;
    private List<ValidationEvent> events;

    public JaxbParsingException(String message, List<ValidationEvent> errors) {
        this.setEvents(errors);
    }

    public List<ValidationEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ValidationEvent> errors) {
        this.events = errors;
    }
    
    /*
     * FIXME: make me print error messages better!!!
     */
}
