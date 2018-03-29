/**
 * 
 */
package org.tdar.oai.bean;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.tdar.oai.bean.generated.oai._2_0.OAIPMHerrorcodeType;
import org.tdar.oai.exception.OAIException;
import org.tdar.utils.MessageHelper;

/**
 * @author ctuohy
 *         Wrapper for an OAI ResumptionToken
 * 
 */
public class OAIResumptionToken {
    private static final String OAI_REGEX_CONSTANT = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}";

    private Date fromDate;
    private Date untilDate;
    private String metadataPrefix;
    private String token = "";
    private Token cursor = new Token();
    private Long set;

    /**
     * Create an empty ResumptionToken (for ending a list)
     */
    public OAIResumptionToken() {

    }

    /**
     * Create a ResumptionToken from a token received from the client
     * 
     * @throws OAIException
     */
    public OAIResumptionToken(String token) throws OAIException {
        setToken(token);
    }

    public OAIResumptionToken(Token cursor2, Date effectiveFrom, Date effectiveUntil, OAIMetadataFormat metadataFormat, Long collectionId) throws OAIException {
        try {
        setCursor(cursor2);
        setFromDate(effectiveFrom);
        setUntilDate(effectiveUntil);
        if (metadataFormat != null) {
            setMetadataPrefix(metadataFormat.getPrefix());
        }
        setSet(collectionId);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    /**
     * @return the cursor
     */
    public Token getCursor() {
        return cursor;
    }

    /**
     * @param cursor
     *            the cursor to set
     */
    public void setCursor(Token cursor) {
        this.cursor = cursor;
        invalidateToken();
    }

    /**
     * @return the fromDate
     */
    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date date) {
        fromDate = date;
    }

    public void setUntilDate(Date date) {
        untilDate = date;
    }

    /**
     * @param fromDate
     *            the fromDate to set
     * @throws OAIException
     */
    public void setFromDate(String fromDate) throws OAIException {
        this.fromDate = new DateTime(fromDate).toDate();
        invalidateToken();
        if (!fromDate.matches(OAI_REGEX_CONSTANT)) {
            throw new OAIException(MessageHelper.getMessage("oaiResumptionToken.invalid_from"), OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN);
        }
    }

    /**
     * @return the untilDate
     */
    public Date getUntilDate() {
        return untilDate;
    }

    /**
     * @param untilDate
     *            the untilDate to set
     * @throws OAIException
     */
    public void setUntilDate(String untilDate) throws OAIException {
        this.untilDate = new DateTime(untilDate).toDate();
        invalidateToken();
        if (!untilDate.matches(OAI_REGEX_CONSTANT)) {
            throw new OAIException(MessageHelper.getMessage("oaiResumptionToken.invalid_until"), OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN);
        }
    }

    /**
     * @return the metadataPrefix
     */
    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    /**
     * @param metadataPrefix
     *            the metadataPrefix to set
     * @throws OAIException
     */
    public void setMetadataPrefix(String metadataPrefix) throws OAIException {
        this.metadataPrefix = metadataPrefix;
        invalidateToken();
        // validate the prefix
        if (metadataPrefix != null) {
            OAIMetadataFormat.fromString(metadataPrefix);
        }
    }

    /**
     * @return the token
     */
    public String getToken() {
        if (token == null) {
            // recompute the token
            StringBuffer tokenBuffer = new StringBuffer(cursor.toString());
            tokenBuffer.append(",");
            tokenBuffer.append(new DateTime(fromDate).toString("yyyy-MM-dd"));
            tokenBuffer.append(",");
            tokenBuffer.append(new DateTime(untilDate).toString("yyyy-MM-dd"));
            tokenBuffer.append(",");
            if (metadataPrefix != null) {
                tokenBuffer.append(metadataPrefix);
            }
            token = tokenBuffer.toString();
        }
        return token;
    }

    /**
     * @param token
     *            the token to set
     * @throws OAIException
     *             if the token is malformed
     */
    private void setToken(String token) throws OAIException {
        this.token = token;
        try {
            // The format of the resumptionToken is: "recordIndex,[from],[until],[metadataFormat]"
            // e.g. it might contain "200,20101201,20110101,tdar" or it might contain "100,1900,3000,oai_dc"
            String[] tokenPart = token.split(",", 4);
            // First parameter is the index of the next record to return
            cursor = new Token(tokenPart[0]);
            // Second and third parameters are optional from and until dates
            setFromDate(tokenPart[1]);
            setUntilDate(tokenPart[2]);
            setMetadataPrefix(tokenPart[3]);
        } catch (Exception e) {
            throw new OAIException(MessageHelper.getMessage("oaiResumptionToken.invalid"), e, OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN);
        }
    }

    /**
     * Token must be recalculated on next request
     */
    private void invalidateToken() {
        token = null;
    }

    public Long getSet() {
        return set;
    }

    public void setSet(Long id) {
        this.set = id;
    }

    public Date getEffectiveFrom(Date from) {
        if (fromDate != null) {
            return fromDate;
        }

        return from;
    }

    public Date getEffectiveUntil(Date until) {
        if (untilDate != null) {
            return untilDate;
        }
        return until;
    }

    public OAIMetadataFormat getEffectiveMetadataPrefix(OAIMetadataFormat metadataPrefix2) throws OAIException {
        if (StringUtils.isNotBlank(metadataPrefix)) {
            return OAIMetadataFormat.fromString(metadataPrefix);
        }
        return metadataPrefix2;
    }
}
