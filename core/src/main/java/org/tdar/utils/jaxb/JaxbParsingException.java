package org.tdar.utils.jaxb;

import java.util.List;

public class JaxbParsingException extends Exception {

    private static final long serialVersionUID = 7526380541101762764L;
    private List<String> events;

    public JaxbParsingException(String message, List<String> errors) {
        this.setEvents(errors);
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> errors) {
        this.events = errors;
    }

    @Override
    public String getMessage() {
        return String.format("%s [%s]", super.getMessage(), events);
    }
}
