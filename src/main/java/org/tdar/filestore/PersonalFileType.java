package org.tdar.filestore;

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
