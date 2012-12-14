package org.tdar.core.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.billing.ResourceEvaluator;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.dao.external.payment.PaymentMethod;

public class AccountITCase extends AbstractIntegrationTestCase {

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
    public void testAccountCanAddResource() {
        // BillingActivityModel model = new BillingActivityModel();
        // ResourceEvaluator re = new ResourceEvaluator(model);
        // model.setCountingResources(true);
        // assertFalse(re.accountHasMinimumForNewResource(new Account()));
        // model.setCountingResources(false);
        // assertTrue(re.accountHasMinimumForNewResource(new Account()));
    }

    @Test
    @Rollback
    public void testAccountUpdateQuota() {
        // BillingActivityModel model = new BillingActivityModel();
        // ResourceEvaluator re = new ResourceEvaluator(model);
        // model.setCountingResources(true);
        // assertFalse(re.accountHasMinimumForNewResource(new Account()));
        // model.setCountingResources(false);
        // assertTrue(re.accountHasMinimumForNewResource(new Account()));
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
        logger.info("su {}", re.getSpaceUsed());

        assertEquals(3, re.getResourcesUsed());
        assertEquals(3, re.getFilesUsed());
        // WARN: brittle...
        assertEquals(11687168, re.getSpaceUsed());

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
        assertEquals(6L, invoice.getTotalSpace().longValue());
        assertEquals(222.2, invoice.getCalculatedCost().floatValue(), 1);
        assertEquals(null, invoice.getTotal());

        // account is empty because invoice is not finalized
        assertEquals(0L, account.getTotalNumberOfSpace().longValue());
        assertEquals(0L, account.getTotalNumberOfResources().longValue());
        assertEquals(0L, account.getTotalNumberOfFiles().longValue());

        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.finalize();
        assertEquals(222.2, invoice.getTotal().floatValue(), 1);
        assertEquals(4L, account.getTotalNumberOfFiles().longValue());
        assertEquals(2L, account.getTotalNumberOfResources().longValue());
        assertEquals(6L, account.getTotalNumberOfSpace().longValue());

    }

}
