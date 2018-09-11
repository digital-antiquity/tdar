package org.tdar.core.bean;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.filestore.personal.PersonalFileType;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * This allows for asynchronous uploads by creating a ticket that tracks the filestore (where things are stored temporarily)
 * and the submitter. The ticket gets created at the beginning of the first upload, and is then kept open and available until
 * the user completes the resource submission process.
 * 
 * @author Jim DeVos
 * @version $Rev$
 */
@Entity
@Table(name = "personal_filestore_ticket")
public class PersonalFilestoreTicket extends AbstractPersistable {

    private static final long serialVersionUID = 3712388159075958666L;

    @Column(nullable = false, name = "date_generated")
    @JsonView(JsonLookupFilter.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateGenerated = new Date();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "personal_file_type")
    private PersonalFileType personalFileType;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "submitter_id")
    @JsonView(JsonLookupFilter.class)
    private TdarUser submitter;

    @Column(length = FieldLength.FIELD_LENGTH_500)
    private String description;

    @XmlElement(name = "submitterRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
        this.submitter = submitter;
    }

    public PersonalFileType getPersonalFileType() {
        return personalFileType;
    }

    public void setPersonalFileType(PersonalFileType personalFileType) {
        this.personalFileType = personalFileType;
    }

    public Date getDateGenerated() {
        return dateGenerated;
    }

    public void setDateGenerated(Date dateGenerated) {
        this.dateGenerated = dateGenerated;
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

    @Override
    public String toString() {
        return String.format("[ticket id:%s submitter:%s date:%s]", getId(), submitter, dateGenerated);
    }

}
