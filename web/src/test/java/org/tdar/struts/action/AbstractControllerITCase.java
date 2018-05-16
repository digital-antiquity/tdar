package org.tdar.struts.action;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.tdar.TestConstants;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.codingSheet.CodingSheetController;
import org.tdar.struts.action.dataset.DatasetController;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.image.ImageController;
import org.tdar.struts.action.ontology.OntologyController;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.action.resource.AbstractSupportingInformationResourceController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractControllerITCase extends AbstractIntegrationControllerTestCase implements TestFileUploadHelper {

    private static final String PATH = TestConstants.TEST_ROOT_DIR;

    public <C> C setupAndLoadResource(String filename, Class<C> cls) throws Exception {
        return setupAndLoadResource(filename, cls, FileAccessRestriction.PUBLIC, -1L);
    }

    public <C> C setupAndLoadResource(String filename, Class<C> cls, FileAccessRestriction permis) throws Exception {
        return setupAndLoadResource(filename, cls, permis, -1L);
    }

    public <C> C setupAndLoadResource(String filename, Class<C> cls, Long id) throws Exception {
        return setupAndLoadResource(filename, cls, FileAccessRestriction.PUBLIC, id);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <C> C setupAndLoadResource(String filename, Class<C> cls, FileAccessRestriction permis, Long id) throws Exception {

        AbstractInformationResourceController controller = null;
        Long ticketId = -1L;
        if (cls.equals(Ontology.class)) {
            controller = generateNewInitializedController(OntologyController.class);
        } else if (cls.equals(Dataset.class)) {
            controller = generateNewInitializedController(DatasetController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(Document.class)) {
            controller = generateNewInitializedController(DocumentController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(Image.class)) {
            controller = generateNewInitializedController(ImageController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(CodingSheet.class)) {
            controller = generateNewInitializedController(CodingSheetController.class);
        }
        if (controller == null) {
            return null;
        }

        if (PersistableUtils.isNotNullOrTransient(id)) {
            controller.setId(id);
        }
        controller.prepare();
        final Resource resource = controller.getResource();
        resource.setTitle(filename);
        resource.setDescription("This resource was created as a result of a test: " + getClass());
        if ((resource instanceof InformationResource) && TdarConfiguration.getInstance().getCopyrightMandatory()) {
            Creator copyrightHolder = genericService.find(Person.class, 1L);
            ((InformationResource) resource).setCopyrightHolder(copyrightHolder);
        }

        List<File> files = new ArrayList<File>();
        List<String> filenames = new ArrayList<String>();
        if (ticketId != -1) {
            controller.setTicketId(ticketId);
            controller.setFileProxies(Arrays.asList(new FileProxy(FilenameUtils.getName(filename), VersionType.UPLOADED, permis)));
        } else {
            File file = new File(getTestFilePath(), filename);
            assertTrue("file not found:" + getTestFilePath() + "/" + filename, file.exists());
            if (FilenameUtils.getExtension(filename).equals("txt") && (controller instanceof AbstractSupportingInformationResourceController<?>)) {
                AbstractSupportingInformationResourceController<?> asc = (AbstractSupportingInformationResourceController<?>) controller;
                asc.setFileInputMethod(AbstractInformationResourceController.FILE_INPUT_METHOD);
                try {
                    asc.setFileTextInput(FileUtils.readFileToString(file));
                } catch (Exception e) {
                    Assert.fail(e.getMessage());
                }
            } else {
                files.add(file);
                filenames.add(filename);
                controller.setUploadedFiles(files);
                controller.setUploadedFilesFileName(filenames);
            }
        }
        try {
            controller.setServletRequest(getServletPostRequest());
            controller.save();
        } catch (TdarActionException exception) {
            // what now?
            exception.printStackTrace();
        }
        return (C) controller.getResource();
    }

    public String getTestFilePath() {
        return PATH;
    }

    @Override
    public TdarUser getUser() {
        return getUser(getUserId());
    }

    protected void reindex() {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE, LookupSource.COLLECTION);
    }

    @Override
    public PersonalFilestoreService getFilestoreService() {
        return filestoreService;
    }

}
