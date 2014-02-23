package org.tdar.filestore.personal;

import org.tdar.utils.MessageHelper;

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
