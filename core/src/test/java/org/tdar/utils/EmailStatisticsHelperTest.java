package org.tdar.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;

public class EmailStatisticsHelperTest extends AbstractIntegrationTestCase{
	
	private EmailStatisticsHelper statsHelper = new EmailStatisticsHelper();
	
	@Test
	public void testBilligAccountResources(){
		Long id = 418L;
		BillingAccount billingAccount = genericService.find(BillingAccount.class, id);
		
		assertNotNull(billingAccount);
		
		logger.debug("Billing account: {} ", billingAccount);
		
		logger.debug("Resource count is {}",billingAccount.getResources().size());
		assertNotNull(billingAccount.getResources());
		
		
		statsHelper.generateUserResourcesPieChartData(billingAccount);
		
		
	}
}
