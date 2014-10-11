package org.tdar.struts.data.dataOne;

import java.io.Serializable;
import java.util.Date;

public class DataOneObjectInfo implements Serializable{

    private static final long serialVersionUID = 972722810496645190L;

    /*
     *   <objectInfo>
    <identifier>AnserMatrix.htm</identifier>
    <formatId>eml://ecoinformatics.org/eml-2.0.0</formatId>
    <checksum algorithm="MD5">0e25cf59d7bd4d57154cc83e0aa32b34</checksum>
    <dateSysMetadataModified>1970-05-27T06:12:49</dateSysMetadataModified>
    <size>11048</size>
  </objectInfo>
     */

    private String identifier;
    private String formatId;
    private DataOneChecksum checksum;
    private Date dateSysMetadataModified;
    private Long size;
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getFormatId() {
        return formatId;
    }
    public void setFormatId(String formatId) {
        this.formatId = formatId;
    }
    public DataOneChecksum getChecksum() {
        return checksum;
    }
    public void setChecksum(DataOneChecksum checksum) {
        this.checksum = checksum;
    }
    public Date getDateSysMetadataModified() {
        return dateSysMetadataModified;
    }
    public void setDateSysMetadataModified(Date dateSysMetadataModified) {
        this.dateSysMetadataModified = dateSysMetadataModified;
    }
    public Long getSize() {
        return size;
    }
    public void setSize(Long size) {
        this.size = size;
    }
}
