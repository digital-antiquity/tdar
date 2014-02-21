package org.tdar.struts.data.dataOne;

import java.io.Serializable;
import java.util.List;

public class DataOneAccessPolicy implements Serializable{

    /*
     *   <accessPolicy>
    <allow>
      <subject>uid=jdoe,o=NCEAS,dc=ecoinformatics,dc=org</subject>
      <permission>read</permission>
      <permission>write</permission>
      <permission>changePermission</permission>
    </allow>
    <allow>
      <subject>public</subject>
      <permission>read</permission>
    </allow>
    <allow>
      <subject>uid=nceasadmin,o=NCEAS,dc=ecoinformatics,dc=org</subject>
      <permission>read</permission>
      <permission>write</permission>
      <permission>changePermission</permission>
    </allow>
  </accessPolicy>

     */

    private static final long serialVersionUID = -391719644946615885L;

    private List<DataOneAccessAllow> allow;

    public List<DataOneAccessAllow> getAllow() {
        return allow;
    }

    public void setAllow(List<DataOneAccessAllow> allow) {
        this.allow = allow;
    }
}
