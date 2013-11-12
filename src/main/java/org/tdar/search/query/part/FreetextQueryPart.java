package org.tdar.search.query.part;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.tdar.utils.MessageHelper;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class FreetextQueryPart extends FieldQueryPart<String> {

    private static final String AN_OBJECT = "*:*";

    @Override
    public String getFieldName() {
        return "";
    }

    @Override
    public String generateQueryString() {
        String txt = super.generateQueryString();
        /* THESE LINES ARE TO MAINTAIN COMPATIBILITY WITH THE TAG GATEWAY SEE TagGatewayITCase.getTopRecords() */
        if (txt != null && (txt.trim().equals(AN_OBJECT) || txt.trim().equals("(*:*)"))) {
            return "";
        }
        logger.info(txt);
        return txt;
    };

    @Override
    public String getDescription() {
        return MessageHelper.getMessage("freetextQueryPart.every_field",  StringUtils.join(getFieldValues(), ", "));
    }

    @Override
    public String getDescriptionHtml() {
        return StringEscapeUtils.escapeHtml4(getDescription());
    }

}
