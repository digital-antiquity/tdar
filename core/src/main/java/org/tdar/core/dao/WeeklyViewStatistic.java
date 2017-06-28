package org.tdar.core.dao;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.tdar.core.bean.resource.Resource;

public class WeeklyViewStatistic implements Serializable {

    private static final long serialVersionUID = 513580771738137788L;
    private DateTime end;
    private Resource resource;
    private Integer d1;
    private Integer d2;
    private Integer d3;
    private Integer d4;
    private Integer d5;
    private Integer d6;
    private Integer d7;
    private DateTime start;

    public WeeklyViewStatistic(Resource resource, Integer d1, Integer d2, Integer d3, Integer d4, Integer d5, Integer d6, Integer d7, DateTime start,
            DateTime end) {
        this.resource = resource;
        this.d1 = d1;
        this.d2 = d2;
        this.d3 = d3;
        this.d4 = d4;
        this.d5 = d5;
        this.d6 = d6;
        this.d7 = d7;
        this.start = start;
        this.end = end;

    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Integer getD1() {
        return d1;
    }

    public void setD1(Integer d1) {
        this.d1 = d1;
    }

    public Integer getD2() {
        return d2;
    }

    public void setD2(Integer d2) {
        this.d2 = d2;
    }

    public Integer getD3() {
        return d3;
    }

    public void setD3(Integer d3) {
        this.d3 = d3;
    }

    public Integer getD4() {
        return d4;
    }

    public void setD4(Integer d4) {
        this.d4 = d4;
    }

    public Integer getD5() {
        return d5;
    }

    public void setD5(Integer d5) {
        this.d5 = d5;
    }

    public Integer getD6() {
        return d6;
    }

    public void setD6(Integer d6) {
        this.d6 = d6;
    }

    public Integer getD7() {
        return d7;
    }

    public void setD7(Integer d7) {
        this.d7 = d7;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

}
