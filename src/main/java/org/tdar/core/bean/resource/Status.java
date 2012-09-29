package org.tdar.core.bean.resource;

public enum Status {
    DRAFT("Draft"),
    ACTIVE("Active"),
    FLAGGED("Flagged"),
    DELETED("Deleted");
    
    private final String label;
    
    private Status(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
    public static Status fromString(String string) {
        if (string == null || "".equals(string)) {
            return null;
        }
        // try to convert incoming resource type String query parameter to ResourceType enum.. unfortunately valueOf only throws RuntimeExceptions.
        try {
            return Status.valueOf(string);
        }
        catch (Exception exception) {
            return null;
        }
    }

}
