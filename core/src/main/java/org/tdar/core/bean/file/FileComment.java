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

    @ManyToOne
    @JoinColumn(name = "resolver_id")
    private TdarUser resolver;

    @Column(name = "date_created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated = new Date();

    @Column(name = "date_resolved", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateResolved = new Date();

    @Column(name = "resolved", nullable = false, columnDefinition = "boolean default false")
    private Boolean resolved = false;

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

    @XmlElement(name = "commentorRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getCommentor() {
        return commentor;
    }

    public void setCommentor(TdarUser commentor) {
        this.commentor = commentor;
    }

    public String getCommentorInitials() {
        if (commentor != null) {
            return commentor.getInitials();
        }
        return null;
    }

    @XmlElement(name = "resolverRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getResolver() {
        return resolver;
    }

    public void setResolver(TdarUser resolver) {
        this.resolver = resolver;
    }

    public Date getDateResolved() {
        return dateResolved;
    }

    public void setDateResolved(Date dateResolved) {
        this.dateResolved = dateResolved;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public String getResolverInitials() {
        if (resolver != null) {
            return resolver.getInitials();
        }
        return null;
    }

    public String getResolverName() {
        if (resolver != null) {
            return resolver.getProperName();
        }
        return null;
    }

}
