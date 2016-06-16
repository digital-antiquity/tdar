/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.billing.BillingAccountController;
import org.tdar.struts.action.billing.CouponCreationAction;

import com.opensymphony.xwork2.Action;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractResourceControllerITCase extends AbstractControllerITCase {
    /*
     * FIXME: figure out if we can load the resource within a new transaction.
     * otherwise since we're running within the same transaction Hibernate's first-level cache
     * will return the same resource that we saved initially as opposed to loading it again
     * within a new transaction for a new web request.
     */
    // @Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.SERIALIZABLE)

    @Autowired
    BillingAccountService accountService;

    public ResourceCreatorProxy getNewResourceCreator(String last, String first, String email, Long id, ResourceCreatorRole role) {
        ResourceCreatorProxy rcp = new ResourceCreatorProxy();
        Person p = rcp.getPerson();
        rcp.getPerson().setLastName(last);
        rcp.getPerson().setFirstName(first);
        rcp.getPerson().setEmail(email);
        // id may be null
        rcp.getPerson().setId(id);
        Institution inst = new Institution();
        inst.setName("University of TEST");
        p.setInstitution(inst);
        rcp.setRole(role);
        return rcp;
    }

    public String createCouponForAccount(Long numberOfFiles, Long numberOfMb, BillingAccount account, Invoice invoice) throws TdarActionException {
        return createCouponForAccount(numberOfFiles, numberOfMb, account, invoice,null);
    }
    
    public String createCouponForAccount(Long numberOfFiles, Long numberOfMb, BillingAccount account, Invoice invoice, TdarUser user) throws TdarActionException {
        CouponCreationAction controller = setupControllerForCoupon(account, invoice, user);
        controller.setNumberOfFiles(numberOfFiles);
        controller.setNumberOfMb(numberOfMb);
        try {
            assertEquals(Action.SUCCESS, controller.execute());
        } catch (Exception e) {
            logger.warn("{}", e);
        }
        return controller.getAccount().getCoupons().iterator().next().getCode();
    }

    public CouponCreationAction setupControllerForCoupon(BillingAccount account, Invoice invoice) throws TdarActionException {
        return setupControllerForCoupon(account, invoice,null);
    }
    
    public CouponCreationAction setupControllerForCoupon(BillingAccount account, Invoice invoice, TdarUser user) throws TdarActionException {
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.markFinal();
        genericService.saveOrUpdate(invoice);
        evictCache();
        logger.info("{}", invoice);

        assertTrue(invoice.getNumberOfFiles() > 0);
        BillingAccountController controller = null;
        if (user != null ) {
            controller = generateNewInitializedController(BillingAccountController.class, user);
        } else {
            controller = generateNewInitializedController(BillingAccountController.class);
        }
        controller.setInvoiceId(invoice.getId());
        controller.setId(account.getId());
        controller.setName("test");
        controller.prepare();
        boolean seen = false;
        controller.setServletRequest(getServletPostRequest());
        genericService.refresh(controller.getAccount());
        accountService.updateQuota(controller.getAccount(), controller.getAuthenticatedUser());
        try {
            logger.info("saving account");
            assertEquals(Action.SUCCESS, controller.save());
        } catch (Exception e) {
            logger.error("exception : {}", e);
            seen = true;
        }
        assertFalse(seen);
        CouponCreationAction controllerc = generateNewInitializedController(CouponCreationAction.class);
        if (user != null ) {
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

}
