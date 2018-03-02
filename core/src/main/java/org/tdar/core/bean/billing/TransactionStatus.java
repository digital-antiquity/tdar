package org.tdar.core.bean.billing;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

public enum TransactionStatus implements HasLabel, Localizable {
    PREPARED("Prepared"), PENDING_TRANSACTION("Pending Transaction"), TRANSACTION_SUCCESSFUL("Transaction Successful"), TRANSACTION_FAILED(
            "Transaction Failed"), TRANSACTION_CANCELLED("Transaction Cancelled");

    private String label;

    private TransactionStatus(String label) {
        this.label = label;
    }

    public boolean isComplete() {
        switch (this) {
            case PENDING_TRANSACTION:
            case PREPARED:
                return false;
            default:
                return true;
        }
    }

    public boolean isInvalid() {
        switch (this) {
            case TRANSACTION_CANCELLED:
            case TRANSACTION_FAILED:
                return true;
            default:
                return false;
        }
    }

    public boolean isModifiable() {
        switch (this) {
            case TRANSACTION_FAILED:
            case TRANSACTION_CANCELLED:
            case TRANSACTION_SUCCESSFUL:
                return false;
            default:
                return true;
        }
    }

    public boolean isSuccessful() {
        return this == TRANSACTION_SUCCESSFUL;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

}