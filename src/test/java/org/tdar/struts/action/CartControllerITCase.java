package org.tdar.struts.action;

import com.opensymphony.xwork2.Action;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.*;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.NelNetPaymentDao;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.external.MockMailSender;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.struts.action.cart.*;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

public class CartControllerITCase extends AbstractResourceControllerITCase {

    @Autowired
    NelNetPaymentDao dao;

    @Autowired
    AccountService accountService;

    @Autowired
    private SendEmailProcess sendEmailProcess;

    @Test
    @Rollback
    public void testCartBasicInvalid() throws TdarActionException {
        setIgnoreActionErrors(true);
        UnauthenticatedCartController controller = generateNewInitializedController(UnauthenticatedCartController.class);
        controller.setInvoice(new Invoice());
        controller.prepare();
        String result = controller.execute();
        assertEquals(Action.SUCCESS, result);
        controller = generateNewInitializedController(UnauthenticatedCartController.class);
        controller.setServletRequest(getServletPostRequest());
        String message = getText("invoiceService.specify_something");
        controller.setInvoice(new Invoice());
        controller.prepare();
        controller.validate();
        String save = controller.preview();
        assertEquals(Action.INPUT, save);

        assertTrue(controller.getActionErrors().contains(message));
    }

    @Test
    @Rollback
    public void testCartBasicValid() throws TdarActionException {
        UnauthenticatedCartController controller = generateNewInitializedController(UnauthenticatedCartController.class);
        createAndTestInvoiceQuantity(controller, 10L, null);
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

    private Invoice setupAccountWithCouponForFiles(long numFilesForCoupon, long numberOfFilesForInvoice) throws TdarActionException {
        Account account = setupAccountWithInvoiceTenOfEach(accountService.getLatestActivityModel(), getAdminUser());
        Invoice invoice_ = account.getInvoices().iterator().next();
        String code = createCouponForAccount(numFilesForCoupon, 0L, account, invoice_);
        UnauthenticatedCartController controller = generateNewInitializedController(UnauthenticatedCartController.class);
        Long invoiceId = createAndTestInvoiceQuantity(controller, numberOfFilesForInvoice, code);
        Invoice invoice = genericService.find(Invoice.class, invoiceId);
        invoice.markFinal();
        return invoice;
    }

    @Test
    @Rollback
    public void testCartPrematurePayment() throws TdarActionException {
        //action errors are expected in this test
        setIgnoreActionErrors(true);

        UnauthenticatedCartController controller_ = generateNewInitializedController(UnauthenticatedCartController.class);
        Long invoiceId = createAndTestInvoiceQuantity(controller_, 10L, null);
        CartController controller = generateNewInitializedController(CartController.class);

        //simulate struts workflow: prepare and validate the action but don't execute it
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        controller.validate();

        //at this point we should have validation errors.
        logger.info("action errors from controller:{}", controller.getActionErrors());
        assertThat(controller.getActionErrors(), contains(getText("cartController.valid_payment_method_is_required")));

    }

    @Test
    @Rollback
    public void testCartBasicAddress() throws TdarActionException {
        UnauthenticatedCartController controller = generateNewInitializedController(UnauthenticatedCartController.class);
        setupAndTestBillingAddress(controller);
    }

    @Test
    @Rollback
    public void testCartPaymentInvalid() throws TdarActionException, IOException {
        BillingActivityModel model = new BillingActivityModel();
        genericService.save(model);
        BillingItem billingItem = new BillingItem(new BillingActivity("error", .21F, model), 1);
        Invoice invoice = processTransaction(billingItem);
        assertEquals(TransactionStatus.TRANSACTION_FAILED, invoice.getTransactionStatus());
        String msg = TdarActionSupport.SUCCESS;

        assertPolingResponseCorrect(invoice.getId(), msg);
    }

    private void assertPolingResponseCorrect(Long invoiceId, String msg) throws TdarActionException, IOException {
        CartApiController controller = generateNewInitializedController(CartApiController.class);
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        String pollingCheck = controller.pollingCheck();
        assertEquals(msg, pollingCheck);
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

    private Invoice processTransaction(BillingItem billingItem) throws TdarActionException, IOException {
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
        //controller.getInvoice().setBillingPhone("123-415-9999");
        invoice.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        String response = controller.processPayment();
        assertEquals(CartController.POLLING, response);
        assertPolingResponseCorrect(invoice.getId(), TdarActionSupport.SUCCESS);

        String redirectUrl = controller.getRedirectUrl();
        String response2 = processMockResponse(invoice, redirectUrl, true);
        assertEquals(Action.SUCCESS, response2);
        return genericService.find(Invoice.class, invoiceId);
    }

    @Test
    @Rollback
    public void testCartPaymentValid() throws TdarActionException, IOException {
        CartController controller = setupPaymentTests();
        Invoice invoice = runSuccessfullTransaction(controller);
        assertEquals(TransactionStatus.TRANSACTION_SUCCESSFUL, invoice.getTransactionStatus());
        sendEmailProcess.setEmailService(emailService);
        sendEmailProcess.execute();
        SimpleMailMessage received = ((MockMailSender) emailService.getMailSender()).getMessages().get(0);
        assertTrue(received.getSubject().contains(getText("cartController.subject")));
        assertTrue(received.getText().contains("Transaction Status"));
        assertEquals(received.getFrom(), emailService.getFromEmail());
    }

    private Invoice runSuccessfullTransaction(CartController controller) throws TdarActionException {
        String response;
        Invoice invoice = controller.getInvoice();
        Long invoiceId = invoice.getId();
        invoice.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        simulateCartUpdate(invoice);


        CartController controller2 = generateNewInitializedController(CartController.class);
        controller2.getSessionData().setInvoiceId(invoiceId);
        controller2.prepare();
        controller2.validate();
        controller2.setBillingPhone("1234567890");
        response = controller2.processPayment();
        assertEquals(CartController.POLLING, response);
        String redirectUrl = controller2.getRedirectUrl();
        String response2 = processMockResponse(invoice, redirectUrl, true);
        assertEquals(Action.SUCCESS, response2);
        invoice = genericService.find(Invoice.class, invoiceId);
        return invoice;
    }

    @Test
    @Rollback
    public void testCartPaymentManual() throws TdarActionException, IOException {
        String response;
        CartController controller = setupPaymentTests();
        Invoice invoice = controller.getInvoice();

        //create an invoice,  then make some changes and save the invoice again
        controller.setBillingPhone("(123) 456-7890");
        invoice.setPaymentMethod(PaymentMethod.MANUAL);
        String otherReason = "this is my reasoning";
        invoice.setOtherReason(otherReason);
        simulateCartUpdate(invoice);

        //navigate back to /cart/process-payment
        controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoice.getId());
        controller.prepare();
        controller.validate();
        response = controller.processPayment();

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
        controller.setBillingPhone("123-456-7890");
        String invoiceNumber = "1234567890";
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setPaymentMethod(PaymentMethod.INVOICE);
        invoice.setOtherReason("this is my reasoning");
        Account account = createAccount(getBasicUser());
        CartBillingAccountController billingAccountController = generateNewInitializedController(CartBillingAccountController.class);
        billingAccountController.setId(account.getId());
        billingAccountController.prepare();
        billingAccountController.processBillingAccountChoice();
        controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoice.getId());
        controller.prepare();
        controller.validate();
        response = controller.processPayment();
        assertEquals(CartController.SUCCESS, response);

        Long invoiceId = invoice.getId();

        invoice = genericService.find(Invoice.class, invoiceId);
        assertEquals(TransactionStatus.TRANSACTION_SUCCESSFUL, invoice.getTransactionStatus());
        assertEquals(PaymentMethod.INVOICE, invoice.getPaymentMethod());
        assertEquals(invoiceNumber, invoice.getInvoiceNumber());
    }

    @Test
    @Rollback
    //@Ignore("This test should mimic invalid input, but doesn't appear to provide any invalid data")
    public void testCartPaymentInvalidParams() throws TdarActionException, IOException {
        setIgnoreActionErrors(true);

        //ensure that the cart controllers do not return success messages if you pass it bogus data
        String response;
        CartController controller = setupPaymentTests();

        Invoice invoice = controller.getInvoice();
        Long invoiceId = invoice.getId();
        controller.setBillingPhone("1234567890");

        //modify the invoice
        invoice.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        simulateCartUpdate(invoice);

        //go back to cart/process-payment
        controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoice.getId());
        controller.prepare();
        controller.validate();
        response = controller.processPayment();

        assertEquals(CartController.POLLING, response);
        String redirectUrl = controller.getRedirectUrl();
        String response2 = processMockResponse(invoice, redirectUrl, false);
        assertEquals(Action.ERROR, response2);
        invoice = genericService.find(Invoice.class, invoiceId);
        // can't mark as failed b/c no way to validate package that has invoice ID
        assertEquals(TransactionStatus.PENDING_TRANSACTION, invoice.getTransactionStatus());
    }

    /**
     * Update a persisted (pre-existing) invoice by simulating workflow of a user interacting w/ struts, e.g. <ol>
     *     <li>at some point after creating (but not completing) an invoice,  user goes back to /cart/add or /cart/modify</li>
     *     <li>user modifies some fields and then submits the form to /cart/preview</li>
     * </ol>
     * @param invoice  invoice w/ pending changes. must have ID.
     * @return string result of the /cart/preview action
     */
    String simulateCartUpdate(Invoice invoice) {
        UnauthenticatedCartController ucc = prepareCartNew(invoice.getId());
        ucc.setInvoice(invoice);
        return ucc.preview();
    }

    private UnauthenticatedCartController prepareCartNew(long id) {
        UnauthenticatedCartController ucc = generateNewInitializedController(UnauthenticatedCartController.class);
        ucc.getSessionData().setInvoiceId(id);
        ucc.prepare();
        ucc.validate();
        return ucc;
    }



    private String processMockResponse(Invoice invoice, String redirectUrl, boolean isValid) throws TdarActionException {
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
        assertInMapAndEquals(params, NelnetTransactionItem.AMOUNT.getKey(), new DecimalFormat("#.00").format(invoice.getTotal()).replace(".", ""));
        assertInMapAndEquals(params, NelnetTransactionItem.ORDER_TYPE.getKey(), dao.getOrderType());
        assertInMapAndEquals(params, NelnetTransactionItem.ORDER_NUMBER.getKey(), invoice.getId().toString());
        assertInMapAndEquals(params, NelnetTransactionItem.USER_CHOICE_2.getKey(), invoice.getOwner().getId().toString());
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
        if (!isValid) {
            // fake tainted connection
            controller.setParameters(mock.getParams());
        }
        controller.prepare();
        controller.validate();
        String response2 = controller.processExternalPayment();
        return response2;
    }

    private void assertInMapAndEquals(Map<String, String[]> params, String key, String val) {
        assertTrue(params.containsKey(key));
        assertEquals(val, params.get(key)[0]);
    }

    private CartController setupPaymentTests() throws TdarActionException {
        UnauthenticatedCartController controller_ = generateNewInitializedController(UnauthenticatedCartController.class);
        Long invoiceId = setupAndTestBillingAddress(controller_);
        CartController controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        // String response = controller.addPaymentMethod();
        // assertEquals(Action.SUCCESS, response);
        controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        return controller;
    }

    @Test
    @Rollback
    public void testPaymentPermissions() {
        UnauthenticatedCartController controller = generateNewController(UnauthenticatedCartController.class);
        init(controller, getBasicUser());
        assertTrue(controller.getAllPaymentMethods().size() == 1);
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.CREDIT_CARD));

        controller = generateNewController(UnauthenticatedCartController.class);
        init(controller, getAdminUser());
        assertTrue(controller.getAllPaymentMethods().size() > 1);
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.CREDIT_CARD));
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.MANUAL));
        assertTrue(controller.getAllPaymentMethods().contains(PaymentMethod.INVOICE));

    }

    // FIXME: I don't see billing address fields in our forms. do we directly collect address info?, does our payment processor send it to us, or is this
    // feature not used?
    private Long setupAndTestBillingAddress(UnauthenticatedCartController controller_) throws TdarActionException {
        Address address = new Address(AddressType.BILLING, "street", "Tempe", "arizona", "q234", "united states");
        Address address2 = new Address(AddressType.MAILING, "2street", "notsurewhere", "california", "q234", "united states");
        Person user = getUser();
        user.getAddresses().add(address);
        user.getAddresses().add(address2);
        genericService.saveOrUpdate(user);
        evictCache();
        Long invoiceId = createAndTestInvoiceQuantity(controller_, 10L, null);
        CartController controller = generateNewInitializedController(CartController.class);
        controller.getSessionData().setInvoiceId(invoiceId);
        controller.prepare();
        // /////// controller.chooseAddress();

        //set the address of the invoice on the /cart/modify form.  You can't actually do this in the app, but let's pretend that you could.
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

    private Long createAndTestInvoiceQuantity(UnauthenticatedCartController controller, Long numberOfFiles, String code) throws TdarActionException {
        controller.prepare();
        String result = controller.execute();
        assertEquals(Action.SUCCESS, result);
        controller = generateNewInitializedController(UnauthenticatedCartController.class);
        controller.prepare();
        if (StringUtils.isNotBlank(code)) {
            controller.setCode(code);
        }
        controller.setInvoice(new Invoice());
        controller.getInvoice().setNumberOfFiles(numberOfFiles);
        controller.setServletRequest(getServletPostRequest());
        String save = controller.preview();

        assertEquals(Action.SUCCESS, save);
        // assertEquals(CartController.SIMPLE, controller.getSaveSuccessPath());
        return controller.getInvoice().getId();
    }

}
