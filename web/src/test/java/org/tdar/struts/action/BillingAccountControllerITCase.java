package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.TestBillingAccountHelper;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.billing.BillingAccountController;
import org.tdar.struts.action.billing.BillingAccountSelectionAction;
import org.tdar.struts.action.billing.CouponCreationAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.Action;

public class BillingAccountControllerITCase extends AbstractControllerITCase implements TestBillingAccountHelper, TestBillingControllerHelper {

    @Autowired
    BillingAccountService accountService;

    @Test
    @Rollback
    public void testAccountControllerChoicesNoAccount() throws TdarActionException {
        // test fence for Invoice
        BillingAccountSelectionAction controller = generateNewInitializedController(BillingAccountSelectionAction.class);
        controller.prepare();
        String msg = null;
        try {
            controller.validate();
            assertEquals(BillingAccountSelectionAction.NEW_ACCOUNT, controller.selectAccount());
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertEquals(MessageHelper.getMessage("billingAccountController.invoice_is_requried"), msg);
    }

    @Test
    @Rollback
    public void testAccountControllerChoicesOkNewAccount() throws TdarActionException {
        TdarUser user = createAndSaveNewUser();
        Invoice invoice = new Invoice(user, PaymentMethod.INVOICE, 10L, 0L, null);
        genericService.saveOrUpdate(invoice);

        BillingAccountSelectionAction controller = generateNewController(BillingAccountSelectionAction.class);
        init(controller, user);
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        controller.validate();
        assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());

    }

    @Test
    @Rollback
    public void testAccountControllerChoicesNoRightsToAssign() throws TdarActionException {
        BillingAccountSelectionAction controller = generateNewController(BillingAccountSelectionAction.class);
        Invoice invoice = createTrivialInvoice();
        String msg = null;
        init(controller, createAndSaveNewUser());
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        try {
            controller.validate();
            assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertEquals(MessageHelper.getMessage("billingAccountController.rights_to_assign_this_invoice"), msg);
    }

    @Test
    @Rollback
    public void testAccountControllerChoicesSelectAccounts() throws TdarActionException {
        Invoice invoice = createTrivialInvoice();
        invoice.setOwner(getAdminUser());
        BillingAccount account = createAccount(getAdminUser(), genericService);
        BillingAccountSelectionAction controller = generateNewController(BillingAccountSelectionAction.class);
        init(controller, getAdminUser());
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        controller.validate();
        assertEquals(Action.SUCCESS, controller.selectAccount());
        assertTrue(controller.getAccounts().contains(account));

    }

    private Invoice createTrivialInvoice() {
        Invoice invoice = new Invoice(getUser(), PaymentMethod.INVOICE, 10L, 0L, null);
        genericService.save(invoice);
        BillingItem item = new BillingItem(new BillingActivity("test", 1.1F, null, 0L, 10L, 0L, accountService.getLatestActivityModel()), 10);
        genericService.save(item.getActivity());
        invoice.getItems().add(item);
        genericService.saveOrUpdate(item);
        genericService.saveOrUpdate(invoice);
        return invoice;
    }

    @Test
    @Rollback
    public void testAddingInvoiceToExistingAccount() throws TdarActionException {
        Long accountId = createAccount(getUser(), genericService).getId();
        Invoice invoice = createTrivialInvoice();
        genericService.saveOrUpdate(invoice);
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.setInvoiceId(invoice.getId());
        controller.setId(accountId);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertEquals(Action.SUCCESS, save);
        BillingAccount account = genericService.find(BillingAccount.class, accountId);
        assertTrue(account.getInvoices().contains(invoice));
    }
    

    @Test
    @Rollback
    public void testAddingUser() throws TdarActionException {
        Long accountId = createAccount(getBasicUser(), genericService).getId();
        Invoice invoice = createTrivialInvoice();
        TdarUser newUser = createAndSaveNewUser();
        genericService.saveOrUpdate(invoice);
        genericService.synchronize();
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class, getBasicUser());
        controller.setInvoiceId(invoice.getId());
        controller.setId(accountId);
        controller.prepare();
        controller.edit();
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getUser(), newUser, Permissions.USE_ACCOUNT)));
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertEquals(Action.SUCCESS, save);
        BillingAccount account = genericService.find(BillingAccount.class, accountId);
        assertTrue(account.getInvoices().contains(invoice));
        assertEquals(account.getAuthorizedUsers().size(), 2);
    }

    @Test
    @Rollback
    public void testReEvaluationAppropriateWithUncountedThings()
            throws TdarActionException, InstantiationException, IllegalAccessException, FileNotFoundException {
        TdarUser person = createAndSaveNewPerson("aa@bbasdas.com", "suffix");
        BillingAccount account = setupAccountWithInvoiceFiveResourcesAndSpace(accountService.getLatestActivityModel(), person);
        Project project = createAndSaveNewProject("title");
        Document doc = createAndSaveNewInformationResource(Document.class, person);
        addFileToResource(doc, TestConstants.getFile(TestConstants.TEST_DOCUMENT));
        doc.setStatus(Status.DELETED);
        Document doc2 = createAndSaveNewInformationResource(Document.class, person);
        addFileToResource(doc2, TestConstants.getFile(TestConstants.TEST_DOCUMENT));
        account.getResources().add(doc);
        account.getResources().add(doc2);
        account.getResources().add(project);
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class, person);
        controller.setId(account.getId());
        controller.prepare();
        controller.updateQuotas();
        assertEquals(1, account.getFilesUsed().intValue());
        assertEquals(4, account.getAvailableNumberOfFiles().intValue());
        assertEquals(1506924, account.getSpaceUsedInBytes().longValue());
        // controller.setServletRequest(getServletPostRequest());
        // String save = controller.save();
        // assertEquals(BillingAccountController.SUCCESS, save);
        // assertTrue(genericService.find(Account.class, accountId).getInvoices().contains(invoice));
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testAddingInvoiceToNewAccount() throws TdarActionException {
        Invoice invoice = createTrivialInvoice();
        BillingAccount account = createAccount(getUser(), genericService);
        CouponCreationAction controller = setupControllerForCoupon(account, invoice);
        controller.setNumberOfFiles(1L);
        String save = controller.execute();
        Long id = controller.getAccount().getId();
        assertEquals(Action.SUCCESS, save);
        Long accountId = controller.getId();
        // assertFalse(genericService.find(Account.class, accountId).getInvoices().contains(invoice));
        assertTrue(genericService.find(BillingAccount.class, id).getInvoices().contains(invoice));
    }

    @Test
    @Rollback
    public void testAddingUsersToAccount() throws TdarActionException {
        Long id = setupAccountWithUsers();

        BillingAccount account = genericService.find(BillingAccount.class, id);
        assertEquals(3, account.getAuthorizedUsers().size());
        logger.debug("users: {}", account.getAuthorizedUsers());
        Boolean seenAdmin = false;
        for (AuthorizedUser au : account.getAuthorizedUsers()) {
            if (au.getUser().equals(getAdminUser())) {
                seenAdmin = true;
            }
        }
        assertTrue(seenAdmin);
    }

    private Long setupAccountWithUsers() throws TdarActionException {
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class, getAdminUser());
        controller.prepare();
        controller.add();
        controller.setName("my test account");
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getAdminUser(), Permissions.ADMINISTER_ACCOUNT)));
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getBillingUser(), Permissions.ADMINISTER_ACCOUNT)));
        controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.USE_ACCOUNT)));
        controller.setServletRequest(getServletPostRequest());
        // controller.validate();
        String save = controller.save();
        Long id = controller.getAccount().getId();
        assertEquals(Action.SUCCESS, save);
        return id;
    }

    @Test
    @Rollback
    public void testRemovingUsersFromAccount() throws TdarActionException {
        Long id = setupAccountWithUsers();
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.setId(id);
        controller.prepare();
        int size = controller.getAccount().getAuthorizedUsers().size();
        controller.getAccount().getAuthorizedUsers().forEach(au -> {
            controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(),au.getUser(), Permissions.ADMINISTER_ACCOUNT)));
        });
        Iterator<UserRightsProxy> iterator = controller.getProxies().iterator();
        while (iterator.hasNext()) {
            UserRightsProxy proxy = iterator.next();
            if (proxy.getId().equals(getBillingAdminUserId())) {
                iterator.remove();
            }
        }
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertNotEquals(Action.SUCCESS, save);
        assertNotEquals(controller.getActionErrors().size(), 0);
        ignoreActionErrors(true);
    }
    

    @Test
    @Rollback
    public void testRemovingUsersFromAccountValid() throws TdarActionException {
        Long id = setupAccountWithUsers();
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class, getAdminUser());
        controller.setId(id);
        controller.prepare();
        int size = controller.getAccount().getAuthorizedUsers().size()- 1;
        controller.getAccount().getAuthorizedUsers().forEach(au -> {
            controller.getProxies().add(new UserRightsProxy(new AuthorizedUser(getAdminUser(),au.getUser(), Permissions.ADMINISTER_ACCOUNT)));
        });
        Iterator<UserRightsProxy> iterator = controller.getProxies().iterator();
        while (iterator.hasNext()) {
            UserRightsProxy proxy = iterator.next();
            if (proxy.getId().equals(getBillingAdminUserId())) {
                iterator.remove();
            }
        }
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertEquals(Action.SUCCESS, save);
        BillingAccount account = genericService.find(BillingAccount.class, id);
        // no change because of admin user
        assertEquals(size, account.getAuthorizedUsers().size());
        assertFalse(account.getAuthorizedUsers().contains(getBillingUser()));

    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testCreateCouponInvalid() throws TdarActionException {
        setIgnoreActionErrors(true);
        Invoice invoice = createTrivialInvoice();
        BillingAccount account = createAccount(getUser(), genericService);
        CouponCreationAction controller = setupControllerForCoupon(account, invoice);
        controller.setNumberOfFiles(1000L);
        try {
            controller.validate();
            String save = controller.execute();
            Long id = controller.getAccount().getId();
        } catch (TdarRecoverableRuntimeException e) {
            logger.error("{}", e, e);
        }
        logger.debug("messages: {}", controller.getActionErrors());
        logger.debug("looking for {}", MessageHelper.getMessage("accountService.not_enough_space_or_files"));
        assertTrue(controller.getActionErrors()
                .contains(MessageHelper.getMessage("accountService.not_enough_space_or_files")));
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testCreateCouponEmpty() throws TdarActionException {
        setIgnoreActionErrors(true);
        Invoice invoice = createTrivialInvoice();
        BillingAccount account = createAccount(getUser(), genericService);
        CouponCreationAction controller = setupControllerForCoupon(account, invoice);
        // controller.setNumberOfFiles(1000L);
        try {
            controller.validate();
            String save = controller.execute();
            Long id = controller.getAccount().getId();
        } catch (TdarRecoverableRuntimeException e) {
            logger.error("{}", e, e);
        }
        assertTrue(controller.getActionErrors().contains(MessageHelper.getMessage("accountService.specify_either_space_or_files")));
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testCreateCouponInvalidBoth() throws TdarActionException {
        setIgnoreActionErrors(true);
        Invoice invoice = createTrivialInvoice();
        BillingAccount account = createAccount(getUser(), genericService);
        CouponCreationAction controller = setupControllerForCoupon(account, invoice);
        controller.setNumberOfFiles(1L);
        controller.setNumberOfMb(1L);
        try {
            controller.validate();
            String save = controller.execute();
            Long id = controller.getAccount().getId();
        } catch (TdarRecoverableRuntimeException e) {
            logger.error("{}", e, e);
        }
        assertTrue(controller.getActionErrors().contains(MessageHelper.getMessage("accountService.specify_either_space_or_files")));
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testCreateCouponValid() throws TdarActionException {
        Invoice invoice = createTrivialInvoice();
        BillingAccount account = createAccount(getUser(), genericService);
        CouponCreationAction controller = setupControllerForCoupon(account, invoice);
        Long files = controller.getAccount().getAvailableNumberOfFiles();
        controller.setNumberOfFiles(1L);
        boolean seen = false;
        try {
            String save = controller.execute();
            Long id = controller.getAccount().getId();
        } catch (Exception e) {
            seen = true;
            logger.error("error: {}", e);
        }
        assertFalse(seen);
        Set<Coupon> coupons = controller.getAccount().getCoupons();
        assertNotEmpty("should have coupons", coupons);
        Coupon coupon = coupons.iterator().next();
        logger.info(coupon.getCode());
        assertNotNull(coupon.getCode());
        assertEquals(files - 1l, controller.getAccount().getAvailableNumberOfFiles().longValue());
    }

    @Override
    public BillingAccountService getAccountService() {
        return accountService;
    }
}
