package org.tdar.struts.data.dataOne;

import java.io.Serializable;

public class DataOneChecksum implements Serializable {

    private static final long serialVersionUID = -9060044082881157381L;

    private String algorithm;
    private String checksum;
    
    /*
     *   <checksum algorithm="MD5">e7451c1775461b13987d7539319ee41f</checksum>
     */

    public String getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    public String getChecksum() {
        return checksum;
    }
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    
}
