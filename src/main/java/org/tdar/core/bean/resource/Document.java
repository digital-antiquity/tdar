package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.tdar.core.bean.BulkImportField;
import org.tdar.index.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.index.TdarStandardAnalyzer;

import com.thoughtworks.xstream.annotations.XStreamAlias;

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
@XStreamAlias("document")
@XmlRootElement
public class Document extends InformationResource {

    private static final long serialVersionUID = 7895887664126751989L;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    @Field
    @Analyzer(impl = TdarStandardAnalyzer.class)
    @BulkImportField
    private DocumentType documentType;

    @BulkImportField
    @Column(name = "series_name")
    @Field
    private String seriesName;

    @BulkImportField
    @Column(name = "series_number")
    private String seriesNumber;

    @BulkImportField
    @Column(name = "number_of_pages")
    private Integer numberOfPages;

    @BulkImportField
    private String edition;

    @BulkImportField
    @Column(name = "publisher_location")
    private String publisherLocation;

    @BulkImportField
    @Field
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    private String publisher;

    @BulkImportField
    @Field
    @Analyzer(impl = KeywordAnalyzer.class)
    private String isbn;

    @BulkImportField
    @Column(name = "book_title")
    @Field(boost = @Boost(1.5f))
    private String bookTitle;

    @BulkImportField
    @Field
    @Analyzer(impl = KeywordAnalyzer.class)
    private String issn;

    @BulkImportField
    @Field
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    private String doi;

    @BulkImportField
    @Column(name = "start_page")
    private String startPage;

    @BulkImportField
    @Column(name = "end_page")
    private String endPage;

    @BulkImportField
    @Column(name = "journal_name")
    @Field
    private String journalName;

    @BulkImportField
    private String volume;

    @BulkImportField
    @Column(name = "number_of_volumes")
    private Integer numberOfVolumes;

    @BulkImportField
    @Column(name = "journal_number")
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

    public String getPublisherLocation() {
        return publisherLocation;
    }

    public void setPublisherLocation(String publisherLocation) {
        this.publisherLocation = publisherLocation;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
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

    public Integer getNumberOfPages() {
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

    // public void addAuthor(DocumentAuthor documentCreatorPerson) {
    // documentCreatorPersons.add(documentCreatorPerson);
    // }

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

    @Transient
    public String getText() {
        // return a formatted citation?
        return "This method is currently stubbed out.";
    }

}
