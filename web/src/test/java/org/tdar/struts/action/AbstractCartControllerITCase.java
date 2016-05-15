package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.BillingTransactionLog;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.NelNetPaymentDao;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.struts.action.cart.CartApiPollingAction;
import org.tdar.struts.action.cart.CartController;
import org.tdar.struts.action.cart.CartExternalPaymentResponseAction;
import org.tdar.struts.action.cart.InvoiceController;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;

import com.opensymphony.xwork2.Action;

public abstract class AbstractCartControllerITCase extends AbstractResourceControllerITCase {

	@Autowired
	NelNetPaymentDao dao;

	@Autowired
	BillingAccountService accountService;

	@Autowired
	SendEmailProcess sendEmailProcess;

	protected void assertInMapAndEquals(Map<String, String[]> params, String key, String val) {
		assertTrue(params.containsKey(key));
		assertEquals(val, params.get(key)[0]);
	}

	protected CartController setupPaymentTests() throws TdarActionException {
		return setupPaymentTests(null);
	}

	protected CartController setupPaymentTests(String coupon) throws TdarActionException {
//		controller_.setCode(coupon);
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
			assertNotEmpty(logs);
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
}
