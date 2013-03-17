package org.tdar.struts.action.resource;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.AccountService;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.AccountEvaluationHelper;
import org.tdar.utils.Pair;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { "src/test/resources/tdar.cc.properties" })
public class PaymentResourceControllerITCase extends AbstractResourceControllerITCase {

    private DocumentController controller;

    @Override
    protected TdarActionSupport getController() {
        return controller;
    }

    public void initControllerFields() {
        controller.prepare();
        controller.setProjectId(TestConstants.PARENT_PROJECT_ID);
    }

    public void setController(DocumentController controller) {
        this.controller = controller;
    }
    
    private class UsagePair extends Pair<Long, Long> {
        public UsagePair(Long first, Long second) {
            super(first, second);
        }
        
        public long files() {
            return getFirst();
        }
        
        public long bytes() {
            return getSecond();
        }
    }

    @Test
    @Rollback()
    public void testCreateWithoutValidAccount() throws Exception {
        controller = generateNewInitializedController(DocumentController.class);
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        Assert.assertTrue(CollectionUtils.isEmpty(controller.getActiveAccounts()));
        initControllerFields();
        Assert.assertTrue(controller.isPayPerIngestEnabled());

        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        String result = null;
        TdarActionException tdae = null;
        try {
            result = controller.edit();
        } catch (TdarActionException e) {
            tdae = e;
        }
        Assert.assertNotNull(tdae);
        Assert.assertNull(result, result);
    }

    @Test
    @Rollback()
    public void testResourceControllerWithoutValidAccount() throws Exception {
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        ResourceController rc = generateNewController(ResourceController.class);
        Person user = createAndSaveNewPerson();
        init(rc, user);

        assertTrue(CollectionUtils.isEmpty(accountService.listAvailableAccountsForUser(user)));
        String result = null;
        TdarActionException tdae = null;
        try {
            result = rc.doDefault();
        } catch (TdarActionException e) {
            tdae = e;
        }
        Assert.assertFalse(rc.isAllowedToCreateResource());
        Assert.assertTrue(rc.isPayPerIngestEnabled());
        Assert.assertNull(tdae);
        Assert.assertEquals(ResourceController.SUCCESS, result);
    }

    @Test
    @Rollback()
    public void testInitialSaveWithoutValidAccount() throws Exception {
        controller = generateNewInitializedController(DocumentController.class);
        Pair<String, Exception> tdae = setupResource(setupDocument());
        assertEquals(DocumentController.INPUT, tdae.getFirst());
        Long newId = controller.getResource().getId();

        // Assert.assertNull(entityService.findByEmail("new@email.com"));
        // now reload the document and see if the institution was saved.
        // Assert.assertEquals("resource status should be flagged", Status.FLAGGED_ACCOUNT_BALANCE, d.getStatus());
        Assert.assertFalse("resource id should be -1 after unpaid resource addition", newId == Long.valueOf(-1L));
        setIgnoreActionErrors(true);
        assertTrue(CollectionUtils.isNotEmpty(controller.getActionErrors()));
    }

    @Test
    @Rollback()
    public void testSecondarySaveWithoutValidAccount() throws Exception {
        controller = generateNewInitializedController(DocumentController.class);
        Document d = setupDocument();
        // Account account = createAccount(getBasicUser());
        // d.setAccount(account);
        genericService.saveOrUpdate(d);

        logger.info("account: {}", d.getAccount());
        Pair<String, Exception> tdae = setupResource(d);
        assertTrue(CollectionUtils.isNotEmpty(getController().getActionErrors()));
        logger.info("errors {}", getController().getActionErrors());
        assertTrue(getController().getActionErrors().contains(AccountService.ACCOUNT_IS_NULL));
        Long newId = controller.getResource().getId();

        Assert.assertNotNull(entityService.findByEmail("new@email.com"));
        // now reload the document and see if the institution was saved.

        Assert.assertNotEquals("resource id should be -1 after unpaid resource addition", newId, Long.valueOf(-1L));
        Assert.assertNull("controller should not be successful", null);
        // Assert.assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, d.getStatus());
        Assert.assertFalse(CollectionUtils.isEmpty(controller.getActionErrors()));
        setIgnoreActionErrors(true);
    }

    @Test
    @Rollback()
    public void testSecondarySaveWithValidAccount() {
        BillingActivityModel model = new BillingActivityModel();
        model.setCountingResources(false);
        genericService.saveOrUpdate(model);
        Account account = setupAccountWithInvoiceFiveResourcesAndSpace(model);
        genericService.saveOrUpdate(account);

        String fmt = "pass %s";
        try {
        for (int i=1; i < 4; i++) {
        extracted(String.format(fmt, i), account);
        }
        } catch (Exception e) {
            logger.error("Exception happened", e);
            fail(e.getMessage());
        }
    }


    private UsagePair amountRemaining(Account account) {
        AccountEvaluationHelper helper = new AccountEvaluationHelper(account, accountService.getLatestActivityModel());
        UsagePair pair = new UsagePair(helper.getAvailableNumberOfFiles(), helper.getAvailableSpaceInBytes());
        return pair;
    }
    
    
    

    private void extracted(String title, Account expectedAccount) throws TdarActionException, FileNotFoundException {
        controller = generateNewInitializedController(DocumentController.class);
        Document d = setupDocument();
        d.setStatus(Status.DRAFT);
        controller.setDocument(d);
        controller.setAccountId(expectedAccount.getId());
        controller.setServletRequest(getServletPostRequest());
        UsagePair statsBefore = amountRemaining(expectedAccount);
        assertEquals("errors: " + StringUtils.join(controller.getActionErrors(),", "), TdarActionSupport.SUCCESS, controller.save());
        
        UsagePair statsAfter = amountRemaining(expectedAccount);
        assertEquals("files remainning should be the same because resource has no files", statsBefore, statsAfter);
        Long id = d.getId();
        Account account = accountService.find(controller.getAccountId());
        assertEquals(expectedAccount, account);

        d = null;
        controller = generateNewInitializedController(DocumentController.class);
        controller.setId(id);
        controller.prepare();
        controller.edit();
        File file = new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME);
        Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync = uploadFilesAsync(Arrays.asList(file));
        controller.setTicketId(uploadFilesAsync.getFirst().getId());
        controller.setFileProxies(uploadFilesAsync.getSecond());
        long fileSize = file.length();
        controller.setServletRequest(getServletPostRequest());
        assertEquals("errors: " + StringUtils.join(controller.getActionErrors(),", "), TdarActionSupport.SUCCESS, controller.save());
        statsAfter = amountRemaining(expectedAccount);
        assertEquals(title + ":files remaining should decrement by 1", statsBefore.files() - 1, statsAfter.files());
        assertEquals(title + ":space remaining should decrement by " + fileSize, statsBefore.bytes() - fileSize, statsAfter.bytes());
        assertEquals(title + ": resource should be in draft", Status.DRAFT, controller.getResource().getStatus());
    }

    private Pair<String, Exception> setupResource(Document d) {
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        if (d != null && d.getId() != null) {
            controller.setId(d.getId());
        }
        initControllerFields();
        controller.setDocument(d);
        if (d.getAccount() != null) {
            controller.setAccountId(d.getAccount().getId());
        }
        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        controller.getAuthorshipProxies().add(getNewResourceCreator("newLast", "newFirst", "new@email.com", null, ResourceCreatorRole.AUTHOR));

        controller.setServletRequest(getServletPostRequest());
        String result = null;
        Exception tdae = null;
        try {
            result = controller.save();
        } catch (Exception e) {
            tdae = e;
        }
        return new Pair<String, Exception>(result, tdae);
    }

    private Document setupDocument() {
        Document d = new Document();
        d.setId(-1L);
        d.setTitle("doc title");
        d.setDescription("desc");
        d.markUpdated(getUser());
        return d;
    }

}
