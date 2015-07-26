package org.tdar.core.service.processes.relatedInfoLog;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.Persistable;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "creatorInfoLog")
public class RelatedInfoLog {
    private List<RelatedInfoLogPart> collaboratorLogPart = new ArrayList<>();
    private List<RelatedInfoLogPart> keywordLogPart = new ArrayList<>();
    private Persistable about;
    private int totalRecords;
    private Double creatorMean;
    private Double creatorMedian;
    private Double keywordMean;
    private Double keywordMedian;

    @XmlAttribute
    public Double getCreatorMean() {
        return creatorMean;
    }

    public void setCreatorMean(Double creatorMean) {
        this.creatorMean = creatorMean;
    }

    @XmlAttribute
    public Double getCreatorMedian() {
        return creatorMedian;
    }

    public void setCreatorMedian(Double creatorMedian) {
        this.creatorMedian = creatorMedian;
    }

    @XmlAttribute
    public Double getKeywordMean() {
        return keywordMean;
    }

    public void setKeywordMean(Double keywordMean) {
        this.keywordMean = keywordMean;
    }

    @XmlAttribute
    public Double getKeywordMedian() {
        return keywordMedian;
    }

    public void setKeywordMedian(Double keywordMedian) {
        this.keywordMedian = keywordMedian;
    }

    @XmlElementWrapper(name = "collaborators")
    @XmlElement(name = "collaborator")
    public List<RelatedInfoLogPart> getCollaboratorLogPart() {
        return collaboratorLogPart;
    }

    public void setCollaboratorLogPart(List<RelatedInfoLogPart> collaboratorLogPart) {
        this.collaboratorLogPart = collaboratorLogPart;
    }

    @XmlElementWrapper(name = "keywords")
    @XmlElement(name = "keyword")
    public List<RelatedInfoLogPart> getKeywordLogPart() {
        return keywordLogPart;
    }

    public void setKeywordLogPart(List<RelatedInfoLogPart> keywordLogPart) {
        this.keywordLogPart = keywordLogPart;
    }

    @XmlAttribute(name = "aboutRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Persistable getAbout() {
        return about;
    }

    public void setAbout(Persistable creator) {
        this.about = creator;
    }

    @XmlAttribute
    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
}