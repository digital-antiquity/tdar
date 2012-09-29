/**
 * 
 */
package org.tdar.struts.data.oai;

import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.OAIException;

/**
 * @author ctuohy
 * 
 *         The types of TDAR record which can be disseminated by OAI-PMH, each with its own set of valid metadata formats
 */
public enum OAIRecordType {
    RESOURCE("Resource", OAIMetadataFormat.values()),
    PERSON("Person", OAIMetadataFormat.DC, OAIMetadataFormat.TDAR),
    INSTITUTION("Institution", OAIMetadataFormat.DC, OAIMetadataFormat.TDAR);

    private String name;
    private OAIMetadataFormat[] metadataFormats;

    private OAIRecordType(String name, OAIMetadataFormat... metadataFormats) {
        this.name = name;
        this.metadataFormats = metadataFormats;
    }

    public String getName() {
        return name;
    }

    public OAIMetadataFormat[] getMetadataFormats() {
        return metadataFormats;
    }

    public void checkCanDisseminateFormat(OAIMetadataFormat format) throws OAIException {
        if (format != OAIMetadataFormat.TDAR || TdarConfiguration.getInstance().enableTdarFormatInOAI()) {
            for (OAIMetadataFormat validFormat : metadataFormats) {
                if (validFormat.equals(format)) {
                    return;
                }
            }
        }

        throw new OAIException("Metadata format '" + format.getPrefix() + "' cannot be disseminated for records of type '" + name + "'",
                OaiErrorCode.CANNOT_DISSEMINATE_FORMAT);
    }

    public static OAIRecordType fromString(String val) throws OAIException {
        for (OAIRecordType type : OAIRecordType.values()) {
            if (type.getName().equals(val)) {
                return type;
            }
        }
        throw new OAIException("Unknown record type '" + val + "'", OaiErrorCode.ID_DOES_NOT_EXIST);
    }

}
