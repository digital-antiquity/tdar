package org.tdar.core.bean;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Account.AccountAdditionStatus;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.billing.ResourceEvaluator;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.service.AccountService;
import org.tdar.core.service.processes.SetupBillingAccountsProcess;
import org.tdar.struts.data.FileProxy;

public class AccountITCase extends AbstractIntegrationTestCase {

    @Autowired
    AccountService accountService;

    @Autowired
    SetupBillingAccountsProcess accountProcess;

    @Test
    @Rollback
    public void testBillingAccountSetup() throws InstantiationException, IllegalAccessException {
        Document document = generateInformationResourceWithFileAndUser();
        accountProcess.process(document.getSubmitter());
        genericService.synchronize();
    }

    @Test
    @Rollback
    public void testResourceEvaluatorCanCreateResource() {
        BillingActivityModel model = new BillingActivityModel();
        ResourceEvaluator re = new ResourceEvaluator(model);
        updateModel(model, true, false, false);
        Account account = new Account();
        assertFalse(re.accountHasMinimumForNewResource(account, null));

        updateModel(model, true, false, false);
        assertTrue(re.accountHasMinimumForNewResource(account, ResourceType.PROJECT));

        updateModel(model, false, true, false);
        assertFalse(re.accountHasMinimumForNewResource(account, null));

        updateModel(model, false, false, true);
        assertFalse(re.accountHasMinimumForNewResource(account, null));

        updateModel(model, false, false, false);
        assertTrue(re.accountHasMinimumForNewResource(account, null));
    }

    public void updateModel(BillingActivityModel model, boolean resources, boolean files, boolean space) {
        model.setCountingResources(resources);
        model.setCountingFiles(files);
        model.setCountingSpace(space);
    }

    @Test
    @Rollback
    public void testAccountCanAddResourceDefaultCases() throws InstantiationException, IllegalAccessException {
        BillingActivityModel model = new BillingActivityModel();
        ResourceEvaluator re = new ResourceEvaluator(model);
        Account account = new Account();
        Document resource = generateInformationResourceWithFileAndUser();
        re.evaluateResources(resource);
        updateModel(model, true, false, false);
        assertEquals(AccountAdditionStatus.NOT_ENOUGH_RESOURCES, account.canAddResource(re));
        updateModel(model, false, true, false);
        logger.info("af: {} , ref: {}", account.getAvailableNumberOfFiles(), re.getFilesUsed());
        assertEquals(AccountAdditionStatus.NOT_ENOUGH_FILES, account.canAddResource(re));
        updateModel(model, false, false, true);
        assertEquals(AccountAdditionStatus.NOT_ENOUGH_SPACE, account.canAddResource(re));
        updateModel(model, false, false, false);
        assertEquals(AccountAdditionStatus.CAN_ADD_RESOURCE, account.canAddResource(re));
        // model.setCountingResources(true);
        // assertFalse(re.accountHasMinimumForNewResource(new Account()));
        // model.setCountingResources(false);
        // assertTrue(re.accountHasMinimumForNewResource(new Account()));
    }

    @Test
    @Rollback
    public void testAccountCanAddResource() throws InstantiationException, IllegalAccessException {
        BillingActivityModel model = new BillingActivityModel();
        ResourceEvaluator re = new ResourceEvaluator(model);
        Document resource = generateInformationResourceWithFileAndUser();
        re.evaluateResources(resource);
        // public BillingActivity(String name, Float price, Integer numHours, Long numberOfResources, Long numberOfFiles, Long numberOfMb) {
        Account account = setupAccountWithInvoiceForOneResource(model);
        updateModel(model, true, false, false);
        assertEquals(AccountAdditionStatus.CAN_ADD_RESOURCE, account.canAddResource(re));
        updateModel(model, false, true, false);

        /* add one file */
        logger.info("af: {} , ref: {}", account.getAvailableNumberOfFiles(), re.getFilesUsed());
        account = setupAccountWithInvoiceForOneFile(model);
        assertEquals(AccountAdditionStatus.CAN_ADD_RESOURCE, account.canAddResource(re));

        /* add 5 MB */
        updateModel(model, false, false, true);
        account = setupAccountWithInvoiceFor6Mb(model);
        assertEquals(AccountAdditionStatus.CAN_ADD_RESOURCE, account.canAddResource(re));
        updateModel(model, false, false, false);
        assertEquals(AccountAdditionStatus.CAN_ADD_RESOURCE, account.canAddResource(re));
    }

    public Account setupAccountWithInvoiceFor6Mb(BillingActivityModel model) {
        Account account = new Account();
        BillingActivity activity = new BillingActivity("6 mb", 10f, 0, 0L, 0L, 6L, model);
        Invoice invoice = initAccount(account, activity);
        genericService.saveOrUpdate(account);
        return account;
    }

    public Account setupAccountWithInvoiceForOneFile(BillingActivityModel model) {
        Account account = new Account();
        Invoice invoice = initAccount(account, new BillingActivity("1 file", 10f, 0, 0L, 1L, 0L, model));
        genericService.saveOrUpdate(account);
        return account;
    }

    public Account setupAccountWithInvoiceForOneResource(BillingActivityModel model) {
        Account account = new Account();
        Invoice invoice = initAccount(account, new BillingActivity("1 resource", 10f, 0, 1L, 0L, 0L, model));
        /* add one resource */
        // account.resetTransientTotals();
        genericService.saveOrUpdate(account);
        return account;
    }

    public Account setupAccountWithInvoiceSomeResourcesAndSpace(BillingActivityModel model) {
        Account account = new Account();
        Invoice invoice = initAccount(account, new BillingActivity("10 resource", 100f, 0, 10L, 10L, 100L, model));
        /* add one resource */
        // account.resetTransientTotals();
        genericService.saveOrUpdate(account);
        return account;
    }

    private Invoice initAccount(Account account, BillingActivity activity) {
        account.markUpdated(getUser());
        Invoice invoice = new Invoice();
        invoice.markUpdated(getUser());
        account.getInvoices().add(invoice);
        genericService.saveOrUpdate(activity.getModel());
        genericService.saveOrUpdate(activity);
        invoice.getItems().add(new BillingItem(activity, 1));
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.markFinal();
        genericService.saveOrUpdate(invoice);
        genericService.saveOrUpdate(invoice.getItems());
        return invoice;
    }

    @Test
    @Rollback
    public void testAccountUpdateQuotaValid() throws InstantiationException, IllegalAccessException {
        BillingActivityModel model = new BillingActivityModel();
        updateModel(model, false, false, true);
        Account account = setupAccountWithInvoiceFor6Mb(model);
        ResourceEvaluator re = new ResourceEvaluator(model);
        Document resource = generateInformationResourceWithFileAndUser();
        re.evaluateResources(resource);
        Long spaceUsedInBytes = account.getSpaceUsedInBytes();
        Long resourcesUsed = account.getResourcesUsed();
        Long filesUsed = account.getFilesUsed();
        assertFalse(account.getResources().contains(resource));
        logger.info("{}", re);
        List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource);
        account.updateQuotas(re, resources);
        account.getResources().add(resource);
        assertTrue(account.getResources().contains(resource));
        assertEquals(spaceUsedInBytes.longValue() + re.getSpaceUsedInBytes(), account.getSpaceUsedInBytes().longValue());
        assertEquals(resourcesUsed.longValue() + re.getResourcesUsed(), account.getResourcesUsed().longValue());
        assertEquals(filesUsed.longValue() + re.getFilesUsed(), account.getFilesUsed().longValue());
    }

    @Test
    @Rollback
    public void testAccountUpdateQuotaOverdrawn() throws InstantiationException, IllegalAccessException {
        BillingActivityModel model = new BillingActivityModel();
        updateModel(model, true, true, true);
        model.setActive(true);
        model.setVersion(100); // forcing the model to be the "latest"
        genericService.saveOrUpdate(model);
        Account account = setupAccountForPerson(getUser());
        Document resource = generateInformationResourceWithFileAndUser();
        logger.info("f{} s{}", resource.getFilesUsed(), resource.getSpaceInBytesUsed());
        Long spaceUsedInBytes = account.getSpaceUsedInBytes();
        Long resourcesUsed = account.getResourcesUsed();
        Long filesUsed = account.getFilesUsed();

        assertFalse(account.getResources().contains(resource));

        AccountAdditionStatus status = accountService.updateQuota(account, resource);
        genericService.refresh(account);
        assertEquals(AccountAdditionStatus.NOT_ENOUGH_SPACE, status);
        logger.info("{} space used in bytes ({})", account.getSpaceUsedInBytes(), spaceUsedInBytes);
        assertTrue(account.getResources().contains(resource));
        assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, resource.getStatus());

        assertNotEquals(spaceUsedInBytes.longValue(), account.getSpaceUsedInBytes().longValue());
        assertEquals(resourcesUsed.longValue(), account.getResourcesUsed().longValue());
        assertNotEquals(filesUsed.longValue(), account.getFilesUsed().longValue());

        assertEquals(spaceUsedInBytes.longValue() + resource.getSpaceInBytesUsed(), account.getSpaceUsedInBytes().longValue());
        // assertEquals(resourcesUsed.longValue() + resource.getResourcesUsed(), account.getResourcesUsed().longValue());
        assertEquals(filesUsed.longValue() + resource.getFilesUsed(), account.getFilesUsed().longValue());
    }
    
    @Test
    @Rollback
    public void testAccountUpdateQuotaOverdrawnMinorEdit() throws InstantiationException, IllegalAccessException {
        BillingActivityModel model = new BillingActivityModel();
        updateModel(model, false, true, false);
        model.setActive(true);
        model.setVersion(100); // forcing the model to be the "latest"
        genericService.saveOrUpdate(model);
        Account account = setupAccountWithInvoiceForOneFile(model);
        Document resource = generateInformationResourceWithFileAndUser();
        Document resource2 = generateInformationResourceWithFileAndUser();
        logger.info("f{} s{}", resource.getFilesUsed(), resource.getSpaceInBytesUsed());

        AccountAdditionStatus statusOk = accountService.updateQuota(account, resource);
        AccountAdditionStatus status = accountService.updateQuota(account, resource2);
        Resource ok = null;
        Resource flagged = null;
        genericService.refresh(account);
        for (Resource res : account.getResources()) {
            if (res.getStatus() == Status.ACTIVE) {
                ok = res;
            }
            if (res.getStatus() == Status.FLAGGED_ACCOUNT_BALANCE) {
                flagged = res;
            }
        }
        assertNotNull(ok);
        assertNotNull(flagged);
        assertEquals(AccountAdditionStatus.CAN_ADD_RESOURCE, statusOk);
        assertEquals(AccountAdditionStatus.NOT_ENOUGH_SPACE, status);
        assertEquals(Status.ACTIVE, ok.getStatus());
        assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, flagged.getStatus());
        ok.setTitle("new title");
        accountService.updateQuota(account, ok);
        assertEquals(Status.ACTIVE, ok.getStatus());
        addFileToResource((InformationResource)ok, new File(TestConstants.TEST_DOCUMENT_DIR, "/t1/test.pdf"));
        accountService.updateQuota(account, ok);
        assertEquals(Status.FLAGGED_ACCOUNT_BALANCE, ok.getStatus());
        
    }

    @Test
    @Rollback
    public void testIncrementalChangeEvaluation() throws InstantiationException, IllegalAccessException, IOException {
        BillingActivityModel model = new BillingActivityModel();
        updateModel(model, true, true, true);
        ResourceEvaluator re = new ResourceEvaluator(model);
        ResourceEvaluator re3 = new ResourceEvaluator(model);
        Document doc = createAndSaveNewInformationResource(Document.class);
        re.evaluateResources(doc);
        ResourceEvaluator re2 = new ResourceEvaluator(model);
        addFileToResource(doc, new File(TestConstants.TEST_DOCUMENT_DIR, "/t1/test.pdf"));
        InformationResourceFile file = doc.getInformationResourceFiles().iterator().next();
        re2.evaluateResources(doc);
        re2.subtract(re);
        logger.info(re2.toString());
        assertEquals(0, re2.getResourcesUsed());
        assertEquals(1, re2.getFilesUsed());
        assertEquals(114875, re2.getSpaceUsedInBytes());
        re3.evaluateResources(doc);
        FileProxy proxy = new FileProxy("test.pdf", new File(TestConstants.TEST_DOCUMENT_DIR, "/t2/test.pdf"), VersionType.UPLOADED_ARCHIVAL);
        proxy.setAction(FileAction.REPLACE);
        proxy.setFileId(file.getId());
        informationResourceService.processFileProxy(doc, proxy);
        ResourceEvaluator re4 = new ResourceEvaluator(model);
        logger.info("files {} ", doc.getInformationResourceFiles());
        re4.evaluateResources(doc);
        re4.subtract(re3);
        logger.info("re {} ", re4);
        assertEquals(0, re4.getResourcesUsed());
        assertEquals(0, re4.getFilesUsed());
        assertEquals(0, re4.getSpaceUsedInBytes());

        logger.info(re3.toString());
    }

    @Test
    @Rollback
    public void testDeletedRemovesFromAccount() throws InstantiationException, IllegalAccessException, IOException {
        BillingActivityModel model = accountService.getLatestActivityModel();
        Account account = setupAccountWithInvoiceSomeResourcesAndSpace(model);
        Document doc = createAndSaveNewInformationResource(Document.class);
        addFileToResource(doc, new File(TestConstants.TEST_DOCUMENT_DIR, "/t1/test.pdf"));
        accountService.getResourceEvaluator(doc);
        genericService.saveOrUpdate(doc);
        Long availableSpaceInMb = account.getAvailableSpaceInMb();
        Long availableNumberOfFiles = account.getAvailableNumberOfFiles();
        accountService.updateQuota(account, doc);
        doc.setStatus(Status.DELETED);
        logger.info("m:{} f:{}", account.getAvailableSpaceInMb(), account.getAvailableNumberOfFiles());
        genericService.saveOrUpdate(doc);
        accountService.updateQuota(account, doc);
        assertEquals(availableNumberOfFiles, account.getAvailableNumberOfFiles());
        assertEquals(availableSpaceInMb, account.getAvailableSpaceInMb());
        logger.info("m:{} f:{}", account.getAvailableSpaceInMb(), account.getAvailableNumberOfFiles());
    }

    @Test
    @Rollback
    public void testResourceEvaluatorEvaluateResourcesByType() throws InstantiationException, IllegalAccessException {
        BillingActivityModel model = new BillingActivityModel();
        ResourceEvaluator re = new ResourceEvaluator(model);
        model.setCountingResources(true);
        assertFalse(re.accountHasMinimumForNewResource(new Account(), null));
        Image img = new Image();
        InformationResource irfile = generateInformationResourceWithFileAndUser();
        InformationResource irfile2 = generateInformationResourceWithFileAndUser();
        InformationResourceFile irfProcessed = new InformationResourceFile(FileStatus.PROCESSED, null);
        img.getInformationResourceFiles().addAll(
                Arrays.asList(new InformationResourceFile(FileStatus.DELETED, null), irfProcessed,
                        irfProcessed, irfProcessed));
        img.setStatus(null);
        List<Resource> resources = Arrays.asList(irfile, irfile2, new Project(), new Ontology(), new CodingSheet(), img);
        re.evaluateResources(resources);

        logger.info("ru {}", re.getResourcesUsed());
        logger.info("fu {}", re.getFilesUsed());
        logger.info("su {}", re.getSpaceUsedInBytes());

        assertEquals(3, re.getResourcesUsed());
        assertEquals(3, re.getFilesUsed());
        // WARN: brittle...
        assertEquals(11687168, re.getSpaceUsedInBytes());

    }

    @Test
    @Rollback
    public void testInvoiceBillingItemIncrements() {
        BillingActivityModel model = new BillingActivityModel();
        List<BillingItem> items = new ArrayList<BillingItem>();
        genericService.saveOrUpdate(model);
        items.add(new BillingItem(new BillingActivity("1 hour", 10f, 10, 0L, 0L, 0L, model), 2));
        // public BillingActivity(String name, Float price, Integer numHours, Long numberOfResources, Long numberOfFiles, Long numberOfMb) {
        items.add(new BillingItem(new BillingActivity("1 resource", 1f, 0, 1L, 0L, 0L, model), 2));
        items.add(new BillingItem(new BillingActivity("1 file", 100f, 0, 0L, 2L, 0L, model), 2));
        items.add(new BillingItem(new BillingActivity("1 mb", .1f, 0, 0L, 0L, 3L, model), 2));
        Invoice invoice = new Invoice(getUser(), PaymentMethod.INVOICE, 10L, 0L, items);
        Account account = new Account("my account");
        account.getInvoices().add(invoice);
        // genericService.saveOrUpdate(invoice);

        assertEquals(4L, invoice.getTotalNumberOfFiles().longValue());
        assertEquals(2L, invoice.getTotalResources().longValue());
        assertEquals(6L, invoice.getTotalSpaceInMb().longValue());
        assertEquals(222.2, invoice.getCalculatedCost().floatValue(), 1);
        assertEquals(null, invoice.getTotal());

        // account is empty because invoice is not finalized
        assertEquals(0L, account.getTotalSpaceInMb().longValue());
        assertEquals(0L, account.getTotalNumberOfResources().longValue());
        assertEquals(0L, account.getTotalNumberOfFiles().longValue());

        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.markFinal();
        assertEquals(222.2, invoice.getTotal().floatValue(), 1);
        assertEquals(4L, account.getTotalNumberOfFiles().longValue());
        assertEquals(2L, account.getTotalNumberOfResources().longValue());
        assertEquals(6L, account.getTotalSpaceInMb().longValue());

    }

}
