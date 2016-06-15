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
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.billing.BillingAccountController;
import org.tdar.struts.action.billing.BillingAccountSelectionAction;
import org.tdar.struts.action.billing.CouponCreationAction;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.Action;

public class BillingAccountControllerITCase extends AbstractResourceControllerITCase {

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
            assertEquals(BillingAccountSelectionAction.NEW_ACCOUNT, controller.selectAccount());
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

        BillingAccountSelectionAction controller = generateNewController(BillingAccountSelectionAction.class);
        init(controller, user);
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());

    }

    @Test
    @Rollback
    public void testAccountControllerChoicesNoRightsToAssign() throws TdarActionException {
    	BillingAccountSelectionAction controller = generateNewController(BillingAccountSelectionAction.class);
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
        BillingAccount account = createAccount(getAdminUser());
        BillingAccountSelectionAction controller = generateNewController(BillingAccountSelectionAction.class);
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
        BillingAccount account = genericService.find(BillingAccount.class, accountId);
        assertTrue(account.getInvoices().contains(invoice));
    }

    @Test
    @Rollback
    public void testReEvaluationAppropriateWithUncountedThings() throws TdarActionException, InstantiationException, IllegalAccessException {
        TdarUser person = createAndSaveNewPerson();
        BillingAccount invoice = setupAccountWithInvoiceFiveResourcesAndSpace(accountService.getLatestActivityModel(), person);
        Project project = createAndSaveNewProject("title");
        Document doc = createAndSaveNewInformationResource(Document.class, person);
        addFileToResource(doc, new File(TestConstants.TEST_DOCUMENT));
        doc.setStatus(Status.DELETED);
        Document doc2 = createAndSaveNewInformationResource(Document.class, person);
        addFileToResource(doc2, new File(TestConstants.TEST_DOCUMENT));
        invoice.getResources().add(doc);
        invoice.getResources().add(doc2);
        invoice.getResources().add(project);
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class, person);
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
        BillingAccount account = createAccount(getUser());
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
        assertEquals(3, account.getAuthorizedMembers().size());
        assertTrue(account.getAuthorizedMembers().contains(getAdminUser()));
    }

    private Long setupAccountWithUsers() throws TdarActionException {
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.prepare();
        controller.getAuthorizedMembers().add(getAdminUser());
        controller.getAuthorizedMembers().add(getBillingUser());
        controller.getAuthorizedMembers().add(getEditorUser());
        controller.setServletRequest(getServletPostRequest());
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
        int size = controller.getAccount().getAuthorizedMembers().size();
        controller.getAuthorizedMembers().addAll(controller.getAccount().getAuthorizedMembers());
        controller.getAuthorizedMembers().remove(getBillingUser());
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertEquals(Action.SUCCESS, save);
        BillingAccount account = genericService.find(BillingAccount.class, id);
        assertEquals(size - 1, account.getAuthorizedMembers().size());
        assertFalse(account.getAuthorizedMembers().contains(getBillingUser()));

    }

    @Test
    @Rollback
    public void testCreateCouponInvalid() throws TdarActionException {
        setIgnoreActionErrors(true);
        Invoice invoice = createTrivialInvoice();
        BillingAccount account = createAccount(getUser());
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

    @Test
    @Rollback
    public void testCreateCouponEmpty() throws TdarActionException {
        setIgnoreActionErrors(true);
        Invoice invoice = createTrivialInvoice();
        BillingAccount account = createAccount(getUser());
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

    @Test
    @Rollback
    public void testCreateCouponInvalidBoth() throws TdarActionException {
        setIgnoreActionErrors(true);
        Invoice invoice = createTrivialInvoice();
        BillingAccount account = createAccount(getUser());
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

    @Test
    @Rollback
    public void testCreateCouponValid() throws TdarActionException {
        Invoice invoice = createTrivialInvoice();
        BillingAccount account = createAccount(getUser());
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
        assertNotEmpty(coupons);
        Coupon coupon = coupons.iterator().next();
        logger.info(coupon.getCode());
        assertNotNull(coupon.getCode());
        assertEquals(files - 1l, controller.getAccount().getAvailableNumberOfFiles().longValue());
    }
}
