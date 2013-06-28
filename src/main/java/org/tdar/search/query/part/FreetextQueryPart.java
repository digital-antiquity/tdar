package org.tdar.search.query.part;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class FreetextQueryPart extends FieldQueryPart<String> {

    @Override
    public String getFieldName() {
        return "";
    }

    @Override
    public String generateQueryString() {
        String txt = super.generateQueryString();
        /* THESE LINES ARE TO MAINTAIN COMPATIBILITY WITH THE TAG GATEWAY SEE TagGatewayITCase.getTopRecords() */
        if (txt != null && (txt.trim().equals("*:*") || txt.trim().equals("(*:*)"))) {
            return "";
        }
        logger.info(txt);
        return txt;
    };
    
    @Override
    public String getDescription() {
        return "Every Single Field: " + StringUtils.join(getFieldValues(), ", ");
    }

    @Override
    public String getDescriptionHtml() {
        return StringEscapeUtils.escapeHtml4(getDescription());
    }

}
