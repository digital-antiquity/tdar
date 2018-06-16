package org.tdar.core.bean.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DailyTotal {

    private Integer total = 0;
    private Integer totalBot = 0;
    private String dateString;
    private Date date;
    private List<Integer> totalDownloads = new ArrayList<>();

    public DailyTotal(Integer i1, Integer i2, String dateString, Date date, List<Integer> downloads) {
        this.setTotal(i1);
        this.setTotalBot(i2);
        this.setDate(date);
        this.setDateString(dateString);
        this.setTotalDownloads(downloads);
    }

    public DailyTotal(Integer i1, Integer i2, String dateString, Date date) {
        this(i1, i2, dateString, date, null);
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

    public void addTotalBot(Number totalBot) {
        if (this.totalBot == null) {
            this.totalBot = 0;
        }
        if (totalBot != null) {
            this.totalBot += totalBot.intValue();
        }
    }

    public void addTotal(Number totalBot) {
        if (this.total == null) {
            this.total = 0;
        }
        if (totalBot != null) {
            this.total += totalBot.intValue();
        }
    }

    public void addTotalDownload(int i, Long count) {
        if (count == null || count == 0L) {
            return;
        }
        Integer integer = getTotalDownloads().get(i);
        getTotalDownloads().set(i, integer + count.intValue());

    }

    public void addTotals(Number total2, Number totalBot2, List<Integer> createDownloadList) {
        addTotal(total2);
        addTotalBot(totalBot2);
        setTotalDownloads(createDownloadList);
    }

}
