package org.tdar.core.bean.request;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;

/**
 * $Id$
 * <p>
 * A request for contributor status by a given Person.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Entity
@Table(name="contributor_request")
public class ContributorRequest extends Persistable.Base implements Comparable<ContributorRequest> {
    
    /**
     * 
     */
    private static final long serialVersionUID = -3070010016927757797L;
    private boolean approved;
    @Temporal(TemporalType.DATE)
    @Column(name="date_approved")
    private Date dateApproved;
    @ManyToOne
    private Person approver;
    @OneToOne
    private Person applicant;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable=false)
    private Date timestamp;
    
    @Column(length=512, name="contributor_reason")
    private String contributorReason;
    
    private String comments;
    
    public boolean isApproved() {
        return approved;
    }
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    public Date getDateApproved() {
        return dateApproved;
    }
    public void setDateApproved(Date dateApproved) {
        this.dateApproved = dateApproved;
    }
    public Person getApprover() {
        return approver;
    }
    public void setApprover(Person approver) {
        this.approver = approver;
    }
    public Person getApplicant() {
        return applicant;
    }
    public void setApplicant(Person applicant) {
        this.applicant = applicant;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public String getContributorReason() {
        return contributorReason;
    }
    public void setContributorReason(String contributorReason) {
        this.contributorReason = contributorReason;
    }
    public int compareTo(ContributorRequest contributorRequest) {
        return timestamp.compareTo(contributorRequest.timestamp);
    }
    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }
}
