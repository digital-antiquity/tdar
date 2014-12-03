package org.tdar.struts.data.dataOne;

import java.io.Serializable;
import java.util.List;

public class DataOneAccessAllow implements Serializable {

    private static final long serialVersionUID = 6005948585578264585L;

    /*
     *     <allow>
          <subject>uid=nceasadmin,o=NCEAS,dc=ecoinformatics,dc=org</subject>
          <permission>read</permission>
          <permission>write</permission>
          <permission>changePermission</permission>
        </allow>
     */
    
    private enum DataOneAccessPermission {
        read, write, changePermission;
    }
    
    private String subject;
    private List<DataOneAccessPermission> permission;

    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public List<DataOneAccessPermission> getPermission() {
        return permission;
    }
    public void setPermission(List<DataOneAccessPermission> permission) {
        this.permission = permission;
    }
}
