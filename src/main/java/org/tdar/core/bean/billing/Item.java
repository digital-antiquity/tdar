package org.tdar.core.bean.billing;

import org.tdar.core.bean.Persistable.Base;

public class Item extends Base {

    private static final long serialVersionUID = -2775737509085985555L;

    private Activity activity;
    private Integer quantity;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
