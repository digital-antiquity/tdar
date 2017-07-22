package org.tdar.core.bean.statistics;

public class DailyTotal {

    private Integer total;
    private String date;
    
    public DailyTotal(Integer total, String date) {
        this.setTotal(total);
        this.setDate(date);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
