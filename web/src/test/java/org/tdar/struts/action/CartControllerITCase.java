package org.tdar.struts.action;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.TestBillingAccountHelper;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.BillingTransactionLog;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.NelNetPaymentDao;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.struts.action.api.cart.CartApiController;
import org.tdar.struts.action.api.cart.CartApiPollingAction;
import org.tdar.struts.action.cart.CartBillingAccountController;
import org.tdar.struts.action.cart.CartController;
import org.tdar.struts.action.cart.CartExternalPaymentResponseAction;
import org.tdar.struts.action.cart.InvoiceController;
import org.tdar.struts.action.test.nelnet.MockNelnetController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.Action;


public class CartControllerITCase extends AbstractControllerITCase implements TestBillingControllerHelper, TestBillingAccountHelper {


    @Autowired
    NelNetPaymentDao dao;

    @Autowired
    BillingAccountService accountService;

    @Autowired
    SendEmailProcess sendEmailProcess;

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
        BillingAccount account = createAccount(getBasicUser(), genericService);
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
        BillingAccount account = createAccount(getAdminUser(), genericService);
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
        assertEquals(Permissions.MODIFY_RECORD, user.getGeneralPermission());
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

    @Override
    public BillingAccountService getAccountService() {
        return accountService;
    }

    

    protected CartController setupPaymentTests() throws TdarActionException {
        return setupPaymentTests(null);
    }

    protected CartController setupPaymentTests(String coupon) throws TdarActionException {
//        controller_.setCode(coupon);
        Long invoiceId = setupAndTestBillingAddress(coupon);
        CartController controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        // String response = controller.addPaymentMethod();
        // assertEquals(Action.SUCCESS, response);
        return controller;
    }

    // FIXME: I don't see billing address fields in our forms. do we directly
    // collect address info?, does our payment processor send it to us, or is
    // this
    // feature not used?
    protected Long setupAndTestBillingAddress(String code) throws TdarActionException {
        Address address = new Address(AddressType.BILLING, "street", "Tempe", "arizona", "q234", "united states");
        Address address2 = new Address(AddressType.MAILING, "2street", "notsurewhere", "california", "q234",
                "united states");
        Person user = getUser();
        user.getAddresses().add(address);
        user.getAddresses().add(address2);
        genericService.saveOrUpdate(user);
        evictCache();
        Long invoiceId = createAndTestInvoiceQuantity(10L, code);
        CartController controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        // /////// controller.chooseAddress();

        // set the address of the invoice on the /cart/modify form. You can't
        // actually do this in the app, but let's pretend that you could.
        Invoice invoice = controller.getInvoice();
        assertNull(invoice.getAddress());
        invoice.setAddress(address);
        simulateCartUpdate(invoice);

        // /////// String saveAddress = controller.saveAddress();
        // /////// assertEquals(CartController.SUCCESS_ADD_PAY, saveAddress);
        invoice = genericService.find(Invoice.class, controller.getSessionData().getInvoiceId());
        assertNotNull(invoice);
        assertNotNull(invoice.getAddress());
        return invoiceId;
    }

    protected Long createAndTestInvoiceQuantity(Long numberOfFiles, String code)
            throws TdarActionException {
        InvoiceController controller = generateNewInitializedController(InvoiceController.class);
        logger.debug("setup");
        controller.prepare();
        String result = controller.execute();
        assertEquals(Action.SUCCESS, result);
        logger.debug("done initial");
        controller = generateNewInitializedController(InvoiceController.class);
        controller.prepare();
        logger.debug("set code:{}", code);
        if (StringUtils.isNotBlank(code)) {
            controller.setCode(code);
        }
        controller.setInvoice(new Invoice());
        controller.getInvoice().setNumberOfFiles(numberOfFiles);
        controller.setServletRequest(getServletPostRequest());
        String save = controller.processInvoice();
        logger.debug("coupon:{}", controller.getInvoice().getCoupon());
        logger.debug("done process invoice");

        assertEquals(Action.SUCCESS, save);
        // assertEquals(CartController.SIMPLE, controller.getSaveSuccessPath());
        return controller.getInvoice().getId();
    }

    protected Invoice runSuccessfullTransaction(CartController controller) throws TdarActionException {
        String response;
        Invoice invoice = controller.getInvoice();
        Long invoiceId = invoice.getId();
        invoice.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        simulateCartUpdate(invoice);
        simulateNewSession();

        CartController controller2 = generateNewInitializedController(CartController.class);
        controller2.getSessionData().setInvoiceId(invoiceId);
        controller2.prepare();
        controller2.validate();
        response = controller2.processPaymentRequest();
        assertEquals(CartController.POLLING, response);
        String redirectUrl = controller2.getRedirectUrl();
        invoice = controller2.getInvoice();
        String response2 = processMockResponse(invoice, redirectUrl, true);
        assertEquals(Action.SUCCESS, response2);
        invoice = genericService.find(Invoice.class, invoiceId);
        return invoice;
    }

    /**
     * in the context of a web application, struts actions typically execute in
     * their own hibernate session. if a single test executes multiple actions,
     * it might be necessary to purge/clear the current session to ensure
     * pending db transactions occur and to avoid loads from hibernate cache
     * instead of the db.
     */
    @SuppressWarnings("deprecation")
    protected void simulateNewSession() {
        genericService.synchronize();
        genericService.clearCurrentSession();
    }

    /**
     * Update a persisted (pre-existing) invoice by simulating workflow of a
     * user interacting w/ struts, e.g.
     * <ol>
     * <li>at some point after creating (but not completing) an invoice, user
     * goes back to /cart/add or /cart/modify</li>
     * <li>user modifies some fields and then submits the form to /cart/preview
     * </li>
     * </ol>
     *
     * @param invoice
     *            invoice w/ pending changes. must have ID.
     * @return string result of the /cart/preview action
     */
    String simulateCartUpdate(Invoice invoice) {
        InvoiceController ucc = generateNewInitializedController(InvoiceController.class);
        ucc.getSessionData().setInvoiceId(invoice.getId());
        ucc.prepare();
        ucc.validate();
        ucc.setInvoice(invoice);
        return ucc.processInvoice();
    }

    protected String processMockResponse(Invoice invoice, String redirectUrl, boolean makeInvalid)
            throws TdarActionException {
        CartExternalPaymentResponseAction controller;
        assertNotNull(redirectUrl);
        Map<String, String[]> params = new HashMap<>();
        String qs = redirectUrl.substring(redirectUrl.indexOf("?") + 1);
        qs = StringUtils.replace(qs, "&amp;", "&");
        for (String part : StringUtils.split(qs, "&")) {
            logger.info("part: {} ", part);
            String[] kvp = StringUtils.split(part, "=");
            params.put(kvp[0], new String[] { kvp[1] });
        }

        // removing the decimal place and forcing two decimal points
        assertInMapAndEquals(params, NelnetTransactionItem.AMOUNT.getKey(),
                new DecimalFormat("#.00").format(invoice.getTotal()).replace(".", ""));
        assertInMapAndEquals(params, NelnetTransactionItem.ORDER_TYPE.getKey(), dao.getOrderType());
        assertInMapAndEquals(params, NelnetTransactionItem.ORDER_NUMBER.getKey(), invoice.getId().toString());
        assertInMapAndEquals(params, NelnetTransactionItem.USER_CHOICE_2.getKey(),
                invoice.getOwner().getId().toString());
        assertInMapAndEquals(params, NelnetTransactionItem.USER_CHOICE_3.getKey(), invoice.getId().toString());

        MockNelnetController mock = generateNewController(MockNelnetController.class);
        logger.info("params:{}", params);
        mock.setParameters(params);
        try {
            mock.execute();
        } catch (Exception ignored) {

        }
        logger.info("{}", mock.getResponseParams());
        controller = generateNewInitializedController(CartExternalPaymentResponseAction.class);
        controller.getSessionData().setInvoiceId(invoice.getId());
        controller.setParameters(mock.getResponseParams());
        if (!makeInvalid) {
            // fake tainted connection
            controller.setParameters(mock.getParams());
        }
        int totalLogs = genericService.findAll(BillingTransactionLog.class).size();
        controller.prepare();
        controller.validate();
        String response2 = controller.processExternalPayment();
        if (response2 == TdarActionSupport.SUCCESS) {
            List<BillingTransactionLog> logs = genericService.findAll(BillingTransactionLog.class);
            assertNotEmpty("should have log entries", logs);
            assertEquals(totalLogs + 1, logs.size());
        }
        return response2;
    }


    protected Invoice setupAccountWithCouponForFiles(long numFilesForCoupon, long numberOfFilesForInvoice) throws TdarActionException {
        BillingAccount account = setupAccountWithInvoiceTenOfEach(accountService.getLatestActivityModel(), getAdminUser());
        Invoice invoice_ = account.getInvoices().iterator().next();
        String code = createCouponForAccount(numFilesForCoupon, 0L, account, invoice_,getAdminUser());
        Long invoiceId = createAndTestInvoiceQuantity(numberOfFilesForInvoice, code);
        Invoice invoice = genericService.find(Invoice.class, invoiceId);
        invoice.markFinal();
        return invoice;
    }

    protected void assertPolingResponseCorrect(Long invoiceId, String msg) throws TdarActionException, IOException {
        CartApiPollingAction controller = generateNewInitializedController(CartApiPollingAction.class);
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        String pollingCheck = controller.pollingCheck();
        assertEquals(msg, pollingCheck);
    }

    protected Invoice processTransaction(BillingItem billingItem) throws TdarActionException, IOException {
        CartController controller = setupPaymentTests();
        Invoice invoice = controller.getInvoice();
        Long invoiceId = invoice.getId();
        if (billingItem != null) {
            invoice.getItems().add(billingItem);
        }
        invoice.setBillingPhone(1234567890L);
        assert billingItem != null;
        genericService.saveOrUpdate(billingItem.getActivity());
        assertPolingResponseCorrect(invoice.getId(), TdarActionSupport.SUCCESS);
        // controller.getInvoice().setBillingPhone("123-415-9999");

        invoice.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        genericService.saveOrUpdate(invoice);
        // simulate the "process payment" action, which implicitly sets the invoice total amongst other things
        simulateNewSession();
        controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        controller.validate();
        String response = controller.processPaymentRequest();
        assertEquals(CartController.POLLING, response);
        assertPolingResponseCorrect(invoice.getId(), TdarActionSupport.SUCCESS);
        String redirectUrl = controller.getRedirectUrl();
        // simulateNewSession();
        invoice = genericService.find(Invoice.class, invoice.getId());
        String response2 = processMockResponse(invoice, redirectUrl, true);
        assertEquals(Action.SUCCESS, response2);
        return genericService.find(Invoice.class, invoiceId);
    }
    
    protected void assertInMapAndEquals(Map<String, String[]> params, String key, String val) {
        assertTrue(params.containsKey(key));
        assertEquals(val, params.get(key)[0]);
    }
}
