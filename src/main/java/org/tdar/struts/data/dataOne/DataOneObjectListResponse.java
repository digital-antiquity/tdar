package org.tdar.struts.data.dataOne;

import java.io.Serializable;
import java.util.List;

public class DataOneObjectListResponse implements Serializable{

    private static final long serialVersionUID = -5779995526010967389L;

    private List<DataOneObjectInfo> objectInfo;

    public List<DataOneObjectInfo> getObjectInfo() {
        return objectInfo;
    }

    public void setObjectInfo(List<DataOneObjectInfo> objectInfo) {
        this.objectInfo = objectInfo;
    }
}
