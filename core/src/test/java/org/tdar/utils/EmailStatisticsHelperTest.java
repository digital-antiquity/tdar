package org.tdar.utils;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.service.processes.charts.ChartGenerator;
import org.tdar.core.service.processes.charts.TdarPieChart;


public class EmailStatisticsHelperTest extends AbstractIntegrationTestCase{
	
	private EmailStatisticsHelper statsHelper = new EmailStatisticsHelper();
	
	@Test
	public void testBillingAccountResources(){
		
		createAndSaveNewUser();
		
		Long id = 418L;
		BillingAccount billingAccount = genericService.find(BillingAccount.class, id);
		assertNotNull(billingAccount);
		
		logger.debug("Resource count is {}",billingAccount.getResources().size());
		assertNotNull(billingAccount.getResources());
		
		Map<String, Number> data = statsHelper.generateUserResourcesPieChartData(billingAccount);
		
		logger.debug("Data is : {}",data);
		
		TdarPieChart pieChart = new TdarPieChart("Tdar Pie Chart", 100, 100, "pie_chart", data);
		
		ChartGenerator chartGenerator = new ChartGenerator();
		chartGenerator.execute(pieChart);
		
	}
}
