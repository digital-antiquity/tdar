package org.tdar.struts.data.oai;

import java.util.Date;

import freemarker.ext.dom.NodeModel;

public class OAIRecordProxy {
    // wrapper object holding values for an OAI Record: id, datestamp, and an XML serialization in a FreeMarker NodeModel
    private String identifier;
    private Date datestamp;
    private NodeModel metadata;

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the datestamp
     */
    public Date getDatestamp() {
        return datestamp;
    }

    /**
     * @return the metadata
     */
    public NodeModel getMetadata() {
        return metadata;
    }

    public void setMetadata(NodeModel metadata) {
        this.metadata = metadata;
    }

    public OAIRecordProxy(String repositoryNamespaceIdentifier, OAIRecordType recordType, long numericIdentifier, Date datestamp) {
        this.identifier = "oai:" + repositoryNamespaceIdentifier + ":" + recordType.getName() + ":" + String.valueOf(numericIdentifier);
        this.datestamp = datestamp;
    }

}
