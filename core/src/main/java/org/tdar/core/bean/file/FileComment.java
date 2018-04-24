package org.tdar.core.bean.file;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

@Entity
@Table(name = "file_comment")
public class FileComment extends AbstractPersistable {

    private static final long serialVersionUID = 4714812765008663814L;

    public FileComment() {
    }

    public FileComment(TdarUser user, String comment) {
        this.comment = comment;
        this.commentor = user;
        this.dateCreated = new Date();
    }

    @ManyToOne
    @JoinColumn(name = "commentor_id")
    private TdarUser commentor;

    @Column(name = "date_created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated = new Date();

    @Column(name = "comment", length = FieldLength.FIELD_LENGTH_2048)
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }


    public String getCommentorName() {
        if (commentor != null) {
            return commentor.getProperName();
        }
        return null;
    }

    @XmlElement(name = "uploaderRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getCommentor() {
        return commentor;
    }

    public void setCommentor(TdarUser commentor) {
        this.commentor = commentor;
    }
}
