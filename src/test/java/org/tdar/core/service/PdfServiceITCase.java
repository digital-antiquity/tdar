package org.tdar.core.service;

import java.io.File;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentCitationFormatTestCase;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.filestore.PairtreeFilestore;

public class PdfServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    PdfService pdfService;

    @Test
    public void testPDFCoversheet() throws Exception {
        // NOTE: needs manual testing to ensure that this actually handles bookmarks properly...
        // THIS TEST WILL FAIL IF RUN IN ECLIPSE WITHOUT DOING A VERIFY FIRST (it needs access to includes)
        File f = new File(TestConstants.TEST_DOCUMENT_DIR, "1-01.PDF");
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);

        InformationResourceFileVersion originalVersion = generateAndStoreVersion(Document.class, "1-01.PDF", f, store);

        // setup document
        Document document = (Document) originalVersion.getInformationResourceFile().getInformationResource();
        DocumentCitationFormatTestCase.setupDocumentWithAllFields(document, DocumentType.BOOK);
        document.setTitle("Remote Sensing Methodology and the Chaco Canyon Prehistoric Road System");
        //aa aa aa aa aa aa aa aa aa aa aa aa a aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa a aa aa aa aa aa aa aa aa aa aa aa aa a aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa a aa aa aa aa aa aa aa aa aa aa aa aa a aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa a aa aa aa aa aa aa aa aa aa aa aa aa a aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa a");
        File merged = pdfService.mergeCoverPage(getBasicUser(), originalVersion);
        logger.debug("{}",merged);
    }
}
