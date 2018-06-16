package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.TestBillingAccountHelper;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.dao.AccountAdditionStatus;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;

@RunWith(MultipleTdarConfigurationRunner.class)
public class ResourceControllerITCase extends AbstractControllerITCase implements TestBillingAccountHelper {
    @Autowired
    BillingAccountService accountService;

    @Test
    @Rollback
    public void testFindProject() {
        @SuppressWarnings("unchecked")
        Project r = projectService.find(1L);
        logger.info("Resource: {} ", r);
        assertNotNull(r);
        r = resourceService.find(1L);
        logger.info("Resource: {} ", r);
        assertNotNull(r);
    }

    /**
     * Tests that the resource controller will trip a flag to disallow upload form to be rendered.
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws TdarActionException
     * @throws FileNotFoundException
     */
    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
    @Rollback
    public void testAvailableSpaceForBillingAccount() throws InstantiationException, IllegalAccessException, TdarActionException, FileNotFoundException {
        // Create a new billing account.
        BillingActivityModel model = new BillingActivityModel();
        updateModel(model, false, false, true);
        BillingAccount account = setupAccountWithInvoiceForSpecifiedMb(model, getUser(), 1L);
        model.setActive(true);
        model.setVersion(100); // forcing the model to be the "latest"
        genericService.saveOrUpdate(model);

        Document resource = createAndSaveDocumentWithFileAndUseDefaultUser();
        Long resourceId = resource.getId();
        Long accountId = account.getId();
        getLogger().debug("Available space is {} MB", account.getAvailableSpaceInMb());
        assertTrue("Account has available space", account.getAvailableSpaceInMb() > 0);
        // logger.info("f{} s{}", resource.getFilesUsed(), resource.getSpaceInBytesUsed());
        Long spaceUsedInBytes = account.getSpaceUsedInBytes();
        Long resourcesUsed = account.getResourcesUsed();
        Long filesUsed = account.getFilesUsed();

        assertFalse(account.getResources().contains(resource));
        resource.setAccount(account);

        AccountAdditionStatus status = accountService.updateQuota(account, resource.getSubmitter(), resource);

        genericService.refresh(account);
        assertTrue("The billing account contains the resource", account.getResources().contains(resource));
        assertEquals(AccountAdditionStatus.NOT_ENOUGH_SPACE, status);

        logger.info("{} space used in bytes ({})", account.getSpaceUsedInBytes(), spaceUsedInBytes);
        assertTrue(account.getResources().contains(resource));

        assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, resource.getStatus());
        assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, account.getStatus());

        genericService.synchronize();

        DocumentController controller = generateNewInitializedController(DocumentController.class);
        resource = null;

        controller.setId(resourceId);
        controller.prepare();
        controller.edit();
        BillingAccount billingAccount = controller.getPersistable().getAccount();

        getLogger().debug("Billing account is {}", billingAccount);
        assertNotNull(billingAccount);

        assertFalse("Controller can't upload files", controller.isAbleToUploadFiles());
        // assert the billing account is first in the controller's list.

        FileProxy proxy = controller.getFileProxies().get(0);

        // set file proxy setAction
        proxy.setAction(FileAction.DELETE);
        controller.setServletRequest(getServletPostRequest());
        assertEquals(controller.save(), TdarActionSupport.SUCCESS);
        controller = null;
        billingAccount = null;
        resource = null;

        genericService.synchronize();
        billingAccount = genericService.find(BillingAccount.class, accountId);
        resource = genericService.find(Document.class, resourceId);

        assertEquals(Status.ACTIVE, billingAccount.getStatus());
        assertEquals(resource.getStatus(), Status.ACTIVE);
    }

}
