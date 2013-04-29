package org.tdar.core.bean.coverage;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.Range;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.index.bridge.TdarPaddedNumberBridge;

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
@Table(name = "coverage_date")
public class CoverageDate extends Persistable.Base implements HasResource<Resource>, Validatable {

    private static final long serialVersionUID = -5878760394443928287L;

    @Column(name = "start_date")
    @Field(name = "startDate", store = Store.YES)
    // @NumericField
    @FieldBridge(impl = TdarPaddedNumberBridge.class)
    private Integer startDate;

    @Column(name = "end_date")
    @Field(name = "endDate", store = Store.YES)
    // @NumericField
    @FieldBridge(impl = TdarPaddedNumberBridge.class)
    private Integer endDate;

    @Enumerated(EnumType.STRING)
    @Field
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
    @Column(name = "date_type")
    private CoverageType dateType;

    @Column(name = "start_aprox", nullable = false)
    private boolean startDateApproximate;

    @Column(name = "end_aprox", nullable = false)
    private boolean endDateApproximate;

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
    public boolean isValid() {
        return validate(startDate, endDate);
    }

    @Transient
    public boolean isValidForController() {
        if (dateType == null || startDate == null || endDate == null) {
            return false;
        } else
            return validate(startDate, endDate);
    }

    protected boolean validate(Integer start, Integer end) {
        return getDateType().validate(start, end);
    }

    public void setDateType(CoverageType dateType) {
        this.dateType = dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = CoverageType.valueOf(dateType);
    }

    public CoverageType getDateType() {
        return dateType;
    }

    public String toString() {
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
        return dateType == coverageDate.getDateType()
                && getRange().containsRange(coverageDate.getRange());
    }

    // return true if start or end (or both) falls within this coverageDate
    public boolean overlaps(CoverageDate coverageDate) {
        return dateType == coverageDate.getDateType()
                && getRange().isOverlappedBy(coverageDate.getRange());
    }

    // is this date even worth 'evaluating'
    @Transient
    public boolean isInitialized() {
        if (getStartDate() == null && getEndDate() == null) {
            return false;
        }
        return true;
    }
}
