package org.tdar.struts;

public enum WROProfile {
    DEFAULT, NG_INTEGRATE;

    public String getProfileName() {
        switch (this) {
            case NG_INTEGRATE:
                return "ng-integrate";
            default:
                return this.name().toLowerCase();
        }
    }
}
