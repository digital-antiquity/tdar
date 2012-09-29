package org.tdar.filestore.personalFilestore;

public enum PersonalFileType {
    UPLOAD("uploads"),
    INTEGRATION("integrations");
    
    private final String pathname;
    
    private PersonalFileType(String pathname) {
        this.pathname = pathname;
    }

    public String getPathname() {
        return pathname;
    }
}
