/**
 * 
 */
package org.tdar.struts.data.oai;

import org.tdar.core.exception.OAIException;

/**
 * @author ctuohy
 * Wrapper for an OAI ResumptionToken
 *  
 */
public class OAIResumptionToken {
    private int cursor = 0;
    private String fromDate;
    private String untilDate;
    private String metadataPrefix;
    private String token = "";
    
    /**
     * Create an empty ResumptionToken (for ending a list)
     */
    public OAIResumptionToken() {
        
    }
    
    /**
     * Create a ResumptionToken from a token received from the client
     * @throws OAIException 
     */
    public OAIResumptionToken(String token) throws OAIException {
        setToken(token);
    }
    
    /**
     * @return the cursor
     */
    public int getCursor() {
        return cursor;
    }
    /**
     * @param cursor the cursor to set
     */
    public void setCursor(int cursor) {
        this.cursor = cursor;
        invalidateToken();
    }
    /**
     * @return the fromDate
     */
    public String getFromDate() {
        return fromDate;
    }
    /**
     * @param fromDate the fromDate to set
     * @throws OAIException 
     */
    public void setFromDate(String fromDate) throws OAIException {
        this.fromDate = fromDate;
        invalidateToken();
        if (! fromDate.matches("[0-9]+")) {
            throw new OAIException("Invalid from parameter in resumptionToken", OaiErrorCode.BAD_RESUMPTION_TOKEN);
        }
    }
    /**
     * @return the untilDate
     */
    public String getUntilDate() {
        return untilDate;
    }
    /**
     * @param untilDate the untilDate to set
     * @throws OAIException 
     */
    public void setUntilDate(String untilDate) throws OAIException {
        this.untilDate = untilDate;
        invalidateToken();
        if (! untilDate.matches("[0-9]+")) {
            throw new OAIException("Invalid until parameter in resumptionToken", OaiErrorCode.BAD_RESUMPTION_TOKEN);
        }
    }
    /**
     * @return the metadataPrefix
     */
    public String getMetadataPrefix() {
        return metadataPrefix;
    }
    /**
     * @param metadataPrefix the metadataPrefix to set
     * @throws OAIException 
     */
    public void setMetadataPrefix(String metadataPrefix) throws OAIException {
        this.metadataPrefix = metadataPrefix;
        invalidateToken();
        // validate the prefix
        OAIMetadataFormat.fromString(metadataPrefix);
    }
    /**
     * @return the token
     */
    public String getToken() {
        if (token == null) {
            // recompute the token
            StringBuffer tokenBuffer = new StringBuffer(String.valueOf(cursor));
            tokenBuffer.append(",");
            tokenBuffer.append(fromDate);
            tokenBuffer.append(",");
            tokenBuffer.append(untilDate);
            tokenBuffer.append(",");
            if (metadataPrefix != null) {
                tokenBuffer.append(metadataPrefix);
            }
            token = tokenBuffer.toString();
        }
        return token;
    }
    /**
     * @param token the token to set
     * @throws OAIException if the token is malformed 
     */
    private void setToken(String token) throws OAIException {
        this.token = token;
        try {
            // The format of the resumptionToken is: "recordIndex,[from],[until],[metadataFormat]"
            // e.g. it might contain "200,20101201,20110101,tdar" or it might contain "100,1900,3000,oai_dc"
            String[] tokenPart = token.split(",", 4);
            // First parameter is the index of the next record to return
            cursor = Integer.valueOf(tokenPart[0]);
            // Second and third parameters are optional from and until dates
            setFromDate(tokenPart[1]);
            setUntilDate(tokenPart[2]);
            setMetadataPrefix(tokenPart[3]);
        } catch (Exception e) {
            throw new OAIException("Invalid resumptionToken", e, OaiErrorCode.BAD_RESUMPTION_TOKEN);
        }
    }
    
    /** 
     * Token must be recalculated on next request
     */
    private void invalidateToken() {
        token = null;
    }
}
