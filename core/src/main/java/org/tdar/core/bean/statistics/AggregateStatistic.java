/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.statistics;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.AbstractPersistable;

/**
 * these are used to manage basic stats on tDAR...
 * 
 * @author Adam Brin
 * 
 */
@Entity
@Table(name = "stats")
public class AggregateStatistic extends AbstractPersistable {

    public enum StatisticType {
        NUM_USERS("# of Users"),
        NUM_IMAGE("# of Images"),
        NUM_DATASET("# of Data Sets"),
        NUM_PROJECT("# of Projects"),
        NUM_DOCUMENT(
                "# of Documents"),
        NUM_CODING_SHEET("# of Coding Sheets"),
        NUM_ONTOLOGY("# of Ontologies"),
        NUM_SENSORY_DATA(
                "# of Sensory Data Objects"),
        NUM_VIDEO(
                "# of Videos"),
        NUM_GIS("# of GeoSpatial Objects"),
        NUM_ARCHIVES("# of Archive Objects"),
        NUM_AUDIO("# of Audio Files"),

        NUM_VIDEO_WITH_FILES("# of Videos with Files"),
        NUM_IMAGE_WITH_FILES("# of Images with Files"),
        NUM_DATASET_WITH_FILES(
                "# of Data Sets with Files"),
        NUM_DOCUMENT_WITH_FILES("# of Documents with Files"),
        NUM_CODING_SHEET_WITH_FILES(
                "# of Coding Sheets with Files"),
        NUM_ONTOLOGY_WITH_FILES("# of Ontologies with Files"),
        NUM_SENSORY_DATA_WITH_FILES(
                "# of Sensory Data Objects with Files"),
        NUM_GIS_WITH_FILES("# of GeoSpatial Objects with Files"),
        NUM_ARCHIVES_WITH_FILES(
                "# of Archive Objects with Files"),
        NUM_AUDIO_WITH_FILES("# of Audio  with Files"),

        NUM_COLLECTIONS("# of Collections"),
        NUM_ACTUAL_CONTRIBUTORS("# of Contributors"),
        REPOSITORY_SIZE("Repository Size"),
        NUM_EMAILS(
                "# of Emails"),
        NUM_CULTURE("# of Culture Keywords (Controled)"),
        NUM_UNCONTROLLED_CULTURE(
                "# of Culture Keywords (Uncontroled)"),
        NUM_GEOGRAPHIC(
                "# of Geographic Keywords"),
        NUM_INVESTIGATION("# of Investigation Types"),
        NUM_MATERIAL(
                "# of Material Types"),
        NUM_OTHER("# of Other Keywords"),
        NUM_TEMPORAL("# of Temporal Keywords"),
        NUM_SITE_NAME(
                "# of Site Name Keywords"),
        NUM_SITE_TYPE("# of Site Type Keywords (Controled)"),
        NUM_UNCONTROLLED_SITE_TYPE(
                "# of Site Type Keywords (Uncontroled)"),
        NUM_COLLECTIONS_WHITE_LABEL(
                "# of White Label Collections"),
        NUM_SHARED_COLLECTIONS(
                "# of Shared Collections"),
        NUM_LIST_COLLECTIONS(
                "# of List Collections"),
        NUM_INTEGRATIONS("# of Data Integrations");

        private String label;

        private StatisticType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

    }

    private static final long serialVersionUID = 2693033966156306987L;

    private Long value;

    @Enumerated(EnumType.STRING)
    @Column(name = "stat_type")
    private StatisticType statisticType;

    private String comment;

    @Column(name = "recorded_date")
    @Temporal(TemporalType.DATE)
    private Date recordedDate;

    @Override
    public String toString() {
        StringBuffer toRet = new StringBuffer();
        toRet.append(statisticType);
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
