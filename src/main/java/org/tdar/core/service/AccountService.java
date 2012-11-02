package org.tdar.core.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.GenericDao;

@Service
public class AccountService {

    @Autowired
    private GenericDao genericDao;

    /*
     * Find all accounts for user: return accounts that are active and have not met their quota
     */
    public List<Account> listAvailableAccountsForUser(Person user) {
        return Arrays.asList(new Account("test account"));
    }

    public void addResourceToAccount(Person user, Resource resource) {
        
        switch (resource.getResourceType()) {
            case CODING_SHEET:
            case ONTOLOGY:
            case PROJECT:
                return;
            default:
        }
        
        List<Account> accounts = listAvailableAccountsForUser(user);
//        for (Account account: accounts) {
//            if (account.can)
//        }
    }
}
