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
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "activity_id")
    @NotNull
    private BillingActivity activity;

    private Integer quantity = 0;

    public BillingActivity getActivity() {
        return activity;
    }

    public void setActivity(BillingActivity activity) {
        this.activity = activity;
    }

    public Integer getQuantity() {
        if (quantity == null || quantity < 1) {
            return 0;
        }
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
