package org.tdar.core.service.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.ArchiveEvaluator;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;


public class ResourceExportServiceITCase extends AbstractIntegrationTestCase {

    private static final String TDAR_XSD = "tdar.xsd";
    private static final String TEST123_ZIP = "test123.zip";
    private static final String ARCHIVAL = "archival/";
    private static final String FILES = "files/";
    private static final String RESOURCE_XML = "resource.xml";
    @Autowired
    ResourceExportService exportService;

    @Test
    @Rollback
    public void testSingleExport() throws Exception {
        Document doc = generateDocumentWithFileAndUser();
        File export = exportService.export(TEST123_ZIP, false, new HashSet<Resource>( Arrays.asList(doc)));
        logger.debug("exported:{}", export);
        Map<String, Long> nameSize = ArchiveEvaluator.unzipArchive(export);
        String prefix = doc.getResourceType().name() + "/" + doc.getId() + "/";
        String filename = doc.getFirstInformationResourceFile().getFilename();
        assertTrue("archive contains schema",nameSize.containsKey(TDAR_XSD));
        assertContains(nameSize, prefix, filename);
        ZipFile zipfile = new ZipFile(export);
        ZipEntry entry = zipfile.getEntry(prefix + RESOURCE_XML);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(zipfile.getInputStream(entry), baos);
        String string = new String( baos.toByteArray(), "utf8");
        logger.debug(string);
        zipfile.close();
        assertTrue("xml contains id:" + doc.getId(), string.contains("id=\""+doc.getId()+"\""));



    }

    private void assertContains(Map<String, Long> nameSize, String prefix, String filename) {
        logger.debug("{}", nameSize);
        logger.debug("prefix: {} filename: {}", prefix,filename);
        assertTrue("archive contains uploaded",nameSize.containsKey(prefix + FILES + filename));
        assertTrue("archive contains archival",nameSize.containsKey(prefix + ARCHIVAL + filename));
        assertTrue("archive contains resource",nameSize.containsKey(prefix + RESOURCE_XML));
    }

    @Test
    @Rollback
    public void testMultipleExport() throws Exception {
        Document doc = generateDocumentWithFileAndUser();
        Image img = generateAndStoreVersion(Image.class, TestConstants.TEST_IMAGE_NAME, new File(TestConstants.TEST_IMAGE_DIR,TestConstants.TEST_IMAGE_NAME), TdarConfiguration.getInstance().getFilestore());
        File export = exportService.export(TEST123_ZIP, false,new HashSet<Resource>( Arrays.asList(doc,img)));
        logger.debug("exported:{}", export);
        Map<String, Long> nameSize = ArchiveEvaluator.unzipArchive(export);
        String prefix = doc.getResourceType().name() + "/" + doc.getId() + "/";
        String filename = doc.getFirstInformationResourceFile().getFilename();
        assertTrue("archive contains schema",nameSize.containsKey(TDAR_XSD));
        prefix = img.getResourceType().name() + "/" + img.getId() + "/";
        filename = img.getFirstInformationResourceFile().getFilename();
        assertContains(nameSize, prefix, filename);
    }

    @Test
    @Rollback
    public void testSingleExportForReimport() throws Exception {
        Document doc = generateDocumentWithFileAndUser();
        Long id = doc.getId();
        File export = exportService.export(TEST123_ZIP, true,new HashSet<Resource>( Arrays.asList(doc)));
        logger.debug("exported:{}", export);
        ZipFile zipfile = new ZipFile(export);
        Map<String, Long> nameSize = ArchiveEvaluator.unzipArchive(export);
        logger.debug("{}", nameSize);
        String name = doc.getResourceType().name() + "/" + id + "/" + RESOURCE_XML;
        logger.debug(name);
        ZipEntry entry = zipfile.getEntry(name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(zipfile.getInputStream(entry), baos);
        String string = new String( baos.toByteArray(), "utf8");
        logger.debug(string);
        zipfile.close();
        assertFalse("xml contains id", string.contains("informationResourceId>"+id));
        assertTrue("xml contains -1 id", string.contains("-1"));
        assertTrue("xml contains fileProxy", string.contains("fileProxy"));
    }


    @Test
    @Rollback
    public void testExportEmail() throws Exception {
        ResourceExportProxy prox = new ResourceExportProxy(getAdminUser());
        exportService.sendEmail(prox, getAdminUser());
    }
}
