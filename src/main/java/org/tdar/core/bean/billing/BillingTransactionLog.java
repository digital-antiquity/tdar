package org.tdar.core.bean.billing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.dao.external.payment.nelnet.TransactionResponse;

@Entity
@Table(name = "pos_transaction_log")
public class BillingTransactionLog extends Base {

    private static final long serialVersionUID = 2104177203134056911L;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name = "response")
    private String responseInJson;

    @NotNull
    @Column(name = "date_created")
    private Date dateCreated;
    // the confirmation id for this invoice

    @Length(max = 255)
    private String transactionId;

    public BillingTransactionLog() {
    }

    public BillingTransactionLog(TransactionResponse response) {
        JsonConfig config = new JsonConfig();
        JSONObject jsonObject = JSONObject.fromObject(response.getValues(), config);
        setResponseInJson(jsonObject.toString());
        setDateCreated(new Date());
        setTransactionId(response.getTransactionId());

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

    // public Invoice getInvoice() {
    // return invoice;
    // }
    //
    // public void setInvoice(Invoice invoice) {
    // this.invoice = invoice;
    // }

}
