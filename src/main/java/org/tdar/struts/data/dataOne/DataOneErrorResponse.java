package org.tdar.struts.data.dataOne;

import java.io.Serializable;

public class DataOneErrorResponse implements Serializable {

    private static final long serialVersionUID = 8973778420703944878L;
    private int code;
    private String detailCode;
    private String pid;
    private String nodeId;
    private String description;
    private String traceInformation;
    
/*
 * <?xml version="1.0" encoding="UTF-8"?>
<error detailCode="1800" errorCode="404" name="NotFound">
   <description>No system metadata could be found for given PID: DOESNTEXIST</description>
</error>    
 */

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getDetailCode() {
        return detailCode;
    }
    public void setDetailCode(String detailCode) {
        this.detailCode = detailCode;
    }
    public String getPid() {
        return pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
    }
    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getTraceInformation() {
        return traceInformation;
    }
    public void setTraceInformation(String traceInformation) {
        this.traceInformation = traceInformation;
    }

}
