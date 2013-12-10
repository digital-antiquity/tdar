package org.tdar.core.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.billing.ResourceEvaluator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.MessageHelper;

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

    @SuppressWarnings("unchecked")
    public Set<Account> findAccountsForUser(Person user, Status... statuses) {
        if (ArrayUtils.isEmpty(statuses)) {
            statuses = new Status[2];
            statuses[0] = Status.ACTIVE;
            statuses[1] = Status.FLAGGED_ACCOUNT_BALANCE;
        }
        Set<Account> accountGroups = new HashSet<Account>();
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNTS_FOR_PERSON);
        query.setParameter("personId", user.getId());
        query.setParameterList("statuses", statuses);
        accountGroups.addAll(query.list());
        for (AccountGroup group : findAccountGroupsForUser(user)) {
            accountGroups.addAll(group.getAccounts());
        }
        return accountGroups;
    }

    @SuppressWarnings("unchecked")
    public List<AccountGroup> findAccountGroupsForUser(Person user) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNT_GROUPS_FOR_PERSON);
        query.setParameter("personId", user.getId());
        query.setParameterList("statuses", Arrays.asList(Status.ACTIVE));
        return query.list();

    }

    public AccountGroup getAccountGroup(Account account) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNT_GROUP_FOR_ACCOUNT);
        query.setParameter("accountId", account.getId());
        return (AccountGroup) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<Long> findResourcesWithDifferentAccount(List<Resource> resourcesToEvaluate, Account account) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.RESOURCES_WITH_NON_MATCHING_ACCOUNT_ID);
        query.setParameter("accountId", account.getId());
        query.setParameterList("ids", Persistable.Base.extractIds(resourcesToEvaluate));
        return (List<Long>) query.list();
    }

    @SuppressWarnings("unchecked")
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
                accountId = ((BigInteger) obj[1]).longValue();
            }
            Account account = accountIdMap.get(accountId);
            if (account == null) {
                account = find(accountId);
                accountIdMap.put(accountId, account);
            }
            Resource resource = resourceIdMap.get(resourceId);
            if (resource != null) {
                logger.trace("setting account {} for resource {}", accountId, resourceId);
                resource.setAccount(account);
            } else {
                logger.error("resource is null somehow for id: {}, account {}", resourceId, account);
            }
        }
    }

    public void updateAccountInfo(Account account, ResourceEvaluator re) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNT_QUOTA_INIT);
        query.setParameter("accountId", account.getId());
        @SuppressWarnings("unchecked")
        List<Status> statuses = new ArrayList<Status>(CollectionUtils.disjunction(Arrays.asList(Status.values()), re.getUncountedResourceStatuses()));
        query.setParameterList("statuses", statuses);
        Long totalFiles = 0L;
        Long totalSpaceInBytes = 0L;
        for (Object objs : query.list()) {
            Object[] obj = (Object[]) objs;
            if (obj[0] != null) {
                totalFiles = ((Long) obj[0]).longValue();
            }
            if (obj[1] != null) {
                totalSpaceInBytes = ((Long) obj[1]).longValue();
            }
        }
        for (Coupon coupon : account.getCoupons()) {
            totalFiles += coupon.getNumberOfFiles();
            totalSpaceInBytes += coupon.getNumberOfMb() * Coupon.ONE_MB;
        }
        account.setFilesUsed(totalFiles);
        account.setSpaceUsedInBytes(totalSpaceInBytes);
    }

    @SuppressWarnings("unchecked")
    public List<Invoice> findUnassignedInvoicesForUser(Person user) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.UNASSIGNED_INVOICES_FOR_PERSON);
        query.setParameter("personId", user.getId());
        query.setParameterList("statuses", Arrays.asList(TransactionStatus.TRANSACTION_SUCCESSFUL));
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Invoice> findInvoicesForUser(Person user) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.INVOICES_FOR_PERSON);
        query.setParameter("personId", user.getId());
        return query.list();
    }

    public Coupon findCoupon(String code, Person user) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_ACTIVE_COUPON);
        query.setParameter("code", code.toLowerCase());
        query.setParameter("ownerId", user.getId());
        return (Coupon) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public void checkCouponStillValidForCheckout(Coupon coupon, Invoice invoice) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_INVOICE_FOR_COUPON);
        query.setParameter("code", coupon.getCode().toLowerCase());
        for (Invoice inv : (List<Invoice>) query.list()) {
            if (inv.getTransactionStatus().isComplete()) {
                throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("accountDao.coupon_already_used"));
            }
        }
        if (!invoice.getCoupon().getId().equals(coupon.getId())) {
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("accountDao.coupon_assigned_wrong"));
        }
    }
}
