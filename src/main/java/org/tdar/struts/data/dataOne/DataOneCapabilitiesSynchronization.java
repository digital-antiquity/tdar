package org.tdar.struts.data.dataOne;

import java.io.Serializable;

public class DataOneCapabilitiesSynchronization implements Serializable{

    private static final long serialVersionUID = -5071930264954358088L;
    private String hour;
    private String mday;
    private String min;
    private String mon;
    private String sec;
    private String wday;
    private String year;

    public String getHour() {
        return hour;
    }
    public void setHour(String hour) {
        this.hour = hour;
    }
    public String getMday() {
        return mday;
    }
    public void setMday(String mday) {
        this.mday = mday;
    }
    public String getMin() {
        return min;
    }
    public void setMin(String min) {
        this.min = min;
    }
    public String getMon() {
        return mon;
    }
    public void setMon(String mon) {
        this.mon = mon;
    }
    public String getSec() {
        return sec;
    }
    public void setSec(String sec) {
        this.sec = sec;
    }
    public String getWday() {
        return wday;
    }
    public void setWday(String wday) {
        this.wday = wday;
    }
    public String getYear() {
        return year;
    }
    public void setYear(String year) {
        this.year = year;
    }
}