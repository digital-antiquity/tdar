package org.tdar.core.bean.coverage;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.Range;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * Mapped superclass entity for CalendarDates and RadiocarbonDates.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "coverage_date", indexes = {
        @Index(name = "coverage_resid", columnList = "resource_id, id")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.coverage.CoverageDate")
public class CoverageDate extends AbstractPersistable implements HasResource<Resource>, Validatable {

    private static final long serialVersionUID = -5878760394443928287L;

    @Column(name = "start_date")
    @JsonView(JsonLookupFilter.class)
    private Integer startDate;

    @Column(name = "end_date")
    @JsonView(JsonLookupFilter.class)
    private Integer endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_type", length = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private CoverageType dateType;

    @Column(name = "start_aprox", nullable = false)
    private boolean startDateApproximate;

    @Column(name = "end_aprox", nullable = false)
    private boolean endDateApproximate;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private String description;

    public CoverageDate() {
    }

    public CoverageDate(CoverageType type) {
        setDateType(type);
    }

    public CoverageDate(CoverageType type, Integer start, Integer end) {
        setDateType(type);
        setStartDate(start);
        setEndDate(end);
    }

    public Integer getStartDate() {
        return startDate;
    }

    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    public Integer getEndDate() {
        return endDate;
    }

    public void setEndDate(Integer endDate) {
        this.endDate = endDate;
    }

    public void copyDatesFrom(CoverageDate coverageDate) {
        setStartDate(coverageDate.getStartDate());
        setEndDate(coverageDate.getEndDate());
    }

    @Transient
    @Override
    public boolean isValid() {
        return validate(startDate, endDate);
    }

    @Transient
    @Override
    public boolean isValidForController() {
        if ((dateType == null) || (startDate == null) || (endDate == null)) {
            return false;
        } else {
            return validate(startDate, endDate);
        }
    }

    protected boolean validate(Integer start, Integer end) {
        return getDateType().validate(start, end);
    }

    public void setDateType(CoverageType dateType) {
        this.dateType = dateType;
    }

    @JsonIgnore
    @XmlTransient
    public void setDateType(String dateType) {
        this.dateType = CoverageType.valueOf(dateType);
    }

    public CoverageType getDateType() {
        return dateType;
    }

    @Override
    public String toString() {
        if (getDateType() == null) {
            return String.format("%s: %s - %s", getDateType(), getStartDate(), getEndDate());
        }
        return String.format("%s: %s - %s", getDateType().getLabel(), getStartDate(), getEndDate());
    }

    /**
     * @param startDateApproximate
     *            the startDateApproximate to set
     */
    public void setStartDateApproximate(boolean startDateApproximate) {
        this.startDateApproximate = startDateApproximate;
    }

    /**
     * @return the startDateApproximate
     */
    public boolean isStartDateApproximate() {
        return startDateApproximate;
    }

    /**
     * @param endDateApproximate
     *            the endDateApproximate to set
     */
    public void setEndDateApproximate(boolean endDateApproximate) {
        this.endDateApproximate = endDateApproximate;
    }

    /**
     * @return the endDateApproximate
     */
    public boolean isEndDateApproximate() {
        return endDateApproximate;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    // package private
    @Transient
    Range<Integer> getRange() {
        Range<Integer> range = Range.between(startDate, endDate);
        return range;
    }

    // return true if the supplied covereageDate completely falls within this date range
    public boolean contains(CoverageDate coverageDate) {
        return (dateType == coverageDate.getDateType())
                && getRange().containsRange(coverageDate.getRange());
    }

    // return true if start or end (or both) falls within this coverageDate
    public boolean overlaps(CoverageDate coverageDate) {
        return (dateType == coverageDate.getDateType())
                && getRange().isOverlappedBy(coverageDate.getRange());
    }

    // is this date even worth 'evaluating'
    @Transient
    public boolean isInitialized() {
        if ((getStartDate() == null) && (getEndDate() == null)) {
            return false;
        }
        return true;
    }
}
