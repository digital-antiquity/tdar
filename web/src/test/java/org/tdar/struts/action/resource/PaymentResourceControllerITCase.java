package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.TestBillingAccountHelper;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TestResourceCollectionHelper;
import org.tdar.struts.action.dataset.DatasetController;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.AccountEvaluationHelper;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.Action;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
public class PaymentResourceControllerITCase extends AbstractControllerITCase implements TestBillingAccountHelper, TestResourceCollectionHelper {

    // private

    @Autowired
    private BillingAccountService accountService;

    public DocumentController initControllerFields() throws TdarActionException {
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.setProjectId(TestConstants.PARENT_PROJECT_ID);
        return controller;
    }

    // public void setController(DocumentController controller) {
    // this.controller = controller;
    // }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testSubmitterWithoutAccountRightsAndNoAccount() throws TdarActionException {
        BillingAccount account = setupAccountWithInvoiceFiveResourcesAndSpace(accountService.getLatestActivityModel(), getEditorUser());
        Dataset dataset = createAndSaveNewDataset();
        dataset.setSubmitter(getBasicUser());
        account.getResources().add(dataset);
        genericService.saveOrUpdate(account);
        genericService.saveOrUpdate(dataset);
        Long id = dataset.getId();
        dataset = null;
        genericService.synchronize();
        DatasetController dc = generateNewInitializedController(DatasetController.class, getBasicUser());
        dc.setId(id);
        dc.prepare();
        assertEquals(TdarActionSupport.SUCCESS, dc.edit());
        logger.debug("active accounts:{}", dc.getActiveAccounts());
        logger.debug("accountId:{}", dc.getAccountId());
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testSubmitterWithInheritedRights() throws Exception {
        BillingAccount account = setupAccountWithInvoiceFiveResourcesAndSpace(accountService.getLatestActivityModel(), getEditorUser());
        Dataset dataset = createAndSaveNewDataset();
        account.getResources().add(dataset);
        genericService.saveOrUpdate(account);
        genericService.saveOrUpdate(dataset);
        genericService.synchronize();
        ResourceCollection rCollection = generateResourceCollection("test", "test", true,
                Arrays.asList(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.MODIFY_METADATA)),
                getAdminUser(), Arrays.asList(dataset), null);
        dataset.getManagedResourceCollections().add(rCollection);
        dataset.setSubmitter(getBasicUser());
        genericService.saveOrUpdate(rCollection);

        Long id = dataset.getId();
        dataset = null;
        genericService.synchronize();
        Dataset ds = genericService.find(Dataset.class, id);
        genericService.refresh(ds);
        accountService.updateTransientAccountInfo(ds);
        logger.debug("accnt:{}", ds.getAccount());
        assertNotEmpty("should have results", ds.getManagedResourceCollections());
        ds = null;
        DatasetController dc = generateNewInitializedController(DatasetController.class, getBasicUser());
        dc.setId(id);
        dc.prepare();
        assertEquals(TdarActionSupport.SUCCESS, dc.edit());
        getAuthorizedUserDao().clearUserPermissionsCache();

        logger.debug("active accounts:{}", dc.getActiveAccounts());
        logger.debug("accountId:{}", dc.getAccountId());
        assertEquals(account.getId(), dc.getAccountId());
        dc.setServletRequest(getServletPostRequest());
        dc.save();
        assertFalse(getActionErrors().size() > 0);
        genericService.synchronize();
        ds = genericService.find(Dataset.class, id);
        accountService.updateTransientAccountInfo(ds);
        assertEquals(account, ds.getAccount());

    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testSubmitterWithoutAccountRightsAndSeparateAccount() throws TdarActionException {
        BillingAccount adminAccount = setupAccountWithInvoiceFiveResourcesAndSpace(accountService.getLatestActivityModel(), getEditorUser());
        BillingAccount basicAccount = setupAccountWithInvoiceFiveResourcesAndSpace(accountService.getLatestActivityModel(), getBasicUser());
        Dataset dataset = createAndSaveNewDataset();
        adminAccount.setName("admin account");
        basicAccount.setName("basic account");
        dataset.setSubmitter(getBasicUser());

        // dataset being set to admin account
        adminAccount.getResources().add(dataset);
        genericService.saveOrUpdate(adminAccount);
        genericService.saveOrUpdate(dataset);
        Long id = dataset.getId();
        dataset = null;
        genericService.synchronize();
        DatasetController dc = generateNewInitializedController(DatasetController.class, getBasicUser());
        dc.setId(id);
        dc.prepare();
        assertEquals(TdarActionSupport.SUCCESS, dc.edit());
        logger.debug("active accounts:{}", dc.getActiveAccounts());
        logger.debug("accountId:{}", dc.getAccountId());
        assertTrue(dc.getActiveAccounts().contains(basicAccount));
        assertEquals(adminAccount.getId(), dc.getAccountId());
        // asserting we're still admin account
        dc.setServletRequest(getServletPostRequest());
        dc.save();
        assertFalse(getActionErrors().size() > 0);
        genericService.synchronize();
        Dataset ds = genericService.find(Dataset.class, id);
        accountService.updateTransientAccountInfo(ds);
        assertEquals(adminAccount, ds.getAccount());
    }

    @Test
    @Rollback()
    public void testCreateWithoutValidAccount() throws Exception {
        DocumentController controller = generateNewInitializedController(DocumentController.class, createAndSaveNewUser());
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        Assert.assertTrue(CollectionUtils.isEmpty(controller.getActiveAccounts()));
        initControllerFields();
        Assert.assertTrue(controller.getConfig().isPayPerIngestEnabled());

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
        // run first a general tDAR user -- so you get the CONTRIBUTOR result
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        TdarUser user = createAndSaveNewUser();
        String result = runController(user, true);
        assertTrue(CollectionUtils.isEmpty(accountService.listAvailableAccountsForUser(user)));
        Assert.assertEquals(ResourceController.CONTRIBUTOR, result);
        // mark the user as a contributor, and you should get BILLING WARNING
        user.setContributor(true);
        assertTrue(CollectionUtils.isEmpty(accountService.listAvailableAccountsForUser(user)));
        result = runController(user, true);
        Assert.assertEquals(ResourceController.BILLING, result);
        BillingAccount account = setupAccountWithInvoiceFiveResourcesAndSpace(accountService.getLatestActivityModel(), user);
        genericService.saveOrUpdate(account);
        result = runController(user, false);
        Assert.assertEquals(ResourceController.SUCCESS, result);

    }

    private String runController(TdarUser user, boolean enabled) {
        ResourceController rc;
        rc = generateNewController(ResourceController.class);
        init(rc, user);

        String result = null;
        Exception tdae = null;
        try {
            result = rc.execute();
        } catch (Exception e) {
            tdae = e;
        }
        if (enabled) {
            Assert.assertFalse(rc.isAllowedToCreateResource());
            Assert.assertTrue(rc.getConfig().isPayPerIngestEnabled());
        }
        Assert.assertNull(tdae);
        return result;
    }

    @Test
    @Rollback()
    public void testInitialSaveWithoutValidAccount() throws Exception {
        setIgnoreActionErrors(true);
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        Pair<String, Exception> tdae = setupResource(controller, setupDocument());
        assertEquals(Action.INPUT, tdae.getFirst());
        Long newId = controller.getResource().getId();

        // Assert.assertNull(entityService.findByEmail("new@email.com"));
        // now reload the document and see if the institution was saved.
        // Assert.assertEquals("resource status should be flagged", Status.FLAGGED_ACCOUNT_BALANCE, d.getStatus());
        Assert.assertFalse("resource id should be -1 after unpaid resource addition", newId == Long.valueOf(-1L));
        assertTrue(CollectionUtils.isNotEmpty(controller.getActionErrors()));
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback()
    public void testSecondarySaveWithoutValidAccount() throws Exception {
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        Document d = setupDocument();
        // Account account = createAccount(getBasicUser());
        // d.setAccount(account);
        genericService.saveOrUpdate(d);
        d.getAuthorizedUsers().add(new AuthorizedUser(d.getSubmitter(), d.getSubmitter(), Permissions.MODIFY_RECORD));
        genericService.saveOrUpdate(d);
        logger.info("account: {}", d.getAccount());
        setIgnoreActionErrors(true);
        Pair<String, Exception> tdae = setupResource(controller, d);
        assertTrue(CollectionUtils.isNotEmpty(controller.getActionErrors()));
        logger.info("errors {}", controller.getActionErrors());
        assertTrue(controller.getActionErrors().contains(MessageHelper.getMessage("accountService.account_is_null")));
        Long newId = controller.getResource().getId();

        Assert.assertNotNull(entityService.findByEmail("new@email.com"));
        // now reload the document and see if the institution was saved.

        Assert.assertNotEquals("resource id should be -1 after unpaid resource addition", newId, Long.valueOf(-1L));
        Assert.assertNull("controller should not be successful", null);
        // Assert.assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, d.getStatus());
        Assert.assertFalse(CollectionUtils.isEmpty(controller.getActionErrors()));
    }

    @Test
    @Rollback()
    public void testSecondarySaveWithValidAccount() {
        BillingActivityModel model = new BillingActivityModel();
        model.setCountingResources(false);
        genericService.saveOrUpdate(model);
        BillingAccount account = setupAccountWithInvoiceFiveResourcesAndSpace(model, getUser());
        genericService.saveOrUpdate(account);

        String fmt = "pass %s";
        try {
            for (int i = 1; i < 4; i++) {
                extracted(String.format(fmt, i), account);
            }
        } catch (Exception e) {
            logger.error("Exception happened", e);
            fail(e.getMessage());
        }
    }

    private UsagePair amountRemaining(BillingAccount account) {
        AccountEvaluationHelper helper = new AccountEvaluationHelper(account, accountService.getLatestActivityModel());
        UsagePair pair = new UsagePair(helper.getAvailableNumberOfFiles(), helper.getAvailableSpaceInBytes());
        return pair;
    }

    private void extracted(String title, BillingAccount expectedAccount) throws Exception {
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        Document d = setupDocument();
        d.setStatus(Status.DRAFT);
        controller.setDocument(d);
        controller.setAccountId(expectedAccount.getId());
        controller.setServletRequest(getServletPostRequest());
        UsagePair statsBefore = amountRemaining(expectedAccount);
        assertEquals("errors: " + StringUtils.join(controller.getActionErrors(), ", "), Action.SUCCESS, controller.save());

        UsagePair statsAfter = amountRemaining(expectedAccount);
        assertEquals("files remainning should be the same because resource has no files", statsBefore, statsAfter);
        Long id = d.getId();
        BillingAccount account = accountService.find(controller.getAccountId());
        assertEquals(expectedAccount, account);

        d = null;
        controller = generateNewInitializedController(DocumentController.class);
        controller.setId(id);
        controller.prepare();
        controller.edit();
        File file = TestConstants.getFile(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME);
        Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync = uploadFilesAsync(Arrays.asList(file));
        controller.setTicketId(uploadFilesAsync.getFirst().getId());
        controller.setFileProxies(uploadFilesAsync.getSecond());
        long fileSize = file.length();
        controller.setServletRequest(getServletPostRequest());
        assertEquals("errors: " + StringUtils.join(controller.getActionErrors(), ", "), Action.SUCCESS, controller.save());
        statsAfter = amountRemaining(expectedAccount);
        assertEquals(title + ":files remaining should decrement by 1", statsBefore.files() - 1, statsAfter.files());
        assertEquals(title + ":space remaining should decrement by " + fileSize, statsBefore.bytes() - fileSize, statsAfter.bytes());
        assertEquals(title + ": resource should be in draft", Status.DRAFT, controller.getResource().getStatus());
    }

    private Pair<String, Exception> setupResource(DocumentController controller, Document d) throws TdarActionException {
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        if ((d != null) && (d.getId() != null)) {
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

    public ResourceCreatorProxy getNewResourceCreator(String last, String first, String email, Long id, ResourceCreatorRole role) {
        ResourceCreatorProxy rcp = new ResourceCreatorProxy();
        Person p = rcp.getPerson();
        rcp.getPerson().setLastName(last);
        rcp.getPerson().setFirstName(first);
        rcp.getPerson().setEmail(email);
        // id may be null
        rcp.getPerson().setId(id);
        Institution inst = new Institution();
        inst.setName("University of TEST");
        p.setInstitution(inst);
        rcp.setRole(role);
        return rcp;
    }

}
