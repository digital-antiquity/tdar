package org.tdar.core.bean.billing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

//import net.sf.json.JSONObject;
//import net.sf.json.JsonConfig;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;

/**
 * A JSON Object that represents the result of a financial transaction. Could be successful or failed.
 * 
 * @author abrin
 * 
 */
@Entity
@Table(name = "pos_transaction_log")
public class BillingTransactionLog extends AbstractPersistable {

    private static final long serialVersionUID = 2104177203134056911L;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "response")
    private String responseInJson;

    @NotNull
    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
    // the confirmation id for this invoice

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String transactionId;

    public BillingTransactionLog() {
    }

    public BillingTransactionLog(String jsonResponse, String transactionId) {
        setResponseInJson(jsonResponse);
        setDateCreated(new Date());
        setTransactionId(transactionId);

    }

    public String getResponseInJson() {
        return responseInJson;
    }

    public void setResponseInJson(String responseInJson) {
        this.responseInJson = responseInJson;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return String.format("TransactionLog (%s) - %s", getId(), getTransactionId());
    }

}
