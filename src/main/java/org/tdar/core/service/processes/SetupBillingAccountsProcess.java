package org.tdar.core.service.processes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.billing.ResourceEvaluator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.data.PricingOption;

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
public class SetupBillingAccountsProcess extends ScheduledBatchProcess<Person> {

    private static final String INVOICE_NOTE = "auto-generated invoice created on %s to cover %s resources, %s (MB) , and %s files created by %s prior to tDAR charging for usage.  Thank you for your support of tDAR.";

    final static long EXTRA_MB = 0l;
    final static long EXTRA_FILES = 5l;
    private static final long serialVersionUID = -2313655718394118279L;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private ResourceService resourceService;

    @Override
    public String getDisplayName() {
        return "Setup Billing Accounts";
    }

    @Override
    public Class<Person> getPersistentClass() {
        return Person.class;
    }

    @Override
    public List<Long> findAllIds() {
        return new ArrayList<Long>(entityService.findAllContributorIds());
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
        ArrayList<Long> batch = new ArrayList<Long>(sublist);
        sublist.clear();
        logger.trace("batch {}", batch);
        return resourceService.findAll(Resource.class, batch);
        }

    @Override
    public void process(Person person) {
        try {
            logger.info("starting process for " + person.getProperName());
            if (person.getId() == 135028) {
                logger.debug("skipping user: {}", person.getProperName());
                return;
            }
            Set<Long> resourceIds = resourceService.findResourcesSubmittedByUser(person);
            ResourceEvaluator re = accountService.getResourceEvaluator();

            ArrayList<Long> queue = new ArrayList<Long>(resourceIds);
            List<Resource> nextResourceBatch = getNextResourceBatch(queue);
            logger.info("{} has {} resources", person.getProperName(), resourceIds.size());
            while (CollectionUtils.isNotEmpty(nextResourceBatch)) {
                logger.info("processing batch {}", nextResourceBatch);
                re.evaluateResources(nextResourceBatch);
                nextResourceBatch = getNextResourceBatch(queue);
            }
            queue = new ArrayList<Long>(resourceIds);

            long spaceUsedInMb = EXTRA_MB + re.getSpaceUsedInMb();
            long filesUsed = EXTRA_FILES + re.getFilesUsed();
            PricingOption option = accountService.getCheapestActivityByFiles(filesUsed, spaceUsedInMb, true);
            PricingOption option2 = accountService.getCheapestActivityByFiles(filesUsed, spaceUsedInMb, false);
            PricingOption option3 = accountService.getCheapestActivityBySpace(filesUsed, spaceUsedInMb);
            logger.info("****** RE : " + re.toString());
            logger.info(String.format("%s|%s|%s|%s|%s|%s|%s|%s", person.getId(), person.getProperName(), option, option2, option3, re.getFilesUsed(),
                    re.getResourcesUsed(),
                    re.getSpaceUsedInMb()));
            Invoice invoice = new Invoice(person, PaymentMethod.MANUAL, filesUsed, spaceUsedInMb, option.getItems());
            invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
            invoice.setOwner(person);
            invoice.markUpdated(person);
            invoice.setOtherReason(String.format(INVOICE_NOTE, new Date(), re.getResourcesUsed(), re.getSpaceUsedInMb(), re.getFilesUsed(),
                    person.getProperName()));
            // logger.info(invoice.getOtherReason());
            genericDao.saveOrUpdate(invoice);
            Account account = new Account(String.format("%s's Account", person.getProperName()));
            account.setDescription("auto-generated account created by tDAR to cover past contributions");
            account.setOwner(person);
            account.markUpdated(person);
            genericDao.saveOrUpdate(account);
            account.getInvoices().add(invoice);
            nextResourceBatch = getNextResourceBatch(queue);
            while (CollectionUtils.isNotEmpty(nextResourceBatch)) {
                accountService.updateQuota(accountService.getResourceEvaluator(), account, false, nextResourceBatch.toArray(new Resource[0]));
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
        return true;
    }

}
