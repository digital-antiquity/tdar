package org.tdar.struts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.billing.BillingItem;

public class PricingOption implements Serializable {

    private static final long serialVersionUID = -3297968564600082652L;

    private List<BillingItem> items = new ArrayList<BillingItem>();
    private Float subtotal = 0f;

    public PricingOption(List<BillingItem> cheapestActivityByFiles) {
        getItems().addAll(cheapestActivityByFiles);
    }

    public PricingOption(BillingItem activity) {
        getItems().add(activity);
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
}
