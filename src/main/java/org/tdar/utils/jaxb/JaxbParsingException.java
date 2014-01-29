package org.tdar.utils.jaxb;

import java.util.List;

public class JaxbParsingException extends Exception {

    private static final long serialVersionUID = 7526380541101762764L;
    private List<JaxbValidationEvent> events;

    public JaxbParsingException(String message, List<JaxbValidationEvent> errors) {
        this.setEvents(errors);
    }

    public List<JaxbValidationEvent> getEvents() {
        return events;
    }

    public void setEvents(List<JaxbValidationEvent> errors) {
        this.events = errors;
    }

    /*
     * FIXME: make me print error messages better!!!
     */
}
