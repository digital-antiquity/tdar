package org.tdar.balk.bean;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.AbstractPersistable;

@Entity
@Table(name = "dropbox_state")
public class DropboxState extends AbstractPersistable {

    private static final long serialVersionUID = -6348303910937425382L;

    @Column(name = "db_cursor", length = 512)
    private String cursor;

    @Enumerated(EnumType.STRING)
    @Column(name = "poll_type", length=25)
    private PollType type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_polled")
    private Date lastPolled;

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public PollType getType() {
        return type;
    }

    public void setType(PollType type) {
        this.type = type;
    }

    public Date getLastPolled() {
        return lastPolled;
    }

    public void setLastPolled(Date lastPolled) {
        this.lastPolled = lastPolled;
    }
    
}
