package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;

/**
 * $Id$
 * 
 * Represents a Document information resource.
 * 
 * The design decision was made to have null fields instead of overloading fields to mean different things for different
 * document types, e.g., a journal article has journal volume, journal name, and journal number instead of series name, series number,
 * and volume / # of volumes
 * 
 * NOTE: uses Resource.dateCreated as year published field
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Indexed
@Table(name = "document")
@XmlRootElement(name = "document")
public class Document extends InformationResource {

    private static final long serialVersionUID = 7895887664126751989L;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 255)
    @Index(name = "document_type_index")
    @Field(norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class))
    @BulkImportField(label = "Document Type")
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree", length = 50)
    @Field(norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class))
    @BulkImportField(label = "Degree")
    private DegreeType degree;

    @BulkImportField(label = "Series Name")
    @Column(name = "series_name")
    @Field
    @Length(max = 255)
    private String seriesName;

    @BulkImportField(label = "Series Number")
    @Column(name = "series_number")
    @Length(max = 255)
    private String seriesNumber;

    @Column(name = "number_of_pages")
    private Integer numberOfPages;

    @BulkImportField(label = "Edition")
    @Length(max = 255)
    private String edition;

    @BulkImportField(label = "ISBN")
    @Field
    @Length(max = 255)
    @Analyzer(impl = KeywordAnalyzer.class)
    private String isbn;

    @BulkImportField(label = "Book Title")
    @Length(max = 255)
    @Column(name = "book_title")
    @Field
    // @Boost(1.5f)
    private String bookTitle;

    @BulkImportField(label = "ISSN")
    @Field
    @Length(max = 255)
    @Analyzer(impl = KeywordAnalyzer.class)
    private String issn;

    @BulkImportField(label = "DOI")
    @Field
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    @Length(max = 255)
    private String doi;

    @BulkImportField(label = "Start Page", order = 10)
    @Column(name = "start_page")
    @Length(max = 10)
    private String startPage;

    @BulkImportField(label = "End Page", order = 11)
    @Column(name = "end_page")
    @Length(max = 10)
    private String endPage;

    @BulkImportField(label = "Journal Name")
    @Column(name = "journal_name")
    @Field
    @Length(max = 255)
    private String journalName;

    @BulkImportField(label = "Volume")
    @Length(max = 255)
    private String volume;

    @BulkImportField(label = "# of Volumes")
    @Column(name = "number_of_volumes")
    private Integer numberOfVolumes;

    @BulkImportField(label = "Journal Number")
    @Column(name = "journal_number")
    @Length(max = 255)
    private String journalNumber;

    public Document() {
        setResourceType(ResourceType.DOCUMENT);
        setDocumentType(DocumentType.BOOK);
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String type) {
        setDocumentType(DocumentType.valueOf(type));
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getNumberOfVolumes() {
        return numberOfVolumes;
    }

    public void setNumberOfVolumes(Integer numberOfVolumes) {
        this.numberOfVolumes = numberOfVolumes;
    }

    @Deprecated
    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    public Integer getTotalNumberOfPages() {
        Integer count = 0;
        if (CollectionUtils.isNotEmpty(getInformationResourceFiles())) {
            for (InformationResourceFile file : getInformationResourceFiles()) {
                if (!file.isDeleted() && file.getNumberOfParts() != null) {
                    count += file.getNumberOfParts();
                }
            }
            if (count > 0) {
                return count;
            }
        }
        return numberOfPages;
    }

    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getStartPage() {
        return startPage;
    }

    public void setStartPage(String startPage) {
        this.startPage = startPage;
    }

    public String getEndPage() {
        return endPage;
    }

    public void setEndPage(String endPage) {
        this.endPage = endPage;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getJournalName() {
        return journalName;
    }

    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    public String getJournalNumber() {
        return journalNumber;
    }

    public void setJournalNumber(String journalNumber) {
        this.journalNumber = journalNumber;
    }

    @JSONTransient
    @Override
    public String getFormattedTitleInfo() {
        StringBuilder sb = new StringBuilder();
        appendIfNotBlank(sb, getTitle(), "", "");
        appendIfNotBlank(sb, getEdition(), ",", "");
        return sb.toString();
    }

    // FIXME: ADD IS?N
    @JSONTransient
    @Override
    //TODO: refactor using MessageFormat or with a freemarker template
    public String getFormattedSourceInformation() {
        StringBuilder sb = new StringBuilder();
        switch (getDocumentType()) {
            case BOOK:
                appendIfNotBlank(sb, getPublisherLocation(), "", "");
                appendIfNotBlank(sb, getPublisherName(), ":", "");
                break;
            case BOOK_SECTION:
                appendIfNotBlank(sb, getBookTitle(), "", "In ");
                appendIfNotBlank(sb, getPageRange(), ".", "Pp. ");
                appendIfNotBlank(sb, getPublisherLocation(), ".", "");
                appendIfNotBlank(sb, getPublisherName(), ":", "");
                break;
            case CONFERENCE_PRESENTATION:
                appendIfNotBlank(sb, getPublisherName(), "", "Presented at ");
                appendIfNotBlank(sb, getPublisherLocation(), ",", "");
                break;
            case JOURNAL_ARTICLE:
                appendIfNotBlank(sb, getJournalName(), "", "");
                if (StringUtils.isNotBlank(getJournalNumber()) || StringUtils.isNotBlank(getVolume())) {
                    sb.append(".");
                }
                appendIfNotBlank(sb, getVolume(), "", "");
                if(StringUtils.isNotBlank(getJournalNumber())) {
                    appendIfNotBlank(sb, "(" + getJournalNumber() + ")", "", "");
                }
                appendIfNotBlank(sb, getPageRange(), ":", "");
                break;
            case OTHER:
                break;
            case THESIS:
                String degreetext = "";
                if (getDegree() != null) {
                    degreetext = getDegree().getLabel();
                }
                appendIfNotBlank(sb, degreetext + ".", "", "");
                appendIfNotBlank(sb, getPublisherName(), "", "");
                appendIfNotBlank(sb, getPublisherLocation(), ",", "");
                break;
        }
        if (getDate() != null && getDate() != -1) {
            appendIfNotBlank(sb, getDate().toString(), ".", "");
        }
        return sb.toString();
    }

    @Override
    public String getAdditonalKeywords() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getAdditonalKeywords()).append(" ").append(getBookTitle()).append(" ").append(getDoi()).
                append(" ").append(getIssn()).append(" ").append(getIsbn()).append(" ").append(getPublisher()).append(" ").
                append(getSeriesName());
        return sb.toString();
    }

    public String getPageRange() {
        StringBuilder sb = new StringBuilder();
        appendIfNotBlank(sb, getStartPage(), "", "");
        appendIfNotBlank(sb, getEndPage(), "-", "");
        return sb.toString().replaceAll("\\s", "");
    }

    public DegreeType getDegree() {
        return degree;
    }

    public void setDegree(DegreeType degree) {
        this.degree = degree;
    }

    @Override
    @Transient
    public boolean isSupportsThumbnails() {
        return true;
    }
}
