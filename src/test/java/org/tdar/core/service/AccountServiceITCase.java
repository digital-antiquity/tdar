package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.AccountAdditionStatus;
import org.tdar.core.service.billing.AccountService;
import org.tdar.core.service.billing.InvoiceService;

public class AccountServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    AccountService accountService;
    
    @Autowired
    InvoiceService invoiceService;

    @Test
    @Rollback
    public void testAccountList() {
        TdarUser p = createAndSaveNewPerson();
        Account account = setupAccountForPerson(p);
        Account accountWithPermissions = new Account("my account");
        TdarUser p2 = createAndSaveNewPerson("a@aas", "bb");
        accountWithPermissions.setOwner(p2);
        accountWithPermissions.markUpdated(getUser());
        accountWithPermissions.setStatus(Status.ACTIVE);
        accountWithPermissions.getAuthorizedMembers().add(p);
        genericService.saveOrUpdate(accountWithPermissions);

        List<Account> accountsForUser = accountService.listAvailableAccountsForUser(p);
        assertTrue(accountsForUser.contains(account));
        assertTrue(accountsForUser.contains(accountWithPermissions));

        accountsForUser = accountService.listAvailableAccountsForUser(getUser());
        assertFalse(accountsForUser.contains(account));
        assertFalse(accountsForUser.contains(accountWithPermissions));
    }

    @Test
    @Rollback
    public void testAccountGroups() {
        AccountGroup group = new AccountGroup();
        group.setName("my account group");
        group.markUpdated(getBasicUser());
        Account accountForPerson = setupAccountForPerson(getBasicUser());
        Account accountForPerson2 = setupAccountForPerson(createAndSaveNewPerson());
        accountForPerson2.getAuthorizedMembers().add(getBasicUser());
        group.getAccounts().add(accountForPerson);
        group.getAccounts().add(accountForPerson2);
        genericService.saveOrUpdate(group);
        assertEquals(accountService.getAccountGroup(accountForPerson), group);
    }

    @Test
    @Rollback(false)
    public void updateOverdrawnAccountTest() throws InstantiationException, IllegalAccessException {
        Account account = setupAccountForPerson(getBasicUser());
        BillingActivityModel model = new BillingActivityModel();
        model.setCountingResources(false);
        model.setCountingFiles(true);
        model.setCountingSpace(false);
        model.setActive(true);
        model.setVersion(100);
        genericService.saveOrUpdate(model);
        Document resource = generateDocumentWithFileAndUseDefaultUser();
        resource.setAccount(account);
        final long rid = resource.getId();
        final long accountId = account.getId();
        genericService.saveOrUpdate(resource);
        AccountAdditionStatus updateQuota = accountService.updateQuota(account, resource);
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

        setVerifyTransactionCallback(new TransactionCallback<Document>() {
            @Override
            public Document doInTransaction(org.springframework.transaction.TransactionStatus status) {
                Account account = genericService.find(Account.class, accountId);
                Document resource2 = genericService.find(Document.class, rid);
                genericService.update(resource2);
                assertFalse(resource2.isUpdated());
                assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, resource2.getStatus());
                accountService.updateQuota(account, account.getResources());

                assertEquals(Status.ACTIVE, resource2.getStatus());
                genericService.delete(resource2);
                genericService.delete(account);
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
    public void testAccountGroupPermissions()  {
        AccountGroup group = new AccountGroup();
        group.setName("my account group");
        group.markUpdated(getBasicUser());
        Account accountForPerson = setupAccountForPerson(getBasicUser());
        Account accountForPerson2 = setupAccountForPerson(getBasicUser());
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
    public void testAvaliableActivities() {
        BillingActivityModel model = new BillingActivityModel();
        BillingActivity disabledDctivity = new BillingActivity();
        disabledDctivity.setEnabled(false);
        disabledDctivity.setName("not active");
        genericService.saveOrUpdate(model);
        disabledDctivity.setModel(model);
        genericService.saveOrUpdate(disabledDctivity);

        BillingActivity ctivity = new BillingActivity("test", 1f, model);
        ctivity.setEnabled(true);
        ctivity.setName("active");
        genericService.saveOrUpdate(ctivity);

        List<BillingActivity> activeBillingActivities = invoiceService.getActiveBillingActivities();
        assertTrue(activeBillingActivities.contains(ctivity));
        assertFalse(activeBillingActivities.contains(disabledDctivity));
    }

}
