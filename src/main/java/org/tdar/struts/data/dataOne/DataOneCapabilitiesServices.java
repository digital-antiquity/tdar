package org.tdar.struts.data.dataOne;

import java.io.Serializable;

public class DataOneCapabilitiesServices implements Serializable {
    private static final long serialVersionUID = 3259392481194005479L;

    private String name;
    private String version;
    private boolean available;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }


}
