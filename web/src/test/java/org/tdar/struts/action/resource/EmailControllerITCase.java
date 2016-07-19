package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.external.RecaptchaService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.struts.action.email.EmailController;
import org.tdar.utils.EmailMessageType;

public class EmailControllerITCase extends AbstractResourceControllerITCase {
    @Autowired
    RecaptchaService recaptchaService;

    @SuppressWarnings("deprecation")
    @Test
    public void testSuccess() throws Exception {
        Document document = genericService.find(Document.class, Long.parseLong(TestConstants.TEST_DOCUMENT_ID));
        for (EmailMessageType type : EmailMessageType.values()) {
            EmailController ec = generateNewInitializedController(EmailController.class, getBasicUser());
            AntiSpamHelper h = new AntiSpamHelper();
            h.setTimeCheck(h.getTimeCheck() - 5000);
            ec.setResourceId(document.getId());
            ec.setFromId(getBasicUserId());
            ec.setType(type);
            ec.setToId(getAdminUserId());
            ec.setMessageBody("this is a test");
            ec.setH(h);
            ec.prepare();
            ec.execute();
            genericService.synchronize();
            Email email = ec.getEmail();
            assertEquals(Status.IN_REVIEW, email.getStatus());
            email.setStatus(Status.QUEUED);
            genericService.saveOrUpdate(email);
            checkMailAndGetLatest("this is a test");
//            assertTrue(received.getText().contains("this is a test"));
        }
    }

    @Test()
    public void testNoMessage() throws Exception {
        setIgnoreActionErrors(true);
        Document document = genericService.find(Document.class, Long.parseLong(TestConstants.TEST_DOCUMENT_ID));
        EmailController ec = generateNewInitializedController(EmailController.class, getBasicUser());
        AntiSpamHelper h = new AntiSpamHelper();
        h.setTimeCheck(h.getTimeCheck() - 5000);
        ec.setResourceId(document.getId());
        ec.setFromId(getBasicUserId());
        ec.setType(EmailMessageType.CONTACT);
        ec.setToId(getAdminUserId());
        ec.setH(h);
        ec.prepare();
        ec.validate();
        assertFalse(ec.getActionErrors().isEmpty());
    }

    @Test()
    public void testNoType() throws Exception {
        setIgnoreActionErrors(true);
        Document document = genericService.find(Document.class, Long.parseLong(TestConstants.TEST_DOCUMENT_ID));
        EmailController ec = generateNewInitializedController(EmailController.class, getBasicUser());
        AntiSpamHelper h = new AntiSpamHelper();
        h.setTimeCheck(h.getTimeCheck() - 5000);
        ec.setResourceId(document.getId());
        ec.setFromId(getAdminUserId());
        ec.setToId(getAdminUserId());
        ec.setH(h);
        ec.setMessageBody("1234");
        ec.prepare();
        ec.validate();
        assertFalse(ec.getActionErrors().isEmpty());
    }

    @Test()
    public void testNoTo() throws Exception {
        setIgnoreActionErrors(true);
        Document document = genericService.find(Document.class, Long.parseLong(TestConstants.TEST_DOCUMENT_ID));
        EmailController ec = generateNewInitializedController(EmailController.class, getBasicUser());
        AntiSpamHelper h = new AntiSpamHelper();
        h.setTimeCheck(h.getTimeCheck() - 5000);
        ec.setResourceId(document.getId());
        ec.setFromId(getBasicUserId());
        ec.setType(EmailMessageType.CONTACT);
        ec.setMessageBody("1234");
        ec.setH(h);
        ec.prepare();
        ec.validate();
        assertFalse(ec.getActionErrors().isEmpty());
    }

    @Test()
    public void testNoResource() throws Exception {
        setIgnoreActionErrors(true);
        EmailController ec = generateNewInitializedController(EmailController.class, getBasicUser());
        AntiSpamHelper h = new AntiSpamHelper();
        h.setTimeCheck(h.getTimeCheck() - 5000);
        ec.setFromId(getBasicUserId());
        ec.setType(EmailMessageType.CONTACT);
        ec.setMessageBody("1234");
        ec.setToId(getAdminUserId());
        ec.setH(h);
        ec.prepare();
        ec.validate();
        assertFalse(ec.getActionErrors().isEmpty());
    }
}
