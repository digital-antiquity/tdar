package org.tdar.core.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Account.AccountAdditionStatus;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.AccountDao;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

@Service
public class AccountService {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private AccountDao accountDao;

    /*
     * Find all accounts for user: return accounts that are active and have not met their quota
     */
    public List<Account> listAvailableAccountsForUser(Person user) {
        return accountDao.findAccountsForUser(user);
    }

    public void addResourceToAccount(Person user, Resource resource) {
        List<Account> accounts = listAvailableAccountsForUser(user);
        // if it doesn't count
        AccountAdditionStatus canAddResource = null;
        for (Account account : accounts) {
            canAddResource = account.canAddResource(resource);
            if (canAddResource == AccountAdditionStatus.CAN_ADD_RESOURCE) {
                account.getResources().add(resource);
                break;
            }
        }
        if (canAddResource != AccountAdditionStatus.CAN_ADD_RESOURCE) {
            throw new TdarRecoverableRuntimeException(String.format("Cannot add resource because %s", canAddResource));
        }
    }
}
