package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivity.BillingActivityType;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.billing.PricingOption;
import org.tdar.core.service.billing.PricingOption.PricingType;
import org.tdar.utils.MathUtils;

@Component
public class InvoiceDao extends Dao.HibernateBase<Invoice>{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BillingAccountDao accountDao;
    
    public InvoiceDao() {
        super(Invoice.class);
    }

    /**
     * Calculate @link PricingOption based on @link PricingType calculation
     * 
     * @param invoice
     * @param pricingType
     * @return
     */
    public PricingOption calculateActivities(Invoice invoice, PricingType pricingType) {
        switch (pricingType) {
            case SIZED_BY_FILE_ABOVE_TIER:
                return getCheapestActivityBySpace(invoice.getNumberOfFiles(), invoice.getNumberOfMb());
            case SIZED_BY_FILE_ONLY:
                return getCheapestActivityByFiles(invoice.getNumberOfFiles(), invoice.getNumberOfMb(), false);
            case SIZED_BY_MB:
                return getCheapestActivityBySpace(invoice.getNumberOfFiles(), invoice.getNumberOfMb());
        }
        return null;
    }

    /**
     * Calculate rate purely on space
     * 
     * @param option
     * @param spaceActivity
     * @param spaceNeeded
     */
    public void calculateSpaceActivity(PricingOption option, BillingActivity spaceActivity, Long spaceNeeded) {
        BillingItem extraSpace;
        if ((spaceNeeded > 0) && (spaceActivity != null)) {
            int qty = (int) MathUtils.divideByRoundUp(spaceNeeded, spaceActivity.getNumberOfMb());
            extraSpace = new BillingItem(spaceActivity, qty);
            option.getItems().add(extraSpace);
        }
    }

    /**
     * As we can bill by different things, # of Files, # of MB, we want to find out what the cheapest method is by # of files either using exact entries in
     * tiers, or even jumping to the next tier which may be cheaper.
     * 
     * @param numFiles_
     * @param numMb_
     * @param exact
     * @return
     */
    public PricingOption getCheapestActivityByFiles(Long numFiles_, Long numMb_, boolean exact) {
        Long numFiles = 0L;
        Long numMb = 0L;
        if (numFiles_ != null) {
            numFiles = numFiles_;
        }
        if (numMb_ != null) {
            numMb = numMb_;
        }

        if ((numFiles == 0) && (numMb == 0)) {
            return null;
        }

        PricingOption option = new PricingOption(PricingType.SIZED_BY_FILE_ONLY);
        if (!exact) {
            option.setType(PricingType.SIZED_BY_FILE_ABOVE_TIER);
        }
        List<BillingItem> items = new ArrayList<>();
        logger.info("files: {} mb: {}", numFiles, numMb);

        for (BillingActivity activity : getActiveBillingActivities()) {
            int calculatedNumberOfFiles = numFiles.intValue(); // Don't use test activities or activities that are Just about MB
            if ((activity.getActivityType() == BillingActivityType.TEST) || !activity.supportsFileLimit()) {
                continue;
            }
            logger.trace("n:{} min:{}", numFiles, activity.getMinAllowedNumberOfFiles());
            if (exact && (numFiles < activity.getMinAllowedNumberOfFiles())) {
                continue;
            }

            // 2 cases (1) exact value; (2) where the next step up might actually be cheaper
            if (!exact && (activity.getMinAllowedNumberOfFiles() >= numFiles)) {
                calculatedNumberOfFiles = activity.getMinAllowedNumberOfFiles().intValue();
            }

            BillingItem e = new BillingItem(activity, calculatedNumberOfFiles);
            logger.trace(" -- {} ({})", e.getActivity().getName(), e);
            items.add(e);
        }
        logger.trace("{} {}", option, items);
        // finding the cheapest
        BillingItem lowest = null;
        for (BillingItem item : items) {
            if (lowest == null) {
                lowest = item;
            } else if (lowest.getSubtotal() > item.getSubtotal()) {
                lowest = item;
            } else if (lowest.getSubtotal().equals(item.getSubtotal())) {
                /*
                 * FIXME: if two items have the SAME price, but one has more "stuff" we should choose the one with more "stuff"
                 * Caution: there are many corner cases whereby we may not know about how to determine what's the better pricing decision when we're providing a
                 * per-file and per-mb price. eg: 8 files and 200 MB or 10 files and 150? There's no way to choose.
                 */
                logger.info("{} =??= {} ", lowest, item);
            }
        }
        option.getItems().add(lowest);
        BillingActivity spaceActivity = getSpaceActivity();
        if (lowest == null) {
            logger.warn("no options found for f:{} m:{} ", numFiles, numMb);
            return null;
        }
        Long spaceAvailable = lowest.getQuantity() * lowest.getActivity().getNumberOfMb();
        Long spaceNeeded = numMb - spaceAvailable;
        logger.info("adtl. space needed: {} avail: {} ", spaceNeeded, spaceAvailable);
        logger.info("space act: {} ", getSpaceActivity());
        calculateSpaceActivity(option, spaceActivity, spaceNeeded);

        if ((option.getTotalMb() < numMb) || (option.getTotalFiles() < numFiles)) {
            return null;
        }

        return option;
    }

    /**
     * As we can bill by different things, # of Files, # of MB, we want to find out what the cheapest method by MB.
     * 
     * @param numFiles_
     * @param spaceInMb_
     * @return
     */
    public PricingOption getCheapestActivityBySpace(Long numFiles_, Long spaceInMb_) {
        Long numFiles = 0L;
        Long spaceInMb = 0L;
        if (numFiles_ != null) {
            numFiles = numFiles_;
        }
        if (spaceInMb_ != null) {
            spaceInMb = spaceInMb_;
        }

        if ((numFiles.longValue() == 0L) && (spaceInMb.longValue() == 0L)) {
            return null;
        }

        PricingOption option = new PricingOption(PricingType.SIZED_BY_MB);
        List<BillingItem> items = new ArrayList<>();
        BillingActivity spaceActivity = getSpaceActivity();
        if ((spaceActivity != null) && ((numFiles == null) || (numFiles.intValue() == 0))) {
            calculateSpaceActivity(option, spaceActivity, spaceInMb);
            return option;
        }

        for (BillingActivity activity : getActiveBillingActivities()) {
            if (activity.getActivityType() == BillingActivityType.TEST) {
                continue;
            }

            if (activity.supportsFileLimit()) {
                Long total = MathUtils.divideByRoundUp(spaceInMb, activity.getNumberOfMb());
                Long minAllowedNumberOfFiles = activity.getMinAllowedNumberOfFiles();
                if (minAllowedNumberOfFiles == null) {
                    minAllowedNumberOfFiles = 0L;
                }

                if ((total * activity.getNumberOfFiles()) < minAllowedNumberOfFiles) {
                    total = minAllowedNumberOfFiles;
                }

                if (total < (numFiles / activity.getNumberOfFiles())) {
                    total = numFiles;
                }
                items.add(new BillingItem(activity, total.intValue()));
            }
        }
        BillingItem lowest = null;
        for (BillingItem item : items) {
            if (lowest == null) {
                lowest = item;
            } else if (lowest.getSubtotal() > item.getSubtotal()) {
                lowest = item;
            }
        }
        option.getItems().add(lowest);
        if ((option == null) || (option.getTotalMb() < spaceInMb) || (option.getTotalFiles() < numFiles)) {
            return null;
        }

        return option;
    }

    /**
     * Iterate through all active @link BillingActivity entries and find the first that is only MB.
     * 
     * @return
     */
    public BillingActivity getSpaceActivity() {
        for (BillingActivity activity : getActiveBillingActivities()) {
            if (activity.getActivityType() == BillingActivityType.TEST) {
                continue;
            }
            if (((activity.getNumberOfFiles() == null) || (activity.getNumberOfFiles() == 0))
                    && ((activity.getNumberOfResources() == null) || (activity.getNumberOfResources() == 0)) && (activity.getNumberOfMb() != null)
                    && (activity.getNumberOfMb() > 0)) {
                return activity;
            }
        }
        return null;
    }

    public List<BillingActivity> getActiveBillingActivities() {
        List<BillingActivity> toReturn = new ArrayList<>();

        BillingActivityModel model = accountDao.getLatestActivityModel();
        for (BillingActivity activity : model.getActivities()) {
            if (activity.getActive()) {
                toReturn.add(activity);
            }
        }
        Collections.sort(toReturn);
        logger.trace("{}", toReturn);
        return toReturn;
    }

    public void completeInvoice(Invoice invoice) {
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.getOwner().setContributor(true);
        BillingAccount account = accountDao.getAccountForInvoice(invoice);
        saveOrUpdate(invoice);
        try {
            if (account != null) {
                Number numFlagged = accountDao.findCountOfFlaggedResourcesInAccount(account);
                if (account.getStatus() == Status.FLAGGED_ACCOUNT_BALANCE || numFlagged != null && numFlagged.longValue() > 0) {
                    accountDao.updateQuota(account, account.getResources(), invoice.getOwner());
                }
            }
        } catch (Exception e) {
            logger.error("exception ocurred in processing FLAGGED ACCOUNT", e);
        }
        
        Coupon coupon = invoice.getCoupon();
        // grant rights to resource(s)
        if (coupon != null && !CollectionUtils.isEmpty(coupon.getResourceIds())) {
            List<Resource> findAll = findAll(Resource.class, coupon.getResourceIds());
            for (Resource res : findAll) {
                res = markWritableOnExistingSession(res);
                ResourceCollection rc = res.getInternalResourceCollection();
                if (rc == null) {
                    rc = new ResourceCollection(CollectionType.INTERNAL);
                    rc.markUpdated(invoice.getOwner());
                    saveOrUpdate(rc);
                    res.getResourceCollections().add(rc);
                }
                rc = markWritableOnExistingSession(rc);
                rc.getResources().add(res);
                AuthorizedUser e = new AuthorizedUser(invoice.getOwner(), GeneralPermissions.MODIFY_RECORD);
                e = markWritableOnExistingSession(e);
                rc.getAuthorizedUsers().add(e);
                res.markUpdated(invoice.getOwner());
                res.setAccount(account);
                account.getResources().add(res);
                accountDao.updateQuota(account, account.getResources(),invoice.getOwner());
                saveOrUpdate(rc);
                saveOrUpdate(res);
                logger.debug("{}",res);
            }
        }
    }

    
}
