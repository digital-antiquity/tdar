/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.data.oai;

import org.tdar.core.exception.OAIException;
import org.tdar.utils.MessageHelper;

/**
 * @author Conal Tuohy
 *         A list of all the URL parameters acceptable to OAIController - other parameters will be rejected
 */
public enum OAIParameter {

    VERB("verb"),
    IDENTIFIER("identifier"),
    METADATA_PREFIX("metadataPrefix"),
    FROM("from"),
    UNTIL("until"),
    SET("set"),
    RESUMPTION_TOKEN("resumptionToken");

    private String name;

    private OAIParameter(String name) {
        this.name = name;
    }

    public static OAIParameter fromString(String val) throws OAIException {
        for (OAIParameter parameter : OAIParameter.values()) {
            if (parameter.getName().equals(val)) {
                return parameter;
            }
        }
        throw new OAIException(MessageHelper.getMessage("oaiParameter.unknown_parameter", val), OaiErrorCode.BAD_ARGUMENT);
    }

    /**
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

}