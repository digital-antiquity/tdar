package org.tdar.struts.data.dataOne;

import java.io.Serializable;
import java.util.List;

public class DataOneCapabilitiesResponse implements Serializable {

    /*
     * <?xml version="1.0" encoding="UTF-8"?>
<d1:node xmlns:d1="http://ns.dataone.org/service/types/v1" replicate="true" synchronize="true" type="mn" state="up">
  <identifier>urn:node:DEMO2</identifier>
  <name>DEMO2 Metacat Node</name>
  <description>A DataONE member node implemented in Metacat.</description>
  <baseURL>https://demo2.test.dataone.org:443/knb/d1/mn</baseURL>
  <services>
    <service name="MNRead" version="v1" available="true"/>
    <service name="MNCore" version="v1" available="true"/>
    <service name="MNAuthorization" version="v1" available="true"/>
    <service name="MNStorage" version="v1" available="true"/>
    <service name="MNReplication" version="v1" available="true"/>
  </services>
  <synchronization>
    <schedule hour="*" mday="*" min="0/3" mon="*" sec="10" wday="?" year="*"/>
    <lastHarvested>2012-03-06T14:57:39.851+00:00</lastHarvested>
    <lastCompleteHarvest>2012-03-06T14:57:39.851+00:00</lastCompleteHarvest>
  </synchronization>
  <ping success="true"/>
  <subject>CN=urn:node:DEMO2, DC=dataone, DC=org</subject>
  <contactSubject>CN=METACAT1, DC=dataone, DC=org</contactSubject>
</d1:node>
     */
    private static final long serialVersionUID = -6750088410853580843L;

    private String identifier;
    private String name;
    private String description;
    private String baseUrl;
    private boolean pingSuccess = true;
    private boolean replicate = false;
    private boolean synchronize = true;
    private String state = "up";
    private String type = "mn";
    private String subject;
    private String contactSubject;
    
    private List<DataOneCapabilitiesServices> services;
    private List<DataOneCapabilitiesSynchronization> schedule;
    
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public boolean isPingSuccess() {
        return pingSuccess;
    }
    public void setPingSuccess(boolean pingSuccess) {
        this.pingSuccess = pingSuccess;
    }
    public boolean isReplicate() {
        return replicate;
    }
    public void setReplicate(boolean replicate) {
        this.replicate = replicate;
    }
    public boolean isSynchronize() {
        return synchronize;
    }
    public void setSynchronize(boolean synchronize) {
        this.synchronize = synchronize;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getContactSubject() {
        return contactSubject;
    }
    public void setContactSubject(String contactSubject) {
        this.contactSubject = contactSubject;
    }
    public List<DataOneCapabilitiesServices> getServices() {
        return services;
    }
    public void setServices(List<DataOneCapabilitiesServices> services) {
        this.services = services;
    }
    public List<DataOneCapabilitiesSynchronization> getSchedule() {
        return schedule;
    }
    public void setSchedule(List<DataOneCapabilitiesSynchronization> schedule) {
        this.schedule = schedule;
    }

}
