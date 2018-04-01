/**
 * 
 */
package org.tdar.oai.bean;

import java.util.Arrays;

import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.oai.bean.generated.oai._2_0.OAIPMHerrorcodeType;
import org.tdar.oai.exception.OAIException;
import org.tdar.oai.service.OaiPmhConfiguration;
import org.tdar.utils.MessageHelper;

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

    public Class<?> getActualClass() {
        switch (this) {
            case INSTITUTION:
                return Institution.class;
            case PERSON:
                return Person.class;
            case RESOURCE:
                return Resource.class;
        }
        return null;
    }

    public OAIMetadataFormat[] getMetadataFormats() {
        return metadataFormats;
    }

    public void checkCanDisseminateFormat(OAIMetadataFormat format) throws OAIException {
        if ((format != OAIMetadataFormat.TDAR) || OaiPmhConfiguration.getInstance().enableTdarFormatInOAI()) {
            for (OAIMetadataFormat validFormat : metadataFormats) {
                if (validFormat.equals(format)) {
                    return;
                }
            }
        }

        throw new OAIException("Metadata format '" + format.getPrefix() + "' cannot be disseminated for records of type '" + name + "'",
                OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT);
    }

    public static OAIRecordType fromString(String val) throws OAIException {
        for (OAIRecordType type : OAIRecordType.values()) {
            if (type.getName().equals(val)) {
                return type;
            }
        }
        throw new OAIException(MessageHelper.getMessage("oaiRecordType.metadata_format_unknown", Arrays.asList(val)), OAIPMHerrorcodeType.ID_DOES_NOT_EXIST);
    }

}
