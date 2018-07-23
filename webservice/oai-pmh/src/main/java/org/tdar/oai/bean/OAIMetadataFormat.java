/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.oai.bean;

import org.apache.commons.lang3.StringUtils;
import org.tdar.oai.bean.generated.oai._2_0.OAIPMHerrorcodeType;
import org.tdar.oai.exception.OAIException;

/**
 * @author Adam Brin
 * 
 */
public enum OAIMetadataFormat {
    // http://www.openarchives.org/OAI/openarchivesprotocol.html#MetadataNamespaces
    DC("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/", "http://www.openarchives.org/OAI/2.0/oai_dc.xsd"),
    EXTENDED_DC("qualified_dc", "http://ns.dataone.org/metadata/schema/onedcx/v1.0", "http://ns.dataone.org/metadata/schema/onedcx/v1.0/onedcx_v1.0.xsd"),
    MODS("mods", "http://www.loc.gov/mods/v3", "http://www.loc.gov/standards/mods/v3/mods-3-1.xsd"),
    TDAR("tdar", "http://www.tdar.org/namespace", "/schema/current");

    private String prefix;
    private String namespace;
    private String schemaLocation;

    private OAIMetadataFormat(String metadataPrefix, String namespace, String schemaLocation) {
        this.setPrefix(metadataPrefix);
        this.setNamespace(namespace);
        this.setSchemaLocation(schemaLocation);
    }

    public static OAIMetadataFormat fromString(String val) throws OAIException {
        for (OAIMetadataFormat prefix_ : OAIMetadataFormat.values()) {
            if (StringUtils.equalsIgnoreCase(prefix_.getPrefix(), val) || StringUtils.equalsIgnoreCase(prefix_.name(), val)) {
                return prefix_;
            }
        }
        throw new OAIException("Unknown or missing metadata format", OAIPMHerrorcodeType.BAD_ARGUMENT);
    }

    /**
     * @return the XML namespace URI
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace
     *            the XML namespace URI
     */
    private void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @return the XML schema location
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * @param schemaLocation
     *            the XML schema location
     */
    private void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix
     *            the prefix to set
     */
    private void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
