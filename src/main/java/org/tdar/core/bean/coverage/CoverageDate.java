package org.tdar.core.bean.coverage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.index.analyzer.TdarStandardAnalyzer;
import org.tdar.index.bridge.TdarPaddedNumberBridge;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

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
@XStreamAlias("coverageDate")
public class CoverageDate extends Persistable.Base implements HasResource<Resource> {

    private static final long serialVersionUID = -5878760394443928287L;

    @XmlTransient
    @OneToOne
    private Resource resource;

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
    @Analyzer(impl = TdarStandardAnalyzer.class)
    @Column(name = "date_type")
    @XStreamAsAttribute
    @XStreamAlias("type")
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

    @XmlTransient
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Transient
    public boolean isValid() {
        return isValid(startDate, endDate);
    }

    @Transient
    public boolean isValid(Integer start, Integer end) {
        if (start == null || end == null) {
            return false;
        } else
            return validate(start, end);
    }

    protected boolean validate(Integer start, Integer end) {
        return getDateType().validate(start, end);
    }

    public void setDateType(CoverageType dateType) {
        this.dateType = dateType;
    }

    public CoverageType getDateType() {
        return dateType;
    }

    public String toString() {
        return String.format(getDateType().getFormat(), getStartDate(), getEndDate());
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

    public boolean isValidForController() {
        return true;
    }
}
