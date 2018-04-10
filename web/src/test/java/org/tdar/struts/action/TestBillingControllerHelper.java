package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.billing.BillingAccountController;
import org.tdar.struts.action.billing.CouponCreationAction;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

public interface TestBillingControllerHelper {

    final Logger logger_ = LoggerFactory.getLogger(TestBillingControllerHelper.class);

    default String createCouponForAccount(Long numberOfFiles, Long numberOfMb, BillingAccount account, Invoice invoice) throws TdarActionException {
        return createCouponForAccount(numberOfFiles, numberOfMb, account, invoice, null);
    }

    default String createCouponForAccount(Long numberOfFiles, Long numberOfMb, BillingAccount account, Invoice invoice, TdarUser user)
            throws TdarActionException {
        CouponCreationAction controller = setupControllerForCoupon(account, invoice, user);
        controller.setNumberOfFiles(numberOfFiles);
        controller.setNumberOfMb(numberOfMb);
        try {
            assertEquals(Action.SUCCESS, controller.execute());
        } catch (Exception e) {
            logger_.warn("{}", e);
        }
        return controller.getAccount().getCoupons().iterator().next().getCode();
    }

    default CouponCreationAction setupControllerForCoupon(BillingAccount account, Invoice invoice) throws TdarActionException {
        return setupControllerForCoupon(account, invoice, null);
    }

    default CouponCreationAction setupControllerForCoupon(BillingAccount account, Invoice invoice, TdarUser user) throws TdarActionException {
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.markFinal();
        getGenericService().saveOrUpdate(invoice);
        getGenericService().synchronize();
        logger_.info("{}", invoice);

        assertTrue(invoice.getNumberOfFiles() > 0);
        BillingAccountController controller = null;
        if (user != null) {
            controller = generateNewInitializedController(BillingAccountController.class, user);
        } else {
            controller = generateNewInitializedController(BillingAccountController.class);
        }
        controller.setInvoiceId(invoice.getId());
        controller.setId(account.getId());
        controller.setName("test");
        controller.prepare();
        controller.edit();
        boolean seen = false;
        controller.setServletRequest(getServletPostRequest());
        getGenericService().refresh(controller.getAccount());
        getAccountService().updateQuota(controller.getAccount(), controller.getAuthenticatedUser());
        try {
            logger_.info("saving account");
            assertEquals(Action.SUCCESS, controller.save());
        } catch (Exception e) {
            logger_.error("exception : {}", e);
            seen = true;
        }
        assertFalse(seen);
        CouponCreationAction controllerc = generateNewInitializedController(CouponCreationAction.class);
        if (user != null) {
            controllerc = generateNewInitializedController(CouponCreationAction.class, user);
        } else {
            controllerc = generateNewInitializedController(CouponCreationAction.class);
        }
        controllerc.setId(account.getId());
        controllerc.prepare();
        controllerc.setQuantity(1);
        controllerc.setServletRequest(getServletPostRequest());
        return controllerc;
    }

    default BillingAccount createAccount(TdarUser owner, GenericService genericService) {
        BillingAccount account = new BillingAccount("my account");
        account.setDescription("this is an account for : " + owner.getProperName());
        account.setOwner(owner);
        account.getAuthorizedUsers().add(new AuthorizedUser(owner, owner, Permissions.EDIT_ACCOUNT));
        account.markUpdated(owner);
        getGenericService().saveOrUpdate(account);
        return account;
    }

    default Invoice createInvoice(TdarUser person, TransactionStatus status, GenericService genericService, BillingItem... items) {
        Invoice invoice = new Invoice();
        invoice.setItems(new ArrayList<BillingItem>());
        for (BillingItem item : items) {
            invoice.getItems().add(item);
        }
        invoice.setOwner(person);
        invoice.setTransactionStatus(status);
        getGenericService().saveOrUpdate(invoice);
        return invoice;
    }

    HttpServletRequest getServletRequest();

    HttpServletRequest getServletPostRequest();

    HttpServletResponse getServletResponse();

    <T extends ActionSupport> T generateNewInitializedController(Class<T> controllerClass, TdarUser user);

    <T extends ActionSupport> T generateNewInitializedController(Class<T> class1);

    GenericService getGenericService();

    BillingAccountService getAccountService();
}
