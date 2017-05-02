package org.tdar.oai.bean;

import java.util.Date;

import org.joda.time.DateTime;

public class Token {

    private Long idFrom = -1L;
    private Date after = DateTime.parse("2000-01-01").toDate();

    public Token() {
    }

    public Token(String string) {
        String[] split = string.split(";");
        idFrom = Long.parseLong(split[0]);
        after = new Date(Long.parseLong(split[1]));
    }

    public Long getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(Long idFrom) {
        this.idFrom = idFrom;
    }

    public Date getAfter() {
        return after;
    }

    public void setAfter(Date after) {
        this.after = after;
    }

    @Override
    public String toString() {
        return String.format("%s;%s", idFrom, after.getTime());
    }
}
