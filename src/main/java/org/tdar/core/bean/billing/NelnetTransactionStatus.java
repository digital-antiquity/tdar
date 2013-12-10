package org.tdar.core.bean.billing;

import java.util.Arrays;
import java.util.List;

import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.utils.MessageHelper;
public enum NelnetTransactionStatus {
    CC_PAYMENT_ACCEPTED(1, MessageHelper.getMessage("nelnetTransactionStatus.accepted"), TransactionStatus.TRANSACTION_SUCCESSFUL),
    CC_PAYMENT_REJECTED(2, MessageHelper.getMessage("nelnetTransactionStatus.cc_rejected"), TransactionStatus.TRANSACTION_FAILED),
    CC_PAYMENT_ERROR(3, MessageHelper.getMessage("nelnetTransactionStatus.cc_error"), TransactionStatus.TRANSACTION_FAILED),
    CC_PAYMENT_UNKNOWN(4,MessageHelper.getMessage("nelnetTransactionStatus.cc_unknown") , TransactionStatus.TRANSACTION_FAILED),
    CHECK_PAYMENT_SUCCESS(5, MessageHelper.getMessage("nelnetTransactionStatus.check_success"), TransactionStatus.TRANSACTION_SUCCESSFUL),
    CHECK_PAYMENT_POSTED(6, MessageHelper.getMessage("nelnetTransactionStatus.check_posted"), TransactionStatus.TRANSACTION_SUCCESSFUL),
    CHECK_PAYMENT_RETURNED(7, MessageHelper.getMessage("nelnetTransactionStatus.check_returned"), TransactionStatus.TRANSACTION_FAILED),
    CHECK_PAYMENT_NOC(8, MessageHelper.getMessage("nelnetTransactionStatus.check_noc"), TransactionStatus.TRANSACTION_SUCCESSFUL);

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
