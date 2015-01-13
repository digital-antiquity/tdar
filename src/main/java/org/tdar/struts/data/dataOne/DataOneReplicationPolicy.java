package org.tdar.struts.data.dataOne;

import java.io.Serializable;

public class DataOneReplicationPolicy implements Serializable {

    private static final long serialVersionUID = -8202364442848053511L;

    /*
     *   <replicationPolicy replicationAllowed="false"/>
     */
    
    private boolean replicationAllowed = false;

    public boolean isReplicationAllowed() {
        return replicationAllowed;
    }

    public void setReplicationAllowed(boolean replicationAllowed) {
        this.replicationAllowed = replicationAllowed;
    }
}
