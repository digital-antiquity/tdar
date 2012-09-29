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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.tdar.core.bean.Persistable;

/**
 * @author Adam Brin
 * 
 */
@Entity
@Table(name = "stats")
public class Statistic extends Persistable.Base {

    public enum StatisticType {
        NUM_USERS, NUM_IMAGE, NUM_DATASET, NUM_PROJECT, NUM_DOCUMENT, NUM_CODING_SHEET, NUM_ONTOLOGY,NUM_SENSORY_DATA
    }

    private static final long serialVersionUID = 2693033966156306987L;

    private Long value;

    @Enumerated(EnumType.STRING)
    @Column(name = "stat_type")
    private StatisticType statisticType;

    private String comment;

    @Column(name = "recorded_date")
    private Date recordedDate;

    
    @Override
    public String toString() {
        StringBuffer toRet = new StringBuffer(statisticType.toString());
        toRet.append(":");
        toRet.append(value);
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
     * @param value
     *            the value to set
     */
    public void setValue(Long value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public Long getValue() {
        return value;
    }

    /**
     * @param statisticType
     *            the statisticType to set
     */
    public void setStatisticType(StatisticType statisticType) {
        this.statisticType = statisticType;
    }

    /**
     * @return the statisticType
     */
    public StatisticType getStatisticType() {
        return statisticType;
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

}
