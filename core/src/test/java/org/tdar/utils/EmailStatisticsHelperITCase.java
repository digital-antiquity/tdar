package org.tdar.utils;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.service.processes.charts.TdarPieChart;


public class EmailStatisticsHelperITCase extends AbstractIntegrationTestCase{
	
	private EmailStatisticsHelper statsHelper = new EmailStatisticsHelper();
	
	@Test
	@Rollback
	public void testBillingAccountResources() throws IOException{
		Long id = 1L;
		BillingAccount billingAccount = genericService.find(BillingAccount.class, id);
		assertNotNull(billingAccount);
		
		logger.debug("Resource count is {}",billingAccount.getResources().size());
		assertNotNull(billingAccount.getResources());
		
		Map<String, Number> data = statsHelper.generateUserResourcesPieChartData(billingAccount);
		
		logger.debug("Data is : {}",data);
		
		TdarPieChart pieChart = new TdarPieChart("Tdar Pie Chart", 750, 750, "pie_chart", data);
		
		pieChart.createChart();
		/*ChartGenerator chartGenerator = new ChartGenerator();
		chartGenerator.execute(pieChart);
		*/
	}
}
