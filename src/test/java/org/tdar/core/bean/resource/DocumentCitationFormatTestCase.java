package org.tdar.core.bean.resource;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;

public class DocumentCitationFormatTestCase {

    @Test
    public void testBook() {
        Document doc = new Document();
        setupDocumentWithAllFields(doc, DocumentType.BOOK);
        assertEquals("publisherLocation: publisher. 1234", doc.getFormattedSourceInformation());
    }

    @Test
    public void testBookChapter() {
        Document doc = new Document();
        setupDocumentWithAllFields(doc, DocumentType.BOOK_SECTION);
        assertEquals("In bookTitle. Pp. startPage-endPage. publisherLocation: publisher. 1234", doc.getFormattedSourceInformation());
    }

    @Test
    public void testConferencePresentation() {
        Document doc = new Document();
        setupDocumentWithAllFields(doc, DocumentType.CONFERENCE_PRESENTATION);
        assertEquals("Presented at publisher, publisherLocation. 1234", doc.getFormattedSourceInformation());
    }

    @Test
    public void testJournalArticle() {
        Document doc = new Document();
        setupDocumentWithAllFields(doc, DocumentType.JOURNAL_ARTICLE);
        assertEquals("journalName. volume journalNumber: startPage-endPage. 1234", doc.getFormattedSourceInformation());
    }

    @Test
    public void testOther() {
        Document doc = new Document();
        setupDocumentWithAllFields(doc, DocumentType.OTHER);
        assertEquals("1234", doc.getFormattedSourceInformation());
    }

    @Test
    public void testThesis() {
        Document doc = new Document();
        setupDocumentWithAllFields(doc, DocumentType.THESIS);
        assertEquals("Thesis or Dissertation. publisher, publisherLocation. 1234", doc.getFormattedSourceInformation());
    }

    public static void setupDocumentWithAllFields(Document document, DocumentType type) {
        document.getResourceCreators().add(new ResourceCreator(document, new Person("First", "Last", "first@last"), ResourceCreatorRole.AUTHOR));
        document.getResourceCreators().add(new ResourceCreator(document, new Institution("institution auth"), ResourceCreatorRole.AUTHOR));
        document.getResourceCreators().add(new ResourceCreator(document, new Person("First2", "Last2", "first2@last"), ResourceCreatorRole.EDITOR));
        document.getResourceCreators().add(new ResourceCreator(document, new Person("First3", "Last3", "first3@last"), ResourceCreatorRole.COLLABORATOR));
        document.setDocumentType(type);
        document.setDate(1234);
        if (document.getId() == null || document.getId() == -1) {
            document.setId(123456L);
        }
        document.setDescription("description");
        document.setBookTitle("bookTitle");
        document.setDoi("doi");
        document.setEdition("edition");
        document.setEndPage("endPage");
        document.setIsbn("isbn");
        document.setIssn("issn");
        document.setJournalName("journalName");
        document.setJournalNumber("journalNumber");
        document.setNumberOfPages(10);
        document.setNumberOfVolumes(2);
        document.setPublisher("publisher");
        document.setPublisherLocation("publisherLocation");
        document.setSeriesName("seriesName");
        document.setSeriesNumber("seriesNumber");
        document.setStartPage("startPage");
        document.setVolume("volume");
    }

}
