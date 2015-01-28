/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.util;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.Persistable;

/**
 * A class to manage scheduled processes and upgrades ... really just running things once
 * 
 * @author Adam Brin
 */
@Entity
@Table(name = "upgrade_task")
public class UpgradeTask extends Persistable.Base {

    private static final long serialVersionUID = -564380362132231493L;
    private String comment;
    private String name;

    @Column(name = "recorded_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date recordedDate;

    private Boolean run = Boolean.valueOf(false);

    @Override
    public String toString() {
        StringBuffer toRet = new StringBuffer();
        toRet.append(":");
        return toRet.toString();
    }

    /**
     * @param recordedDate
     *            the recordedDate to set
     */
    public void setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
    }

    /**
     * @return the recordedDate
     */
    public Date getRecordedDate() {
        return recordedDate;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param run
     *            the run to set
     */
    public void setRun(Boolean run) {
        this.run = run;
    }

    /**
     * @return the run
     */
    public Boolean getRun() {
        return run;
    }

    public Boolean hasRun() {
        return run;
    }

}
