package org.tdar.struts.data.dataOne;

import java.io.Serializable;
import java.util.Date;

public class DataOneSystemMetadataResponse implements Serializable {

    private static final long serialVersionUID = -4310395814860115299L;
    private int serialVersion;
    private String identifier;
    private String formatId;
    private long size;
    private DataOneChecksum checksum;
    private String submitter;
    private String rightsHolder;
    private DataOneAccessPolicy accessPolicy;
    private DataOneReplicationPolicy replicationPolicy;
    private String obsoletes;
    private String obsoletedBy;
    private boolean archived;
    private Date dateUploaded;
    private Date dateSysMetadataModified;
    private String originMemberNode;
    private String authoritativeMemberNode;
/*
 * <?xml version="1.0" encoding="UTF-8"?>
<d1:systemMetadata xmlns:d1="http://ns.dataone.org/service/types/v1">
  <serialVersion>1</serialVersion>
  <identifier>XYZ332</identifier>
  <formatId>eml://ecoinformatics.org/eml-2.1.0</formatId>
  <size>20875</size>
  <checksum algorithm="MD5">e7451c1775461b13987d7539319ee41f</checksum>
  <submitter>uid=mbauer,o=NCEAS,dc=ecoinformatics,dc=org</submitter>
  <rightsHolder>uid=mbauer,o=NCEAS,dc=ecoinformatics,dc=org</rightsHolder>
  <accessPolicy>
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
  <replicationPolicy replicationAllowed="false"/>
  <obsoletes>XYZ331</obsoletes>
  <obsoletedBy>XYZ333</obsoletedBy>
  <archived>true</archived>
  <dateUploaded>2008-04-01T23:00:00.000+00:00</dateUploaded>
  <dateSysMetadataModified>2012-06-26T03:51:25.058+00:00</dateSysMetadataModified>
  <originMemberNode>urn:node:TEST</originMemberNode>
  <authoritativeMemberNode>urn:node:TEST</authoritativeMemberNode>
</d1:systemMetadata>
 */
    public int getSerialVersion() {
        return serialVersion;
    }
    public void setSerialVersion(int serialVersion) {
        this.serialVersion = serialVersion;
    }
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
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public DataOneChecksum getChecksum() {
        return checksum;
    }
    public void setChecksum(DataOneChecksum checksum) {
        this.checksum = checksum;
    }
    public String getSubmitter() {
        return submitter;
    }
    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }
    public String getRightsHolder() {
        return rightsHolder;
    }
    public void setRightsHolder(String rightsHolder) {
        this.rightsHolder = rightsHolder;
    }
    public DataOneAccessPolicy getAccessPolicy() {
        return accessPolicy;
    }
    public void setAccessPolicy(DataOneAccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
    }
    public DataOneReplicationPolicy getReplicationPolicy() {
        return replicationPolicy;
    }
    public void setReplicationPolicy(DataOneReplicationPolicy replicationPolicy) {
        this.replicationPolicy = replicationPolicy;
    }
    public String getObsoletes() {
        return obsoletes;
    }
    public void setObsoletes(String obsoletes) {
        this.obsoletes = obsoletes;
    }
    public String getObsoletedBy() {
        return obsoletedBy;
    }
    public void setObsoletedBy(String obsoletedBy) {
        this.obsoletedBy = obsoletedBy;
    }
    public boolean isArchived() {
        return archived;
    }
    public void setArchived(boolean archived) {
        this.archived = archived;
    }
    public Date getDateUploaded() {
        return dateUploaded;
    }
    public void setDateUploaded(Date dateUploaded) {
        this.dateUploaded = dateUploaded;
    }
    public Date getDateSysMetadataModified() {
        return dateSysMetadataModified;
    }
    public void setDateSysMetadataModified(Date dateSysMetadataModified) {
        this.dateSysMetadataModified = dateSysMetadataModified;
    }
    public String getOriginMemberNode() {
        return originMemberNode;
    }
    public void setOriginMemberNode(String originMemberNode) {
        this.originMemberNode = originMemberNode;
    }
    public String getAuthoritativeMemberNode() {
        return authoritativeMemberNode;
    }
    public void setAuthoritativeMemberNode(String authoritativeMemberNode) {
        this.authoritativeMemberNode = authoritativeMemberNode;
    }

}
