package org.tdar.core.service.processes.upgradeTasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivity.BillingActivityType;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.ResourceEvaluator;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.billing.InvoiceService;
import org.tdar.core.service.billing.PricingOption;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;
import org.tdar.core.service.resource.ResourceService;

/**
 * $Id$
 * 
 * ScheduledProcess to reprocess all datasets.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Component
public class SetupBillingAccountsProcess extends AbstractScheduledBatchProcess<TdarUser> {

    private static final String INVOICE_NOTE = "This invoice was generated on %s to cover %s resources, %s (MB) , and %s files created by %s prior to tDAR charging for usage.  Thank you for your support of tDAR.";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    final long EXTRA_MB = 10l;
    final long EXTRA_FILES = 1l;
    private static final long serialVersionUID = -2313655718394118279L;

    @Autowired
    private transient BillingAccountService accountService;

    @Autowired
    private transient InvoiceService invoiceService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient GenericService genericService;

    @Override
    public String getDisplayName() {
        return "Setup Billing Accounts";
    }

    @Override
    public Class<TdarUser> getPersistentClass() {
        return TdarUser.class;
    }

    @Override
    public List<Long> findAllIds() {
        return new ArrayList<>(entityService.findAllContributorIds());
    }

    @Override
    public boolean isSingleRunProcess() {
        return true;
    }

    public List<Resource> getNextResourceBatch(List<Long> queue) {
        if (CollectionUtils.isEmpty(queue)) {
            logger.trace("No more ids to process");
            return Collections.emptyList();
        }
        int endIndex = Math.min(queue.size(), 100);
        List<Long> sublist = queue.subList(0, endIndex);
        ArrayList<Long> batch = new ArrayList<>(sublist);
        sublist.clear();
        logger.trace("batch {}", batch);
        return genericService.findAll(Resource.class, batch);
    }

    @Override
    public void process(TdarUser person) {
        List<BillingActivity> activeBillingActivities = invoiceService.getActiveBillingActivities();
        BillingActivity oneFileActivity = null;
        BillingActivity oneMbActivity = null;
        for (BillingActivity activity : activeBillingActivities) {
            if ((activity.getActive() == true) && !activity.isProduction()) {
                if ((activity.getNumberOfFiles() == 1L) && (activity.getNumberOfMb() == 0L)) {
                    oneFileActivity = activity;
                }
                if ((activity.getNumberOfFiles() == 0L) && (activity.getNumberOfMb() == 1L)) {
                    oneMbActivity = activity;
                }
            }
        }
        if (oneFileActivity == null) {
            oneFileActivity = new BillingActivity("one file", 0f, accountService.getLatestActivityModel());
            oneFileActivity.setNumberOfFiles(1L);
            oneFileActivity.setMinAllowedNumberOfFiles(0L);
            oneFileActivity.setNumberOfMb(0L);
            oneFileActivity.setActivityType(BillingActivityType.TEST);
            oneFileActivity.setActive(true);
            genericService.saveOrUpdate(oneFileActivity);
        }

        if (oneMbActivity == null) {
            oneMbActivity = new BillingActivity("one mb", 0f, accountService.getLatestActivityModel());
            oneMbActivity.setActive(true);
            oneMbActivity.setNumberOfFiles(0L);
            oneMbActivity.setActivityType(BillingActivityType.TEST);
            oneMbActivity.setMinAllowedNumberOfFiles(0L);
            oneMbActivity.setNumberOfMb(1L);
            genericService.saveOrUpdate(oneMbActivity);
        }
        try {
            String properName = person.getProperName();
            logger.info("starting process for " + properName);
            if (person.getId() == 135028) {
                logger.debug("skipping user: {}", properName);
                return;
            }
            Set<Long> resourceIds = resourceService.findResourcesSubmittedByUser(person);
            ResourceEvaluator re = accountService.getResourceEvaluator();

            ArrayList<Long> queue = new ArrayList<Long>(resourceIds);
            List<Resource> nextResourceBatch = getNextResourceBatch(queue);
            logger.info("{} has {} resources", properName, resourceIds.size());
            while (CollectionUtils.isNotEmpty(nextResourceBatch)) {
                re.evaluateResources(nextResourceBatch);
                nextResourceBatch = getNextResourceBatch(queue);
            }
            queue = new ArrayList<Long>(resourceIds);

            long spaceUsedInMb = EXTRA_MB + re.getSpaceUsedInMb();
            long filesUsed = EXTRA_FILES + re.getFilesUsed();
            PricingOption option = invoiceService.getCheapestActivityByFiles(filesUsed, spaceUsedInMb, true);
            PricingOption option2 = invoiceService.getCheapestActivityByFiles(filesUsed, spaceUsedInMb, false);
            PricingOption option3 = invoiceService.getCheapestActivityBySpace(filesUsed, spaceUsedInMb);
            logger.info("****** RE : {}", re.toString());
            logger.info("{}|{}|{}|{}|{}|{}|{}|{}",
                    person.getId(), properName, option, option2, option3, re.getFilesUsed(),
                    re.getResourcesUsed(), re.getSpaceUsedInMb());
            List<BillingItem> items = new ArrayList<>();
            logger.info(" {}  {} ", Long.valueOf(spaceUsedInMb).intValue(), Long.valueOf(filesUsed).intValue());
            items.add(new BillingItem(oneMbActivity, Long.valueOf(spaceUsedInMb).intValue()));
            items.add(new BillingItem(oneFileActivity, Long.valueOf(filesUsed).intValue()));
            Invoice invoice = new Invoice();
            invoice.setPaymentMethod(PaymentMethod.MANUAL);
            invoice.setNumberOfFiles(filesUsed);
            invoice.getItems().addAll(items);
            invoice.setNumberOfMb(spaceUsedInMb);
            invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
            invoice.setOwner(person);
            invoice.markUpdated(person);
            invoice.markFinal();
            invoice.setOtherReason(String.format(INVOICE_NOTE, new Date(), re.getResourcesUsed(), re.getSpaceUsedInMb(), re.getFilesUsed(), properName));
            genericDao.saveOrUpdate(invoice);
            genericDao.saveOrUpdate(invoice.getItems());
            logger.info("final: {}f {}mb {}", invoice.getTotalNumberOfFiles(), invoice.getTotalSpaceInMb(), person.getId());
            BillingAccount account = new BillingAccount(String.format("%s's Account", properName));
            account.setDescription("auto-generated account created by tDAR to cover past contributions");
            account.setOwner(person);
            account.markUpdated(person);
            genericDao.saveOrUpdate(account);
            account.getInvoices().add(invoice);
            nextResourceBatch = getNextResourceBatch(queue);
            while (CollectionUtils.isNotEmpty(nextResourceBatch)) {
                accountService.updateQuota(account, person, nextResourceBatch.toArray(new Resource[0]));
                nextResourceBatch = getNextResourceBatch(queue);
            }
            genericDao.saveOrUpdate(account);
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

    @Override
    public int getBatchSize() {
        return 5;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

}
