package org.tdar.core.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingAccountGroup;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.AccountAdditionStatus;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.billing.InvoiceService;

public class AccountServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    BillingAccountService accountService;

    @Autowired
    InvoiceService invoiceService;

    @Test
    @Rollback
    public void testAccountList() {
        TdarUser p = createAndSaveNewPerson();
        BillingAccount account = setupAccountForPerson(p);
        BillingAccount accountWithPermissions = new BillingAccount("my account");
        TdarUser p2 = createAndSaveNewPerson("a@aas", "bb");
        accountWithPermissions.setOwner(p2);
        accountWithPermissions.markUpdated(getUser());
        accountWithPermissions.setStatus(Status.ACTIVE);
        accountWithPermissions.getAuthorizedMembers().add(p);
        genericService.saveOrUpdate(accountWithPermissions);

        List<BillingAccount> accountsForUser = accountService.listAvailableAccountsForUser(p);
        assertTrue(accountsForUser.contains(account));
        assertTrue(accountsForUser.contains(accountWithPermissions));

        accountsForUser = accountService.listAvailableAccountsForUser(getUser());
        assertFalse(accountsForUser.contains(account));
        assertFalse(accountsForUser.contains(accountWithPermissions));
    }

    @Test
    @Rollback
    public void testAccountGroups() {
        BillingAccountGroup group = new BillingAccountGroup();
        group.setName("my account group");
        group.markUpdated(getBasicUser());
        BillingAccount accountForPerson = setupAccountForPerson(getBasicUser());
        BillingAccount accountForPerson2 = setupAccountForPerson(createAndSaveNewPerson());
        accountForPerson2.getAuthorizedMembers().add(getBasicUser());
        group.getAccounts().add(accountForPerson);
        group.getAccounts().add(accountForPerson2);
        genericService.saveOrUpdate(group);
        assertEquals(accountService.getAccountGroup(accountForPerson), group);
    }

    @Test
    @Rollback(false)
    public void updateOverdrawnAccountTest() throws InstantiationException, IllegalAccessException {
        BillingAccount account = setupAccountForPerson(getBasicUser());
        BillingActivityModel model = new BillingActivityModel();
        model.setCountingResources(false);
        model.setCountingFiles(true);
        model.setCountingSpace(false);
        model.setActive(true);
        model.setVersion(100);
        genericService.saveOrUpdate(model);
        final Long modelId = model.getId();
        Document resource = generateDocumentWithFileAndUseDefaultUser();
        resource.setAccount(account);
        final long rid = resource.getId();
        final long accountId = account.getId();
        genericService.saveOrUpdate(resource);
        AccountAdditionStatus updateQuota = accountService.updateQuota(account, account.getOwner(), resource);
        assertEquals(AccountAdditionStatus.NOT_ENOUGH_SPACE, updateQuota);

        Invoice invoice = new Invoice();
        invoice.markUpdated(getBasicUser());
        invoice.getItems().add(new BillingItem(new BillingActivity("6 mb", 10f, 0, 0L, 1L, 10L, model), 1));
        BillingActivity activity = invoice.getItems().get(0).getActivity();
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.resetTransientValues();
        invoice.markFinal();
        activity.setModel(model);
        account.getInvoices().add(invoice);
        genericService.saveOrUpdate(model);
        genericService.saveOrUpdate(activity);
        genericService.saveOrUpdate(invoice);
        genericService.saveOrUpdate(account);
        logger.info("updating quotas");
        resource = null;
        invoice = null;
        setVerifyTransactionCallback(new TransactionCallback<Document>() {
            @Override
            public Document doInTransaction(org.springframework.transaction.TransactionStatus status) {
                BillingAccount account = genericService.find(BillingAccount.class, accountId);
                Document resource2 = genericService.find(Document.class, rid);
                genericService.update(resource2);
                assertFalse(resource2.isUpdated());
                assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, resource2.getStatus());
                accountService.updateQuota(account, account.getResources(), account.getOwner());
                Invoice invoice_ = account.getInvoices().iterator().next();
                assertEquals(Status.ACTIVE, resource2.getStatus());
                genericService.delete(resource2);
                genericService.delete(account);
                genericService.delete(invoice_.getItems());
                BillingActivityModel model_ = genericService.find(BillingActivityModel.class, modelId);
                genericService.delete(model_.getActivities());
                genericService.delete(model_);
                return null;
            }

        });
    }

    @Test
    @Rollback
    public void testUpdatedIsInitializedProperly() {
        Document doc = genericService.findRandom(Document.class, 1).get(0);
        logger.info("{}", doc.isUpdated());
        assertFalse(doc.isUpdated());
    }

    @Test
    @Rollback
    public void testAccountGroupPermissions() {
        BillingAccountGroup group = new BillingAccountGroup();
        group.setName("my account group");
        group.markUpdated(getBasicUser());
        BillingAccount accountForPerson = setupAccountForPerson(getBasicUser());
        BillingAccount accountForPerson2 = setupAccountForPerson(getBasicUser());
        accountForPerson2.getAuthorizedMembers().add(getBasicUser());
        TdarUser person = createAndSaveNewPerson();
        group.getAuthorizedMembers().add(person);
        group.getAccounts().add(accountForPerson);
        group.getAccounts().add(accountForPerson2);
        genericService.saveOrUpdate(group);
        assertEquals(accountService.getAccountGroup(accountForPerson), group);
        assertTrue(accountService.listAvailableAccountsForUser(person).contains(accountForPerson));
        assertTrue(accountService.listAvailableAccountsForUser(person).contains(accountForPerson2));
    }

    
    @Test
    @Rollback
    public void testAccountTransfer() {
        BillingAccount to = setupAccountForPerson(getBasicUser());
        BillingAccount from = setupAccountWithInvoiceFiveResourcesAndSpace(accountService.getLatestActivityModel(), getBasicUser());
        accountService.transferBalanace(getAdminUser(), from, to, null);
        logger.debug("to: {}", to.availableString());
        logger.debug("from: {}", from.availableString());
        assertEquals(5, to.getAvailableNumberOfFiles().intValue());
        assertEquals(0, from.getAvailableNumberOfFiles().intValue());
        
    }
    
    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testAvaliableActivities() {
        BillingActivityModel model = new BillingActivityModel();
        model.setActive(true);
        BillingActivity disabledDctivity = new BillingActivity();
        disabledDctivity.setActive(false);
        disabledDctivity.setName("not active");
        genericService.saveOrUpdate(model);
        disabledDctivity.setModel(model);
        genericService.saveOrUpdate(disabledDctivity);
        model.setVersion(105);
        BillingActivity ctivity = new BillingActivity("test", 1f, model);
        ctivity.setActive(true);
        ctivity.setName("active");
        genericService.saveOrUpdate(ctivity);
        model.getActivities().add(disabledDctivity);
        model.getActivities().add(ctivity);
        genericService.saveOrUpdate(model);
        genericService.synchronize();

        List<BillingActivity> activeBillingActivities = invoiceService.getActiveBillingActivities();
        assertTrue(activeBillingActivities.contains(ctivity));
        assertFalse(activeBillingActivities.contains(disabledDctivity));
    }

    @Test
    @Rollback
    /**
     * Simulate a situation where someone invoices this method by way of repeating parts of the invoice purchase workflow without
     * completing the purchase (i.e. finalizing/paying for the invoice)
     */
    public void testRepeatedBlankBillingAccountChoice() {
        TdarUser authenticatedUser = getAdminUser();
        Invoice invoice = new Invoice();
        invoice.markUpdated(authenticatedUser);
        invoice.getItems().add(new BillingItem(new BillingActivity("6 mb", 10f, 0, 0L, 1L, 10L, accountService.getLatestActivityModel()), 1));
        BillingActivity activity = invoice.getItems().get(0).getActivity();
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);

        genericService.saveOrUpdate(activity, invoice);

        //recreate a repeat of the "choose a billing account" step  with a blank account.
        BillingAccount account1 = accountService.reconcileSelectedAccount(-1L, invoice, null, Collections.<BillingAccount>emptyList());
        account1.markUpdated(authenticatedUser);
        accountService.processBillingAccountChoice(account1, invoice, authenticatedUser);

        BillingAccount account2 = accountService.reconcileSelectedAccount(-1L, invoice, null, Collections.<BillingAccount>emptyList());

        //account1 and account2 should  be equal because the system should detect on the second call that it is not necessary to create a new account
        assertThat(account1, is( account2));
    }

}
