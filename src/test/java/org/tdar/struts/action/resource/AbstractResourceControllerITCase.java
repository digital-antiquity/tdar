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
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.service.AccountService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.BillingAccountController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.ResourceCreatorProxy;

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
    AccountService accountService;

    public static void loadResourceFromId(AbstractResourceController<?> controller, Long id) throws TdarActionException {
        controller.setId(id);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        if (controller instanceof AbstractInformationResourceController) {
            ((AbstractInformationResourceController<?>) controller).loadInformationResourceProperties();
        }
    }

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

    public String createCouponForAccount(Long numberOfFiles, Long numberOfMb, Account account, Invoice invoice) {
        BillingAccountController controller = setupContrllerForCoupon(account, invoice);
        controller.setNumberOfFiles(numberOfFiles);
        controller.setNumberOfMb(numberOfMb);
        assertEquals(TdarActionSupport.SUCCESS,controller.createCouponCode());
        
        return controller.getAccount().getCoupons().iterator().next().getCode();
    }

    public BillingAccountController setupContrllerForCoupon(Account account, Invoice invoice) {
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.markFinal();
        genericService.saveOrUpdate(invoice);
        genericService.synchronize();
        logger.info("{}", invoice);
        
        assertTrue(invoice.getNumberOfFiles() > 0);
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.setInvoiceId(invoice.getId());
        controller.setId(account.getId());
        controller.setName("test");
        controller.prepare();
        boolean seen = false;
        controller.setServletRequest(getServletPostRequest());
        genericService.refresh(controller.getAccount());
        accountService.updateQuota(controller.getAccount());
        try {
            logger.info("saving account");
            assertEquals(TdarActionSupport.SUCCESS, controller.save());
        } catch (Exception e) {
            logger.error("exception : {}", e);
            seen = true;
        }
        assertFalse(seen);
        controller = generateNewInitializedController(BillingAccountController.class);
        controller.setId(account.getId());
        controller.prepare();
        controller.setQuantity(1);
        controller.setServletRequest(getServletPostRequest());
        return controller;
    }


}
