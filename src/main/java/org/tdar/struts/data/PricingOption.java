package org.tdar.struts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.billing.BillingItem;

public class PricingOption implements Serializable {

    
    public enum PricingType  {
        SIZED_BY_MB,
        SIZED_BY_FILE_ONLY,
        SIZED_BY_FILE_ABOVE_TIER
    }
    
    private static final long serialVersionUID = -3297968564600082652L;

    private List<BillingItem> items = new ArrayList<BillingItem>();
    private Float subtotal = 0f;
    private PricingType type;

    public PricingOption(PricingType type, List<BillingItem> cheapestActivityByFiles) {
        getItems().addAll(cheapestActivityByFiles);
        this.setType(type);
    }

    public PricingOption(PricingType type) {
        this.setType(type);
    }

    public PricingOption(PricingType type, BillingItem activity) {
        getItems().add(activity);
        this.setType(type);
    }

    public List<BillingItem> getItems() {
        return items;
    }

    public Float getSubtotal() {
        subtotal = 0f;
        for (BillingItem item : items) {
            if (item != null) {
                subtotal += item.getSubtotal();
            }
        }
        return subtotal;
    }

    public PricingType getType() {
        return type;
    }

    public void setType(PricingType type) {
        this.type = type;
    }
}
