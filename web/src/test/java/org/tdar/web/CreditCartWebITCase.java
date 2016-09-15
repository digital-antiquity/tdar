package org.tdar.web;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.URLConstants;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.Pair;
import org.tdar.utils.SimpleHttpUtils;
import org.tdar.utils.TestConfiguration;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

@RunWith(MultipleWebTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
// @ContextConfiguration(classes = TdarAppConfiguration.class)
public class CreditCartWebITCase extends AbstractWebTestCase {

	private static final String NEXT_STEP_PAYMENT = "Next Step: Payment";
	private static final String CART_PROCESS_PAYMENT_REQUEST = "/cart/process-payment-request";
	private static final String CART_REVIEW2 = "/cart/review";
	private static final TestConfiguration CFG = TestConfiguration.getInstance();
	// @Autowired
	// private InvoiceService invoiceService;

	public Long getItemId(String name) {
		switch (name) {
		// HARD CODED to ta database
		case "error":
			return 1L;
		case "unknown":
			return 3L;
		case "decline":
			return 2L;
		}
		return -1L;
	}

	@Test
	public void testCartIncomplete() throws MalformedURLException {
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "0");
		setInput("invoice.numberOfFiles", "0");
		submitFormWithoutErrorCheck();
		assertCurrentUrlContains("process-choice");
		assertTextPresentInCode("Pricing");
		assertTextPresentInCode(MessageHelper.getInstance().getText("cartController.specify_mb_or_files"));
	}

	@Test
	public void testCartFilesNoMB() throws MalformedURLException {
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "0");
		setInput("invoice.numberOfFiles", "100");
		submitForm();
		assertTextPresent("50-500:100:$31:$3,100");
		assertTextPresent("total:$3,100");
		loginAndSpecifyCC();
		selectAnyAccount();
		testAccountPollingResponse("310000", TransactionStatus.TRANSACTION_SUCCESSFUL);

	}

	private void loginAndSpecifyCC() {
		if (getPageText().contains("Log In")) {
			clickLinkOnPage("Log In");
			logger.debug(getCurrentUrlPath());
			completeLoginForm(CFG.getUsername(), CFG.getPassword(), false);
			gotoPage(CART_REVIEW2);
		}
	}

	@Test
	public void testCartMBNoFiles() throws MalformedURLException {
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "100");
		setInput("invoice.numberOfFiles", "0");
		submitForm();
		assertTextPresent("100 mb:1:$50:$50");
		assertTextPresent("total:$50");
		loginAndSpecifyCC();
		selectAnyAccount();
		testAccountPollingResponse("5000", TransactionStatus.TRANSACTION_SUCCESSFUL);
	}

	@Test
	public void testCartSuccess() throws MalformedURLException {
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "2000");
		setInput("invoice.numberOfFiles", "10");
		submitForm();

		assertTextPresent("100 mb:19:$50:$950");
		assertTextPresent("5- 19:10:$40:$400");
		assertTextPresent("total:$1,350");
		loginAndSpecifyCC();
		selectAnyAccount();
		testAccountPollingResponse("135000", TransactionStatus.TRANSACTION_SUCCESSFUL);
	}

	@Test
	public void testCartWithAccount() throws MalformedURLException {
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "2000");
		setInput("invoice.numberOfFiles", "10");
		submitForm();
		loginAndSpecifyCC();
		selectAnyAccount();
		String invoiceId = testAccountPollingResponse("135000", TransactionStatus.TRANSACTION_SUCCESSFUL)
				.get(INVOICE_ID);
		String accountId = addInvoiceToNewAccount(invoiceId, null, null);
		assertTrue(accountId != "-1");
	}

	/**
	 * Create two invoices and assign them to the same billing account. Verify
	 * that the billing account page contains line-items associated with the
	 * invoices that we create in this test.
	 * 
	 * @throws MalformedURLException
	 */
	@SuppressWarnings("unused")
    @Test
	public void testAddCartToSameAccount() throws MalformedURLException {
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "2000");
		setInput("invoice.numberOfFiles", "10");
		submitForm();
		assertCurrentUrlContains(CART_REVIEW2);
		loginAndSpecifyCC();

		// expecting to be on the choose billing account page
		assertCurrentUrlContains(CART_REVIEW2);
		// submitForm("Next Step: Payment");
		// assertCurrentUrlContains("/cart/choose-billing-account");
		selectAnyAccount();
		submitForm(NEXT_STEP_PAYMENT);

		// remember the account we chose/created; we will assign our next
		// invoice to this account
		String accountId = testAccountPollingResponse("135000", TransactionStatus.TRANSACTION_SUCCESSFUL)
				.get(ACCOUNT_ID);
		assertTrue(accountId != "-1");
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "10000");
		setInput("invoice.numberOfFiles", "12");
		submitForm();

		// we should be on the 'review' page, just click through to the billing
		// account page
		assertCurrentUrlContains(CART_REVIEW2);

		// we should now be on the "choose billing account" page. Specify the
		// same account that we used for the previous invoice.
		setInput("id", accountId);
		submitForm(NEXT_STEP_PAYMENT);

		// now we should be on the process-payment page... i think?
		assertCurrentUrlContains(CART_PROCESS_PAYMENT_REQUEST);
		String invoiceId2 = testAccountPollingResponse("543000", TransactionStatus.TRANSACTION_SUCCESSFUL)
				.get(INVOICE_ID);
		gotoPage("/billing/" + accountId);

		assertTextPresent("10,020");
		assertTextPresent("2,000");
		assertTextPresent("10");
		assertTextPresent("12");
		assertTextPresent("$5,430");
		assertTextPresent("$1,350");
		logger.trace(getPageText());

	}

	@Test
	public void testAddPaymentsToMultipleAccount() throws MalformedURLException {
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "2000");
		setInput("invoice.numberOfFiles", "10");
		submitForm();
		loginAndSpecifyCC();
		selectAnyAccount();
		submitForm(NEXT_STEP_PAYMENT);
		// assertCurrentUrlContains("/cart/choose-billing-account");
		String invoiceId = testAccountPollingResponse("135000", TransactionStatus.TRANSACTION_SUCCESSFUL)
				.get(INVOICE_ID);
		String accountName = "test account 1";
		String accountId = addInvoiceToNewAccount(invoiceId, null, accountName);
		assertTrue(accountId != "-1");
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "10000");
		setInput("invoice.numberOfFiles", "12");
		submitForm();
		assertCurrentUrlContains(CART_REVIEW2);
		// clickLinkWithText("Next Step: Choose Billing Account");
		setInput("id", accountId);
		submitForm(NEXT_STEP_PAYMENT);
		String invoiceId2 = testAccountPollingResponse("543000", TransactionStatus.TRANSACTION_SUCCESSFUL)
				.get(INVOICE_ID);
		String accountName2 = "test account 2";
		String account = addInvoiceToNewAccount(invoiceId2, null, accountName2);
		assertTextPresent(accountName2);
		assertTextNotPresent(accountName);
		assertNotEquals(account, accountId);
		gotoPage("/billing");
		assertTextPresent(accountName);
		assertTextPresent(accountName2);
		logger.trace(getPageText());

	}

	@Test
	public void testCartError() throws MalformedURLException {
		login(CFG.getAdminUsername(), CFG.getAdminPassword());
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "2000");
		setInput("invoice.numberOfFiles", "10");
		setExtraItem("error", "1");

		submitForm();
		assertTextPresent("100 mb:19:$50:$950");
		assertTextPresent("5- 19:10:$40:$400");
		assertTextPresent("total:$1,405.21");
		selectAnyAccount();
		testAccountPollingResponse("140521", TransactionStatus.TRANSACTION_FAILED);
	}

	@Test
	public void testCartUnknown() throws MalformedURLException {
		login(CFG.getAdminUsername(), CFG.getAdminPassword());
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "2000");
		setInput("invoice.numberOfFiles", "10");
		setExtraItem("unknown", "1");

		submitForm();

		assertTextPresent("100 mb:19:$50:$950");
		assertTextPresent("5- 19:10:$40:$400");
		assertTextPresent("total:$1,405.31");
		selectAnyAccount();
		testAccountPollingResponse("140531", TransactionStatus.TRANSACTION_FAILED);
	}

	private void setExtraItem(String name, String val) {
		for (int i = 0; i < 100; i++) {
			try {
				HtmlElement input = getInput(String.format("extraItemIds[%s]", i));
				if (input != null) {
					String string = getItemId(name).toString();
					logger.info(" {}|{} ", input.getAttribute("value"), string);
					if (input.getAttribute("value").equals(string)) {
						setInput(String.format("extraItemQuantities[%s]", i), val);
						logger.info("setting value {} {}", input.toString(), i);
					}
				}
			} catch (NoSuchElementException e) {
				logger.warn("{}", e.getMessage());
			} catch (Exception e) {
				logger.warn("exception:", e);
			}
		}
	}

	@Test
	public void testCartDecline() throws MalformedURLException {
		login(CFG.getAdminUsername(), CFG.getAdminPassword());
		gotoPage(URLConstants.CART_ADD);
		setInput("invoice.numberOfMb", "2000");
		setInput("invoice.numberOfFiles", "10");

		setExtraItem("decline", "1");

		submitForm();

		assertTextPresent("100 mb:19:$50:$950");
		assertTextPresent("5- 19:10:$40:$400");
		assertTextPresent("total:$1,405.11");
		testAccountPollingResponse("140511", TransactionStatus.TRANSACTION_FAILED);
	}

	/**
	 * give the nelnet event notification endpoint totally bogus data. We should
	 * get back non-200 status code and "failure" as the response body.
	 */
	@Test
	public void testCompletelyBogusEndpointRequest() {
		String url = String.format("https://%s:%s/cart/process-external-payment-response",
				TestConfiguration.getInstance().getHostName(), TestConfiguration.getInstance().getHttpsPort());
		Pair<Integer, String> responsePair = SimpleHttpUtils.parseResponse(SimpleHttpUtils.post(url,
				asList(SimpleHttpUtils.nameValuePair("foo", "bar"), SimpleHttpUtils.nameValuePair("ping", "pong"))));
		assertThat(responsePair.getFirst(), is(not(200)));
		assertThat(responsePair.getSecond(), is("failure"));
	}
}
