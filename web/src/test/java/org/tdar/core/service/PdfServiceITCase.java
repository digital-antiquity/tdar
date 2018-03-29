package org.tdar.core.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.AbstractIntegrationWebTestCase;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentCitationFormatTestCase;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.utils.MessageHelper;


public class PdfServiceITCase extends AbstractIntegrationWebTestCase {

    @Autowired
    PdfService pdfService;

    @Before
    public void setup() {
        /** setup for jUnit
         */
        File f = new File("target/tdar-web/WEB-INF/themes/tdar/cover_page.pdf");
        if (!f.exists()) {
            File dir = new File("target/tdar-web/WEB-INF/themes/tdar/");
            dir.mkdirs();
            try {
                FileUtils.copyFile(new File("src/main/webapp/WEB-INF/themes/tdar/cover_page.pdf"), f);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }
    }
    
    @Test
    public void transliterateTest() {
        assertEquals("aeiou",pdfService.transliterate("åéîøü"));
    }
    

    
    @Test
    @Rollback(true)
    public void testPDFCoversheet() throws Exception {
        // NOTE: needs manual testing to ensure that this actually handles bookmarks properly...
        // THIS TEST WILL FAIL IF RUN IN ECLIPSE WITHOUT DOING A VERIFY FIRST (it needs access to includes)
        setupAndTest("Remote Sensing Methodology and the Chaco Canyon Prehistoric Road System");
    }

    @Test
    @Rollback(true)
    @Ignore("test for TDAR-1805")
    public void testPDFUnicodeCoversheet() throws Exception {
        // NOTE: needs manual testing to ensure that this actually handles bookmarks properly...
        // THIS TEST WILL FAIL IF RUN IN ECLIPSE WITHOUT DOING A VERIFY FIRST (it needs access to includes)
        setupAndTest("åéîøü - ʇsǝʇ ǝpoɔıun ʎɯ sı sıɥʇ");
    }

    private void setupAndTest(String title) throws InstantiationException, IllegalAccessException, IOException, FileNotFoundException {
        File f = TestConstants.getFile(TestConstants.TEST_DOCUMENT_DIR, "1-01.PDF");
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        Document document = generateAndStoreVersion(Document.class, "1-01.PDF", f, store);
        InformationResourceFileVersion originalVersion = document.getLatestUploadedVersion();
        // setup document
        DocumentCitationFormatTestCase.setupDocumentWithAllFields(document, DocumentType.BOOK);
        for (ResourceCreator c : document.getResourceCreators()) {
            genericService.saveOrUpdate(c.getCreator());
            genericService.saveOrUpdate(c);
        }
        genericService.saveOrUpdate(document.getPublisher());
        for (Person p : genericService.findRandom(Person.class, 30)) {
            document.getResourceCreators().add(new ResourceCreator(p, ResourceCreatorRole.AUTHOR));
        }
        genericService.saveOrUpdate(document.getResourceCreators().toArray());
        document.setTitle(title);
        String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus mi ipsum, pulvinar a faucibus in, feugiat id enim. Ut euismod neque eros, in auctor mi tristique et. Nulla ac varius nibh. Donec blandit nisl sit amet magna tempor adipiscing sed non diam. Morbi a tempor erat, eget adipiscing magna. Mauris semper tellus eget facilisis dapibus. Donec ut suscipit velit. Etiam vitae diam lacus. Aliquam placerat laoreet velit sit amet suscipit. Vestibulum tincidunt, sem sed aliquet congue, odio justo ultrices augue, quis faucibus ante velit ac neque. Quisque id pharetra enim. Proin rutrum leo eget vulputate suscipit. Nullam sit amet imperdiet elit.\r\nPraesent nisi purus, fringilla non orci at, malesuada malesuada risus. Pellentesque tempor vitae sem id rutrum. Pellentesque vitae enim diam. Ut gravida, dolor quis tincidunt ornare, libero risus viverra purus, ut molestie tortor est commodo leo. Aliquam dignissim facilisis felis vitae porta. Etiam mattis ligula ut scelerisque vehicula. Aenean ipsum libero, ultrices dignissim imperdiet nec, interdum eu magna. Donec ornare velit id urna condimentum, at eleifend metus blandit. Mauris quam ipsum, dapibus at velit sed, lacinia ultrices libero. Ut eleifend sem est, non tempor dolor imperdiet quis. Praesent feugiat, enim eget tincidunt feugiat, tortor felis suscipit eros, at accumsan libero neque in diam. Integer vel fringilla augue. \r\n Nam orci magna, semper sit amet laoreet sit amet, semper et mauris. Maecenas vitae sodales urna. Pellentesque eget risus augue. Aliquam ac porta massa, vel eleifend est. Pellentesque egestas suscipit velit, eu consequat arcu sollicitudin non. Integer sed eleifend massa. Quisque consequat mauris at orci porta, a tempus sem faucibus. Suspendisse sagittis urna id enim consequat sagittis. Ut lectus magna, iaculis ac urna nec, rutrum rhoncus risus. Integer tempor, sem ultrices venenatis molestie, metus nunc ultricies massa, ac posuere mi nisl quis orci. Sed fermentum fermentum enim et blandit. Fusce a nisl sodales, blandit sapien a, malesuada tortor. \r\n Integer faucibus metus quis suscipit feugiat. Sed condimentum eleifend bibendum. Sed et sollicitudin augue. Phasellus eleifend nibh orci, id porttitor neque volutpat a. Cras tempus, massa non vulputate iaculis, tellus sem dapibus lacus, eu congue diam nunc vitae magna. Nunc id elit nibh. Donec nec tempor lacus. Aliquam commodo ante cursus, placerat magna sit amet, porttitor lectus. \r\n Phasellus aliquet egestas leo, tempor cursus ligula posuere a. Suspendisse eget justo leo. Suspendisse potenti. Cras ut sem ut leo lobortis vestibulum gravida posuere eros. Donec lorem urna, hendrerit eget tempor ut, accumsan a sem. Vivamus ante augue, imperdiet in pretium ut, fringilla nec nisl. In non arcu feugiat, tincidunt nulla ut, sagittis mauris. Nam leo sapien, auctor et dui eu, varius suscipit libero. Pellentesque lectus est, semper vitae consequat ac, mollis nec risus. Vivamus molestie nec sapien id iaculis. Proin sed lacus placerat, convallis arcu at, ullamcorper lorem. Quisque tincidunt elementum ultrices. Praesent at risus est. Quisque semper convallis tempus. Donec tristique scelerisque sapien eu accumsan.";
        originalVersion.getInformationResourceFile().setDescription(text);
        InputStream merged = pdfService.mergeCoverPage(MessageHelper.getInstance(), getBasicUser(), originalVersion, document, null);
        File tempFile = File.createTempFile("temp_merge", ".pdf");
        IOUtils.copy(merged, new FileOutputStream(tempFile));
        logger.debug("{}", tempFile);
    }
}
