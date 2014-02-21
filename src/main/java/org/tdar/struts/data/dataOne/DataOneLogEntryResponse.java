package org.tdar.struts.data.dataOne;

import java.io.Serializable;
import java.util.List;

public class DataOneLogEntryResponse implements Serializable {


    /*
     * <?xml version="1.0" encoding="UTF-8"?>
<d1:log xmlns:d1="http://ns.dataone.org/service/types/v1" count="3" start="0" total="1273">
  <logEntry>
    <entryId>1</entryId>
    <identifier>MNodeTierTests.201260152556757.</identifier>
    <ipAddress>129.24.0.17</ipAddress>
    <userAgent>null</userAgent>
    <subject>CN=testSubmitter,DC=dataone,DC=org</subject>
    <event>create</event>
    <dateLogged>2012-02-29T23:25:58.104+00:00</dateLogged>
    <nodeIdentifier>urn:node:DEMO2</nodeIdentifier>
  </logEntry>
  <logEntry>
    <entryId>2</entryId>
    <identifier>TierTesting:testObject:RightsHolder_Person.4</identifier>
    <ipAddress>129.24.0.17</ipAddress>
    <userAgent>null</userAgent>
    <subject>CN=testSubmitter,DC=dataone,DC=org</subject>
    <event>create</event>
    <dateLogged>2012-02-29T23:26:38.828+00:00</dateLogged>
    <nodeIdentifier>urn:node:DEMO2</nodeIdentifier>
  </logEntry>
  <logEntry>
    <entryId>3</entryId>
    <identifier>TierTesting:testObject:RightsHolder_Group.4</identifier>
    <ipAddress>129.24.0.17</ipAddress>
    <userAgent>null</userAgent>
    <subject>CN=testSubmitter,DC=dataone,DC=org</subject>
    <event>create</event>
    <dateLogged>2012-02-29T23:27:40.255+00:00</dateLogged>
    <nodeIdentifier>urn:node:DEMO2</nodeIdentifier>
  </logEntry>
</d1:log>
     */

    private static final long serialVersionUID = -7299439567248788358L;
    private List<DataOneLogEntry> logEntries;
    public List<DataOneLogEntry> getLogEntries() {
        return logEntries;
    }
    public void setLogEntries(List<DataOneLogEntry> logEntries) {
        this.logEntries = logEntries;
    }
}
