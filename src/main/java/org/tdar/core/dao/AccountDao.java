package org.tdar.core.dao;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.AccountGroup;
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

    /*
     * FixMe: replace with a HQL query this should find all accounts the user is an owner on or all child account they're the owner of an account group; if
     * administrator, or finance person should be "all accounts".
     */
    @SuppressWarnings("unchecked")
    public List<Account> findAccountsForUser(Person user) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNTS_FOR_PERSON);
        query.setParameter("personId", user.getId());
        return (List<Account>) query.list();
    }

    public AccountGroup getAccountGroup(Account account) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNT_GROUP_FOR_ACCOUNT);
        query.setParameter("accountId", account.getId());
        return (AccountGroup) query.uniqueResult();
    }
}
