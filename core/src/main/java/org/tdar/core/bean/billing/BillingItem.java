package org.tdar.core.bean.billing;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.exception.TdarValidationException;

/**
 * an Activity + quantity for a financial transaction. Multiple activities may be associated with a single financial transaction.
 * 
 */
@Entity
@Table(name = "pos_item")
public class BillingItem extends AbstractPersistable implements Validatable {

    private static final long serialVersionUID = -2775737509085985555L;
    @SuppressWarnings("unused")
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "activity_id")
    @NotNull
    private BillingActivity activity;

    private Integer quantity = 0;

    public BillingItem() {
    }

    public BillingItem(BillingActivity activity, int quantity) {
        this.activity = activity;
        this.quantity = quantity;
    }

    /**
     * Instantiate a billing item of the specified activity with the implicit quantity of
     * one.
     * @param activity
     */
    public BillingItem(BillingActivity activity) {
        this(activity, 1);
    }

    public BillingActivity getActivity() {
        return activity;
    }

    public void setActivity(BillingActivity activity) {
        this.activity = activity;
    }

    public Integer getQuantity() {
        if ((quantity == null) || (quantity < 1)) {
            return 0;
        }
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    @XmlTransient
    public boolean isValidForController() {
        if (getActivity() == null) {
            throw new TdarValidationException("billingItem.specify_activity");
        }
        if (getQuantity() < 1) {
            throw new TdarValidationException("billingItem.non_zero_value");
        }
        return true;
    }

    @Override
    @XmlTransient
    public boolean isValid() {
        return isValidForController();
    }

    public Float getSubtotal() {
        return activity.getPrice() * getQuantity().floatValue();
    }

    @Override
    public String toString() {
        return String.format("%s %s ($%s)", getQuantity(), getActivity(), getSubtotal());
    }
}
