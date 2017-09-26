package org.tdar.core.bean.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DailyTotal {

    private Integer total;
    private Integer totalBot;
    private String dateString;
    private Date date;
    private List<Integer> totalDownloads = new ArrayList<>();
    
    public DailyTotal(Integer i1, Integer i2, String dateString, Date date) {
        this.setTotal(i1);
        this.setTotalBot(i2);
        this.setDateString(dateString);
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String date) {
        this.dateString = date;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Integer> getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(List<Integer> totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getTotalBot() {
        return totalBot;
    }

    public void setTotalBot(Integer totalBot) {
        this.totalBot = totalBot;
    }
    
}
