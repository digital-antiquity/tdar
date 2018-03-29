package org.tdar.core.service.billing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.utils.MessageHelper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
public class PricingOption implements Serializable {

    public enum PricingType implements HasLabel, Localizable {
        SIZED_BY_MB("Priced by MB"), SIZED_BY_FILE_ONLY("Priced by File"), SIZED_BY_FILE_ABOVE_TIER("Priced by File rounded up");

        private String label;

        private PricingType(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getLocaleKey() {
            return MessageHelper.formatLocalizableKey(this);
        }
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

    public boolean sameAs(PricingOption other) {
        if (other == null) {
            return false;
        }

        if (ObjectUtils.notEqual(getItems().size(), other.getItems().size())) {
            return false;
        }
        Map<Long, Integer> compMap = new HashMap<Long, Integer>();
        for (BillingItem item : getItems()) {
            compMap.put(item.getActivity().getId(), item.getQuantity());
        }
        for (BillingItem item : other.getItems()) {
            Integer key = compMap.get(item.getActivity().getId());
            if ((key == null) || !key.equals(item.getQuantity())) {
                return false;
            }
        }
        return true;

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

    @Override
    public String toString() {
        return String.format("%s: $%s", getType(), getSubtotal());
    }

    public PricingType getType() {
        return type;
    }

    public void setType(PricingType type) {
        this.type = type;
    }

    public Long getTotalMb() {
        Long mb = 0L;
        for (BillingItem item : items) {
            if ((item.getActivity().getNumberOfMb() != null) && (item.getQuantity() != null)) {
                mb += item.getQuantity().longValue() * item.getActivity().getNumberOfMb();
            }
        }
        return mb;
    }

    public Long getTotalFiles() {
        Long files = 0L;
        for (BillingItem item : items) {
            if ((item.getActivity().getNumberOfFiles() != null) && (item.getQuantity() != null)) {
                files += item.getQuantity().longValue() * item.getActivity().getNumberOfFiles();
            }
        }
        return files;
    }
}
