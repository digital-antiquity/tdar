<<<<<<< mine
//package org.tdar.core.service.processes;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Set;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.tdar.core.bean.billing.Account;
//import org.tdar.core.bean.billing.Invoice;
//import org.tdar.core.bean.billing.Invoice.TransactionStatus;
//import org.tdar.core.bean.billing.ResourceEvaluator;
//import org.tdar.core.bean.entity.Person;
//import org.tdar.core.bean.resource.Resource;
//import org.tdar.core.bean.util.ScheduledBatchProcess;
//import org.tdar.core.dao.external.payment.PaymentMethod;
//import org.tdar.core.service.AccountService;
//import org.tdar.core.service.EntityService;
//import org.tdar.core.service.resource.ResourceService;
//import org.tdar.struts.data.PricingOption;
//
///**
// * $Id$
// * 
// * ScheduledProcess to reprocess all datasets.
// * 
// * 
// * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
// * @version $Rev$
// */
//
//@Component
//public class SetupBillingAccountsProcess extends ScheduledBatchProcess<Person> {
//
//    private static final String INVOICE_NOTE = "auto-generated invoice created on %s to cover %s resources, %s (MB) , and %s files created by %s prior to tDAR charging for usage.  Thank you for your support of tDAR.";
//
//    final static long EXTRA_MB = 0l;
//    final static long EXTRA_FILES = 5l;
//    private static final long serialVersionUID = -2313655718394118279L;
//
//    @Autowired
//    private AccountService accountService;
//
//    @Autowired
//    private EntityService entityService;
//
//    @Autowired
//    private ResourceService resourceService;
//
//    @Override
//    public String getDisplayName() {
//        return "Setup Billing Accounts";
//    }
//
//    @Override
//    public Class<Person> getPersistentClass() {
//        return Person.class;
//    }
//
//    @Override
//    public List<Long> findAllIds() {
//        return new ArrayList<Long>(entityService.findAllContributorIds());
//    }
//
//    @Override
//    public boolean isSingleRunProcess() {
//        return true;
//    }
//
//    @Override
//    public void process(Person person) {
//        Set<Resource> resources = resourceService.findResourcesSubmittedByUser(person);
//        ResourceEvaluator re = accountService.getResourceEvaluator();
//        re.evaluateResources(resources);
//        long spaceUsedInMb = EXTRA_MB + re.getSpaceUsedInMb();
//        long filesUsed = EXTRA_FILES + re.getFilesUsed();
//        PricingOption option = accountService.getCheapestActivityByFiles(filesUsed, spaceUsedInMb, true);
//        PricingOption option2 = accountService.getCheapestActivityByFiles(filesUsed, spaceUsedInMb, false);
//        PricingOption option3 = accountService.getCheapestActivityBySpace(filesUsed, spaceUsedInMb);
//        logger.info("****** RE : " + re.toString());
//        logger.info(String.format("%s|%s|%s|%s",person.getProperName(), option, option2, option3));
//        Invoice invoice = new Invoice(person, PaymentMethod.MANUAL, filesUsed, spaceUsedInMb, option.getItems());
//        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
//        invoice.setOwner(person);
//        invoice.markUpdated(person);
//        invoice.setOtherReason(String.format(INVOICE_NOTE, new Date(), re.getResourcesUsed(), re.getSpaceUsedInMb(), re.getFilesUsed(), person.getProperName()));
//        logger.info(invoice.getOtherReason());
//        genericDao.saveOrUpdate(invoice);
//        Account account = new Account(String.format("%s's Account", person.getProperName()));
//        account.setDescription("auto-generated account created by tDAR to cover past contributions");
//        account.setOwner(person);
//        account.markUpdated(person);
//        genericDao.saveOrUpdate(account);
//        account.getInvoices().add(invoice);
//        accountService.updateQuota(accountService.getResourceEvaluator(), account, false, resources.toArray(new Resource[0]));
//        genericDao.saveOrUpdate(account);
//    }
//
//    @Override
//    public int getBatchSize() {
//        return 5;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return false;
//    }
//
//}
=======
package org.tdar.core.service.processes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.billing.ResourceEvaluator;
import org.tdar.core.bean.entity.Person;
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

    @Override
    public void process(Person person) {
        Set<Resource> resources = resourceService.findResourcesSubmittedByUser(person);
        ResourceEvaluator re = accountService.getResourceEvaluator();
        re.evaluateResources(resources);
        long spaceUsedInMb = EXTRA_MB + re.getSpaceUsedInMb();
        long filesUsed = EXTRA_FILES + re.getFilesUsed();
        PricingOption option = accountService.getCheapestActivityByFiles(filesUsed, spaceUsedInMb, false);
        logger.info("****** RE : " + re.toString());
        Invoice invoice = new Invoice(person, PaymentMethod.MANUAL, filesUsed, spaceUsedInMb, option.getItems());
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.setOwner(person);
        invoice.markUpdated(person);
        invoice.setOtherReason(String.format(INVOICE_NOTE, new Date(), re.getResourcesUsed(), re.getSpaceUsedInMb(), re.getFilesUsed(), person.getProperName()));
        logger.info(invoice.getOtherReason());
        genericDao.saveOrUpdate(invoice);
        Account account = new Account(String.format("%s's Account", person.getProperName()));
        account.setDescription("auto-generated account created by tDAR to cover past contributions");
        account.setOwner(person);
        account.markUpdated(person);
        genericDao.saveOrUpdate(account);
        account.getInvoices().add(invoice);
        accountService.updateQuota(accountService.getResourceEvaluator(), account, false, resources.toArray(new Resource[0]));
        genericDao.saveOrUpdate(account);
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
>>>>>>> theirs
