package org.tdar.struts.action;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.GenericService;

public class TestBillingHelper {

    public static BillingAccount createAccount(TdarUser owner, GenericService genericService) {
        BillingAccount account = new BillingAccount("my account");
        account.setDescription("this is an account for : " + owner.getProperName());
        account.setOwner(owner);
        account.markUpdated(owner);
        genericService.saveOrUpdate(account);
        return account;
    }

    public Invoice createInvoice(TdarUser person, TransactionStatus status, GenericService genericService, BillingItem... items) {
        Invoice invoice = new Invoice();
        invoice.setItems(new ArrayList<BillingItem>());
        for (BillingItem item : items) {
            invoice.getItems().add(item);
        }
        invoice.setOwner(person);
        invoice.setTransactionStatus(status);
        genericService.saveOrUpdate(invoice);
        return invoice;
    }
}
