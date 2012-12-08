package org.tdar.core.dao;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;

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
        query.setParameterList("statuses", Arrays.asList(Status.ACTIVE));
        return (List<Account>) query.list();
    }

    public AccountGroup getAccountGroup(Account account) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNT_GROUP_FOR_ACCOUNT);
        query.setParameter("accountId", account.getId());
        return (AccountGroup) query.uniqueResult();
    }

    public List<Long> findResourcesWithDifferentAccount(List<Resource> resourcesToEvaluate, Account account) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.RESOURCES_WITH_NON_MATCHING_ACCOUNT_ID);
        query.setParameter("accountId", account.getId());
        query.setParameterList("ids", Persistable.Base.extractIds(resourcesToEvaluate));
        return (List<Long>) query.list();
    }

    public List<Long> findResourcesWithNullAccount(List<Resource> resourcesToEvaluate) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.RESOURCES_WITH_NULL_ACCOUNT_ID);
        query.setParameterList("ids", Persistable.Base.extractIds(resourcesToEvaluate));
        return (List<Long>) query.list();
    }

    public void updateTransientAccountOnResources(Collection<Resource> resourcesToEvaluate) {
        Map<Long, Resource> resourceIdMap = Persistable.Base.createIdMap(resourcesToEvaluate);
        String sql = String.format(TdarNamedQueries.QUERY_ACCOUNTS_FOR_RESOURCES, StringUtils.join(resourceIdMap.keySet().toArray()));
        if (CollectionUtils.isEmpty(resourceIdMap.keySet()) || resourceIdMap.keySet().size() == 1 && resourceIdMap.keySet().contains(-1L)) {
            return;
        }
        Query query = getCurrentSession().createSQLQuery(sql);

        Map<Long, Account> accountIdMap = new HashMap<Long, Account>();
        for (Object objs : query.list()) {
            Object[] obj = (Object[]) objs;
            Long resourceId = ((BigInteger) obj[0]).longValue();
            Long accountId = null;
            if (obj[1] != null) {
                ((BigInteger) obj[1]).longValue();
            }
            Account account = accountIdMap.get(accountId);
            if (account == null) {
                accountIdMap.put(accountId, find(accountId));
            }
            resourceIdMap.get(resourceId).setAccount(account);
        }
    }
}
