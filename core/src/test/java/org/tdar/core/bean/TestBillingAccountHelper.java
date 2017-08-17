package org.tdar.core.bean;

import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericService;

public interface TestBillingAccountHelper {

    /**
     * Creates a new BillingAccount object
     * @param model
     * @param user
     * @return BillingAccount initialized to 6 mb
     */
    default BillingAccount setupAccountWithInvoiceForSpecifiedMb(BillingActivityModel model, TdarUser user, Long size) {
    	BillingAccount account = new BillingAccount();
    	BillingActivity activity = new BillingActivity(size.toString()+" mb", 10f, 0, 0L, 0L, size, model);
    	initAccount(account, activity, getUser());
    	getGenericService().saveOrUpdate(account);
    	return account;
    }


    default BillingAccount setupAccountWithInvoiceFor6Mb(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        BillingActivity activity = new BillingActivity("6 mb", 10f, 0, 0L, 0L, 6L, model);
        initAccount(account, activity, getUser());
        getGenericService().saveOrUpdate(account);
        return account;
    }

    default BillingAccount setupAccountWithInvoiceForOneFile(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        initAccount(account, new BillingActivity("1 file", 10f, 0, 0L, 1L, 0L, model), user);
        getGenericService().saveOrUpdate(account);
        return account;
    }

    default BillingAccount setupAccountWithInvoiceForOneResource(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        initAccount(account, new BillingActivity("1 resource", 10f, 0, 1L, 0L, 0L, model), user);
        /* add one resource */
        // account.resetTransientTotals();
        getGenericService().saveOrUpdate(account);
        return account;
    }

    default BillingAccount setupAccountWithInvoiceSomeResourcesAndSpace(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        initAccount(account, new BillingActivity("10 resource", 100f, 0, 10L, 10L, 100L, model), user);
        /* add one resource */
        // account.resetTransientTotals();
        getGenericService().saveOrUpdate(account);
        return account;
    }

    default BillingAccount setupAccountWithInvoiceFiveResourcesAndSpace(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        account.setName("test account");
        initAccount(account, new BillingActivity("10 resource", 5f, 0, 5L, 5L, 50L, model), user);
        /* add one resource */
        // account.resetTransientTotals();
        getGenericService().saveOrUpdate(account);
        return account;
    }

    default BillingAccount setupAccountWithInvoiceTenOfEach(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        initAccount(account, new BillingActivity("10 resource", 10f, 10, 10L, 10L, 10L, model), user);
        /* add one resource */
        // account.resetTransientTotals();
        getGenericService().saveOrUpdate(account);
        return account;
    }

    default Invoice initAccount(BillingAccount account, BillingActivity activity, TdarUser user) {
        account.markUpdated(user);
        Invoice invoice = setupInvoice(activity, user);
        account.getInvoices().add(invoice);
        return invoice;
    }

    default Invoice setupInvoice(BillingActivity activity, TdarUser user) {
        Invoice invoice = new Invoice();
        invoice.markUpdated(user);
        getGenericService().saveOrUpdate(activity.getModel());
        getGenericService().saveOrUpdate(activity);
        invoice.setNumberOfFiles(activity.getNumberOfFiles());
        invoice.setNumberOfMb(activity.getNumberOfMb());
        invoice.getItems().add(new BillingItem(activity, 1));
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.markFinal();
        getGenericService().saveOrUpdate(invoice);
        getGenericService().saveOrUpdate(invoice.getItems());
        return invoice;
    }

    default BillingAccount setupAccountForPerson(TdarUser p) {
        BillingAccount account = new BillingAccount("my account");
        account.setOwner(p);
        account.setStatus(Status.ACTIVE);
        account.markUpdated(getUser());
        getGenericService().saveOrUpdate(account);
        return account;
    }
    
    default void updateModel(BillingActivityModel model, boolean resources, boolean files, boolean space) {
        model.setCountingResources(resources);
        model.setCountingFiles(files);
        model.setCountingSpace(space);
    }


    GenericService getGenericService();


    TdarUser getUser();
}
