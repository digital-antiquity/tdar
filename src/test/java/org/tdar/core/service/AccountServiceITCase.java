package org.tdar.core.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Status;
import org.tdar.struts.action.TdarActionException;

public class AccountServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    AccountService accountService;

    @Autowired
    GenericService genericService;

    @Test
    @Rollback
    public void testAccountList() throws TdarActionException {
        Person p = createAndSaveNewPerson();
        Account account = new Account("my account");
        account.setOwner(p);
        account.setStatus(Status.ACTIVE);
        account.markUpdated(getUser());
        genericService.saveOrUpdate(account);
        Account accountWithPermissions = new Account("my account");
        Person p2 = createAndSaveNewPerson("a@aas", "bb");
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
    public void testAvaliableActivities() throws TdarActionException {
        BillingActivity disabledDctivity = new BillingActivity();
        disabledDctivity.setEnabled(false);
        disabledDctivity.setName("not active");
        genericService.saveOrUpdate(disabledDctivity);

        BillingActivity ctivity = new BillingActivity();
        ctivity.setEnabled(true);
        ctivity.setName("active");
        genericService.saveOrUpdate(ctivity);
        
        List<BillingActivity> activeBillingActivities = accountService.getActiveBillingActivities();
        assertTrue(activeBillingActivities.contains(ctivity));
        assertFalse(activeBillingActivities.contains(disabledDctivity));

        
    }    
    
}
