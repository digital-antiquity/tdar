package org.tdar.search.query;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class FreetextQueryPart extends QueryPart.Base {

    private String fieldValue;

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String queryString) {
        this.fieldValue = queryString;
    }

    @Override
    public String generateQueryString() {
        return fieldValue;
    }

}
