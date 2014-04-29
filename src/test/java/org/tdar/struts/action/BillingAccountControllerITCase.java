package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.service.AccountService;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.Action;

public class BillingAccountControllerITCase extends AbstractResourceControllerITCase {

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    @Autowired
    AccountService accountService;

    @Test
    @Rollback
    public void testAccountControllerChoicesNoAccount() throws TdarActionException {
        // test fence for Invoice
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.prepare();
        String msg = null;
        try {
            assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertEquals(MessageHelper.getMessage("billingAccountController.invoice_is_requried"), msg);
    }

    @Test
    @Rollback
    public void testAccountControllerChoicesOkNewAccount() throws TdarActionException {
        TdarUser user = createAndSaveNewPerson();
        Invoice invoice = new Invoice(user, PaymentMethod.INVOICE, 10L, 0L, null);
        genericService.saveOrUpdate(invoice);

        BillingAccountController controller = generateNewController(BillingAccountController.class);
        init(controller, user);
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());

    }

    @Test
    @Rollback
    public void testAccountControllerChoicesNoRightsToAssign() throws TdarActionException {
        BillingAccountController controller = generateNewController(BillingAccountController.class);
        Invoice invoice = createTrivialInvoice();
        String msg = null;
        init(controller, createAndSaveNewPerson());
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        try {
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
        Account account = createAccount(getAdminUser());
        BillingAccountController controller = generateNewController(BillingAccountController.class);
        init(controller, getAdminUser());
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
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
        Long accountId = createAccount(getUser()).getId();
        Invoice invoice = createTrivialInvoice();
        genericService.saveOrUpdate(invoice);
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.setInvoiceId(invoice.getId());
        controller.setId(accountId);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertEquals(Action.SUCCESS, save);
        Account account = genericService.find(Account.class, accountId);
        assertTrue(account.getInvoices().contains(invoice));
    }

    @Test
    @Rollback
    public void testReEvaluationAppropriateWithUncountedThings() throws TdarActionException, InstantiationException, IllegalAccessException {
        TdarUser person = createAndSaveNewPerson();
        Account invoice = setupAccountWithInvoiceFiveResourcesAndSpace(accountService.getLatestActivityModel(), person);
        Project project = createAndSaveNewProject("title");
        Document doc = createAndSaveNewInformationResource(Document.class, person);
        addFileToResource(doc, new File(TestConstants.TEST_DOCUMENT));
        doc.setStatus(Status.DELETED);
        Document doc2 = createAndSaveNewInformationResource(Document.class, person);
        addFileToResource(doc2, new File(TestConstants.TEST_DOCUMENT));
        invoice.getResources().add(doc);
        invoice.getResources().add(doc2);
        invoice.getResources().add(project);
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.setId(invoice.getId());
        controller.prepare();
        controller.updateQuotas();
        assertEquals(1, invoice.getFilesUsed().intValue());
        assertEquals(4, invoice.getAvailableNumberOfFiles().intValue());
        assertEquals(1506924, invoice.getSpaceUsedInBytes().longValue());
        // controller.setServletRequest(getServletPostRequest());
        // String save = controller.save();
        // assertEquals(BillingAccountController.SUCCESS, save);
        // assertTrue(genericService.find(Account.class, accountId).getInvoices().contains(invoice));
    }

    @Test
    @Rollback
    public void testAddingInvoiceToNewAccount() throws TdarActionException {
        Invoice invoice = createTrivialInvoice();
        Account account = createAccount(getUser());
        BillingAccountController controller = setupContrllerForCoupon(account, invoice);
        String save = controller.save();
        Long id = controller.getAccount().getId();
        assertEquals(Action.SUCCESS, save);
        Long accountId = controller.getId();
        // assertFalse(genericService.find(Account.class, accountId).getInvoices().contains(invoice));
        assertTrue(genericService.find(Account.class, id).getInvoices().contains(invoice));
    }

    @Test
    @Rollback
    public void testAddingUsersToAccount() throws TdarActionException {
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.prepare();
        controller.getAuthorizedMembers().add(getAdminUser());
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        Long id = controller.getAccount().getId();
        assertEquals(Action.SUCCESS, save);

        Account account = genericService.find(Account.class, id);
        assertEquals(1, account.getAuthorizedMembers().size());
        assertTrue(account.getAuthorizedMembers().contains(getAdminUser()));
    }

    @Test
    @Rollback
    public void testCreateCouponInvalid() throws TdarActionException {
        setIgnoreActionErrors(true);
        Invoice invoice = createTrivialInvoice();
        Account account = createAccount(getUser());
        BillingAccountController controller = setupContrllerForCoupon(account, invoice);
        controller.setNumberOfFiles(1000L);
        String save = controller.createCouponCode();
        Long id = controller.getAccount().getId();
        logger.debug("messages: {}", controller.getActionErrors());
        assertTrue(controller.getActionErrors().contains(MessageHelper.getMessage("accountService.not_enough_space_or_files")));
    }

    @Test
    @Rollback
    public void testCreateCouponEmpty() throws TdarActionException {
        setIgnoreActionErrors(true);
        Invoice invoice = createTrivialInvoice();
        Account account = createAccount(getUser());
        BillingAccountController controller = setupContrllerForCoupon(account, invoice);
        // controller.setNumberOfFiles(1000L);
        String save = controller.createCouponCode();
        Long id = controller.getAccount().getId();
        assertTrue(controller.getActionErrors().contains(MessageHelper.getMessage("accountService.cannot_generate_a_coupon_for_nothing")));
    }

    @Test
    @Rollback
    public void testCreateCouponInvalidBoth() throws TdarActionException {
        setIgnoreActionErrors(true);
        Invoice invoice = createTrivialInvoice();
        Account account = createAccount(getUser());
        BillingAccountController controller = setupContrllerForCoupon(account, invoice);
        controller.setNumberOfFiles(1L);
        controller.setNumberOfMb(1L);
        String save = controller.createCouponCode();
        Long id = controller.getAccount().getId();
        assertTrue(controller.getActionErrors().contains(MessageHelper.getMessage("accountService.specify_either_space_or_files")));
    }

    @Test
    @Rollback
    public void testCreateCouponValid() {
        Invoice invoice = createTrivialInvoice();
        Account account = createAccount(getUser());
        BillingAccountController controller = setupContrllerForCoupon(account, invoice);
        Long files = controller.getAccount().getAvailableNumberOfFiles();
        controller.setNumberOfFiles(1L);
        boolean seen = false;
        try {
            String save = controller.createCouponCode();
            Long id = controller.getAccount().getId();
        } catch (Exception e) {
            seen = true;
            logger.error("error: {}", e);
        }
        assertFalse(seen);
        Set<Coupon> coupons = controller.getAccount().getCoupons();
        assertNotEmpty(coupons);
        Coupon coupon = coupons.iterator().next();
        logger.info(coupon.getCode());
        assertNotNull(coupon.getCode());
        assertEquals(files - 1l, controller.getAccount().getAvailableNumberOfFiles().longValue());
    }
}
