package org.tdar.core.bean.billing;

import java.util.Date;

import org.tdar.core.bean.Persistable.Base;

/**
 * $Id$
 * 
 * Represents a credit card swipe to purchase space and number of resources for a given Account.
 * 
 * @author TDAR
 * @version $Rev$
 */
public class Invoice extends Base {

    private static final long serialVersionUID = -3613460318580954253L;
    
    private Date dateCreated = new Date();
    // the confirmation id for this invoice 
    private String transactionId;
    private Integer numberOfRecords;
    private Long numberOfBytes;
    
    /**
     * @return the dateCreated
     */
    public Date getDateCreated() {
        return dateCreated;
    }
    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    /**
     * @return the transactionId
     */
    public String getTransactionId() {
        return transactionId;
    }
    /**
     * @param transactionId the transactionId to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    /**
     * @return the numberOfRecords
     */
    public Integer getNumberOfRecords() {
        return numberOfRecords;
    }
    /**
     * @param numberOfRecords the numberOfRecords to set
     */
    public void setNumberOfRecords(Integer numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }
    /**
     * @return the numberOfBytes
     */
    public Long getNumberOfBytes() {
        return numberOfBytes;
    }
    /**
     * @param numberOfBytes the numberOfBytes to set
     */
    public void setNumberOfBytes(Long numberOfBytes) {
        this.numberOfBytes = numberOfBytes;
    }
    

}
