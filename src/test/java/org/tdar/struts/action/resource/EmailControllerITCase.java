package org.tdar.struts.action.resource;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Document;
import org.tdar.struts.action.EmailController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.AntiSpamHelper;
import org.tdar.utils.EmailMessageType;

public class EmailControllerITCase extends AbstractResourceControllerITCase {

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
            SimpleMailMessage received = checkMailAndGetLatest();
            assertTrue(received.getText().contains("this is a test"));
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
