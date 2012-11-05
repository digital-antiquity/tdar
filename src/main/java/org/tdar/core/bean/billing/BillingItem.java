package org.tdar.core.bean.billing;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.Validatable;
import org.tdar.core.configuration.JSONTransient;

/*
 * an Activity + quanitty
 */
@Entity
@Table(name = "pos_item")
public class BillingItem extends Base implements Validatable {

    private static final long serialVersionUID = -2775737509085985555L;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "activity_id")
    @NotNull
    private BillingActivity activity;

    private Integer quantity;

    public BillingActivity getActivity() {
        return activity;
    }

    public void setActivity(BillingActivity activity) {
        this.activity = activity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    @JSONTransient
    @XmlTransient
    public boolean isValidForController() {
        return (getQuantity() > 0);
    }

    @Override
    @JSONTransient
    @XmlTransient
    public boolean isValid() {
        return isValidForController();
    }

    public Float getSubtotal() {
        return activity.getPrice() * getQuantity().floatValue();
    }
}
