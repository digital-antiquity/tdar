package org.tdar.search.service.index;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class Counter implements Serializable {

    private static final long serialVersionUID = -5010809939738705044L;
    private AtomicLong count = new AtomicLong(0);
    private AtomicLong subCount = new AtomicLong(0);
    private Long subTotal = 0L;
    private Long total = 0L;

    public AtomicLong getCount() {
        return count;
    }

    public void setCount(AtomicLong count) {
        this.count = count;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Long subTotal) {
        this.subTotal = subTotal;
    }

    public float getPercent() {
        return 100f * (count.floatValue() / total.floatValue());
    }

    /**
     * help's calcualate the percentage complete
     * 
     * @param total
     * @return
     */
    public int getDivisor(Number total) {
        int divisor = 5;
        if (total.intValue() < 50) {
            divisor = 2;
        } else if (total.intValue() < 100) {
            divisor = 20;
        } else if (total.intValue() < 1000) {
            divisor = 50;
        } else if (total.intValue() < 10000) {
            divisor = 500;
        } else {
            divisor = 5000;
        }
        return divisor;
    }
    public int getDivisor() {
        return getDivisor(subTotal);
    }

    public AtomicLong getSubCount() {
        return subCount;
    }

    public void setSubCount(AtomicLong subCount) {
        this.subCount = subCount;
    }

}
