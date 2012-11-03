package org.tdar.core.dao;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.Person;

/**
 * $Id$
 * 
 * Provides DAO access for Person entities, including a variety of methods for
 * looking up a Person in tDAR.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class AccountDao extends Dao.HibernateBase<Account> {

    public AccountDao() {
        super(Account.class);
    }

    public List<Account> findAccountsForUser(Person user) {
        return Arrays.asList(new Account("test account"));
    }
}
