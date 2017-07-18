package org.tdar.struts.action;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.struts.action.api.cart.CartApiController;
import org.tdar.struts.action.cart.CartBillingAccountController;
import org.tdar.struts.action.cart.CartController;
import org.tdar.struts.action.cart.InvoiceController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.Action;


public class CartControllerITCase extends AbstractCartControllerITCase {

    @Test
    @Rollback
    public void testCartBasicInvalid() throws TdarActionException {
        setIgnoreActionErrors(true);
        InvoiceController controller = generateNewInitializedController(InvoiceController.class);
        controller.setInvoice(new Invoice());
        controller.prepare();
        String result = controller.execute();
        assertEquals(Action.SUCCESS, result);
        controller = generateNewInitializedController(InvoiceController.class);
        controller.setServletRequest(getServletPostRequest());
        String message = getText("invoiceService.specify_something");
        controller.setInvoice(new Invoice());
        controller.prepare();
        controller.validate();
        String save = controller.processInvoice();
        assertEquals(Action.INPUT, save);

        assertTrue(controller.getActionErrors().contains(message));
    }

    @Test
    @Rollback
    public void testCartBasicValid() throws TdarActionException {
        createAndTestInvoiceQuantity(10L, null);
    }

    @Test
    @Rollback
    public void testApi() throws IOException {
        CartApiController cac = generateNewInitializedController(CartApiController.class);
        cac.setLookupFileCount(10L);
        cac.prepare();
        cac.api();
        String result = IOUtils.toString(cac.getResultJson());
        logger.debug(result);
        assertFalse(result.contains("error"));
        assertFalse(result.contains("serializer"));
        logger.debug(result);
    }

    @Test
    @Rollback
    public void testCartCouponExact() throws TdarActionException {
        long numFiles = 10L;
        Invoice invoice = setupAccountWithCouponForFiles(numFiles, numFiles);
        logger.info("{} {} ", invoice.getTotalNumberOfFiles(), invoice.getTotal());
        assertEquals(0.0, invoice.getTotal(), 0);
        assertEquals(numFiles, invoice.getTotalNumberOfFiles().longValue());
    }

    @Test
    @Rollback
    public void testCartCouponSmallerThanAmmount() throws TdarActionException {
        long numFilesInCoupon = 10L;
        Invoice invoice = setupAccountWithCouponForFiles(numFilesInCoupon, 20L);
        logger.info("{} {} ", invoice.getTotalNumberOfFiles(), invoice.getTotal());
        assertEquals(350.0, invoice.getTotal(), 0);
        assertEquals(20L, invoice.getTotalNumberOfFiles().longValue());
    }

    @Test
    @Rollback
    public void testCartCouponNone() throws TdarActionException {
        long numFiles = 10L;
        Invoice invoice = setupAccountWithCouponForFiles(numFiles, 0L);
        logger.info("{} {} ", invoice.getTotalNumberOfFiles(), invoice.getTotal());
        assertEquals(0.0, invoice.getTotal(), 0);
        assertEquals(numFiles, invoice.getTotalNumberOfFiles().longValue());
    }

    @Test
    @Rollback
    public void testCartCouponLargertThanAmmount() throws TdarActionException {
        long numFiles = 5L;
        long numFilesForCoupon = 10L;
        Invoice invoice = setupAccountWithCouponForFiles(numFilesForCoupon, numFiles);
        logger.info("{} {} ", invoice.getTotalNumberOfFiles(), invoice.getTotal());
        assertNotEquals(0.0, invoice.getTotal());
        assertEquals(numFilesForCoupon, invoice.getTotalNumberOfFiles().longValue());
        assertEquals(0.0, invoice.getTotal(), 0);
        assertNotEquals(numFiles, invoice.getTotalNumberOfFiles());
    }

    @Test
    @Rollback
    public void testCartPrematurePayment() throws TdarActionException {
        // action errors are expected in this test
        setIgnoreActionErrors(true);

        Long invoiceId = createAndTestInvoiceQuantity(10L, null);
        CartController controller = generateNewInitializedController(CartController.class);

        // simulate struts workflow: prepare and validate the action but don't execute it
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        controller.validate();

        // at this point we should have validation errors.
        logger.info("action errors from controller:{}", controller.getActionErrors());
        assertThat(controller.getActionErrors(), contains(getText("cartController.valid_payment_method_is_required")));

    }

    @Test
    @Rollback
    public void testCartBasicAddress() throws TdarActionException {
        setupAndTestBillingAddress(null);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testCartPaymentInvalid() throws TdarActionException, IOException {
        BillingActivityModel model = new BillingActivityModel();
        genericService.save(model);
        BillingItem billingItem = new BillingItem(new BillingActivity("error", .21F, model), 1);
        simulateNewSession();
        Invoice invoice = processTransaction(billingItem);
        assertEquals(TransactionStatus.TRANSACTION_FAILED, invoice.getTransactionStatus());
        String msg = TdarActionSupport.SUCCESS;
        // this test fails intermittently unless we do a synchronize. I have no idea why.
        genericService.synchronize();
        assertPolingResponseCorrect(invoice.getId(), msg);
    }

    @Test
    @Rollback
    public void testCartPaymentInvalid2() throws TdarActionException, IOException {
        BillingActivityModel model = new BillingActivityModel();
        genericService.save(model);
        BillingItem billingItem = new BillingItem(new BillingActivity("invalid", .11F, model), 1);
        Invoice invoice = processTransaction(billingItem);
        assertEquals(TransactionStatus.TRANSACTION_FAILED, invoice.getTransactionStatus());
    }

    @Test
    @Rollback
    public void testCartPaymentInvalid3() throws TdarActionException, IOException {
        BillingActivityModel model = new BillingActivityModel();
        genericService.save(model);
        BillingItem billingItem = new BillingItem(new BillingActivity("unknown", .31F, model), 1);
        Invoice invoice = processTransaction(billingItem);
        assertEquals(TransactionStatus.TRANSACTION_FAILED, invoice.getTransactionStatus());
        assertEquals(PaymentMethod.CREDIT_CARD, invoice.getPaymentMethod());
    }

    @Test
    @Rollback
    public void testCartPaymentValid() throws TdarActionException, IOException {
        CartController controller = setupPaymentTests();
        Invoice invoice = runSuccessfullTransaction(controller);
        assertEquals(TransactionStatus.TRANSACTION_SUCCESSFUL, invoice.getTransactionStatus());
        sendEmailProcess.setEmailService(emailService);
        sendEmailProcess.execute();
        SimpleMailMessage received = checkMailAndGetLatest("Transaction Status");

        assertTrue(received.getSubject().contains("Billing Transaction"));
        assertTrue(received.getText().contains("Transaction Status"));
        assertEquals(received.getFrom(), emailService.getFromEmail());
    }


    @Test
    @Rollback
    public void testCartPaymentManual() throws TdarActionException, IOException {
        String response;
        CartController controller = setupPaymentTests();
        Invoice invoice = controller.getInvoice();

        // create an invoice, then make some changes and save the invoice again
        invoice.setPaymentMethod(PaymentMethod.MANUAL);
        String otherReason = "this is my reasoning";
        invoice.setOtherReason(otherReason);
        simulateCartUpdate(invoice);

        // navigate back to /cart/process-payment
        controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoice.getId());
        controller.prepare();
        controller.validate();
        response = controller.processPaymentRequest();

        assertEquals(CartController.SUCCESS, response);
        Long invoiceId = invoice.getId();
        invoice = genericService.find(Invoice.class, invoiceId);
        assertEquals(TransactionStatus.TRANSACTION_SUCCESSFUL, invoice.getTransactionStatus());
        assertEquals(PaymentMethod.MANUAL, invoice.getPaymentMethod());
        assertEquals(otherReason, invoice.getOtherReason());
    }

    @Test
    @Rollback
    public void testCartPaymentInvoice() throws TdarActionException, IOException {
        String response;
        CartController controller = setupPaymentTests();
        Invoice invoice = controller.getInvoice();
        String invoiceNumber = "1234567890";
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setPaymentMethod(PaymentMethod.INVOICE);
        invoice.setOtherReason("this is my reasoning");
        BillingAccount account = TestBillingHelper.createAccount(getBasicUser(), genericService);
        CartBillingAccountController billingAccountController = generateNewInitializedController(CartBillingAccountController.class);
        billingAccountController.setId(account.getId());
        billingAccountController.prepare();
        billingAccountController.processBillingAccountChoice();
        controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoice.getId());
        controller.prepare();
        controller.validate();
        response = controller.processPaymentRequest();
        assertEquals(CartController.SUCCESS, response);

        Long invoiceId = invoice.getId();

        invoice = genericService.find(Invoice.class, invoiceId);
        assertEquals(TransactionStatus.TRANSACTION_SUCCESSFUL, invoice.getTransactionStatus());
        assertEquals(PaymentMethod.INVOICE, invoice.getPaymentMethod());
        assertEquals(invoiceNumber, invoice.getInvoiceNumber());
    }

    
    @Test
    @Rollback
    public void testCartCouponWithRights() throws TdarActionException, IOException, InstantiationException, IllegalAccessException {
        String response;
        Document doc = generateDocumentWithFileAndUser();
        BillingAccount account = TestBillingHelper.createAccount(getAdminUser(), genericService);
        Coupon coupon = new Coupon();
        account.getCoupons().add(coupon);
        coupon.setCode("ABCD");
        coupon.setNumberOfFiles(1L);
        coupon.setDateCreated(new DateTime().minusDays(5).toDate());
//        coupon.setUser(getAdminUser());
        coupon.setDateExpires(new DateTime().plusDays(4).toDate());
        coupon.getResourceIds().add(doc.getId());
        genericService.saveOrUpdate(coupon);
        logger.debug("couponId:{}", coupon.getId());
        genericService.saveOrUpdate(account);
        Long docId = doc.getId();
        doc= null;
        CartController controller = setupPaymentTests(null);
        Invoice invoice = controller.getInvoice();
        invoice.setCoupon(coupon);
        String invoiceNumber = "1234567890";
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setPaymentMethod(PaymentMethod.INVOICE);
        invoice.setTransactionStatus(TransactionStatus.PREPARED);
        invoice.setOtherReason("this is my reasoning");
        genericService.saveOrUpdate(invoice);
        assertTrue(invoice.getCoupon() != null);
        CartBillingAccountController billingAccountController = generateNewInitializedController(CartBillingAccountController.class);
        billingAccountController.setId(account.getId());
        billingAccountController.prepare();
        billingAccountController.processBillingAccountChoice();
        controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoice.getId());
        controller.prepare();
        controller.validate();
        
        response = controller.processPaymentRequest();
        assertEquals(CartController.SUCCESS, response);

        Long invoiceId = invoice.getId();

        invoice = genericService.find(Invoice.class, invoiceId);
        assertEquals(TransactionStatus.TRANSACTION_SUCCESSFUL, invoice.getTransactionStatus());
        assertEquals(PaymentMethod.INVOICE, invoice.getPaymentMethod());
        assertEquals(invoiceNumber, invoice.getInvoiceNumber());
        doc =  genericService.find(Document.class, docId);
        AuthorizedUser user = null;
        for (AuthorizedUser au : doc.getAuthorizedUsers()) {
            if (au.getUser().getId().equals(getUserId())) {
                user = au;
            }
        }
        assertNotNull(user);
        assertEquals(GeneralPermissions.MODIFY_RECORD, user.getGeneralPermission());
    }

    @Test
    @Rollback
    public void testCartPaymentInvalidParams() throws TdarActionException, IOException {
        setIgnoreActionErrors(true);
        // ensure that the cart controllers do not return success messages if you pass it bogus data
        String response;
        CartController controller = setupPaymentTests();

        Invoice invoice = controller.getInvoice();
        Long invoiceId = invoice.getId();

        // modify the invoice
        invoice.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        simulateCartUpdate(invoice);

        simulateNewSession();

        CartBillingAccountController cbac = generateNewInitializedController(CartBillingAccountController.class);
        cbac.getSessionData().setInvoiceId(invoice.getId());
        cbac.prepare();
        cbac.validate();
        response = cbac.processBillingAccountChoice();

        simulateNewSession();
        // go back to cart/process-payment
        controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoice.getId());
        controller.prepare();
        controller.validate();
        response = controller.processPaymentRequest();

        assertEquals(CartController.POLLING, response);
        String redirectUrl = controller.getRedirectUrl();
        invoice = controller.getInvoice();

        String response2 = processMockResponse(invoice, redirectUrl, false);
        assertEquals(Action.ERROR, response2);
        invoice = genericService.find(Invoice.class, invoiceId);
        // can't mark as failed b/c no way to validate package that has invoice ID
        assertEquals(TransactionStatus.PENDING_TRANSACTION, invoice.getTransactionStatus());
    }


    @Test
    @Rollback
    public void testPaymentPermissions() {
        InvoiceController controller = generateNewController(InvoiceController.class);
        init(controller, getBasicUser());
        assertTrue(controller.getAllPaymentMethods().size() == 1);
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.CREDIT_CARD));

        controller = generateNewController(InvoiceController.class);
        init(controller, getAdminUser());
        assertTrue(controller.getAllPaymentMethods().size() > 1);
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.CREDIT_CARD));
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.MANUAL));
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.INVOICE));

    }


}
