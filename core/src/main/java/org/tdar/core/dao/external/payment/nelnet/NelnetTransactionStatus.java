package org.tdar.core.dao.external.payment.nelnet;

import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.billing.TransactionStatus;

/**
 * Enum to track NelNet Transaction Status
 * 
 * @author abrin
 * 
 */
public enum NelnetTransactionStatus {
    CC_PAYMENT_ACCEPTED(1, "Accepted credit card payment/refund (successful)", TransactionStatus.TRANSACTION_SUCCESSFUL),
    CC_PAYMENT_REJECTED(2,
            "Rejected credit card payment/refund (declined)", TransactionStatus.TRANSACTION_FAILED),
    CC_PAYMENT_ERROR(3,
            "Error credit card payment/refund (error)", TransactionStatus.TRANSACTION_FAILED),
    CC_PAYMENT_UNKNOWN(4,
            "Unknown credit card payment/refund (unknown)", TransactionStatus.TRANSACTION_FAILED),
    CHECK_PAYMENT_SUCCESS(5,
            "Accepted eCheck payment (successful)", TransactionStatus.TRANSACTION_SUCCESSFUL),
    CHECK_PAYMENT_POSTED(6,
            "Posted eCheck payment (successful)", TransactionStatus.TRANSACTION_SUCCESSFUL),
    CHECK_PAYMENT_RETURNED(7,
            "Returned eCheck payment (failed)", TransactionStatus.TRANSACTION_FAILED),
    CHECK_PAYMENT_NOC(8,
            "NOC eCheck payment (successful)", TransactionStatus.TRANSACTION_SUCCESSFUL);

    private int ordinalValue;
    private String label;
    private TransactionStatus status;

    private NelnetTransactionStatus(int ord, String label, TransactionStatus status) {
        this.setLabel(label);
        this.setOrdinalValue(ord);
        this.setStatus(status);
    }

    public String getLabel() {
        return label;
    }

    private void setLabel(String label) {
        this.label = label;
    }

    public int getOrdinalValue() {
        return ordinalValue;
    }

    private void setOrdinalValue(int ordinalValue) {
        this.ordinalValue = ordinalValue;
    }

    public static List<NelnetTransactionStatus> getSuccessfulStatuses() {
        return Arrays.asList(CC_PAYMENT_ACCEPTED, CHECK_PAYMENT_NOC, CHECK_PAYMENT_POSTED, CHECK_PAYMENT_SUCCESS);
    }

    public static NelnetTransactionStatus fromOrdinal(int ord) {
        for (NelnetTransactionStatus type : values()) {
            if (type.getOrdinalValue() == ord) {
                return type;
            }
        }
        return null;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    private void setStatus(TransactionStatus status) {
        this.status = status;
    }

}
