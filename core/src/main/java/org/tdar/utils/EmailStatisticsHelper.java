package org.tdar.utils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.StatsResultObject;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.StatisticsService;
import org.tdar.core.service.resource.ResourceService;

@Component
public class EmailStatisticsHelper {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StatisticsService statisticsService;

	@Autowired
	private ResourceService resourceService;

	public Map<String, Number> generateUserResourcesPieChartData(BillingAccount billingAccount) {
		Set<Resource> resources = billingAccount.getResources();
		Map<String, Number> map = new HashMap<String, Number>();
		for (Resource r : resources) {
			String mediaType = r.getResourceType().toDcmiTypeString();

			if (!map.containsKey(mediaType)) {
				map.put(mediaType, 1);
			} else {
				Integer count = (Integer) map.get(mediaType);
				count++;
				map.put(mediaType, count);
			}
		}
		return map;
	}

	public StatsResultObject getAccountStatistics(BillingAccount billingAccount, DateGranularity granularity) {
		return statisticsService.getStatsForAccount(billingAccount, MessageHelper.getInstance(), granularity);
	}

	public Map<String, Number> generateTotalViewsChartData(BillingAccount billingAccount, StatsResultObject stats) {
		/*
		 * Map<String, Map<String, Number>> map = new HashMap<String,
		 * Map<String, Number>>(); //Gets the download information.
		 * Collection<Map<String, Object>> data = stats.getObjectForJson();
		 * 
		 * for(Map<String, Object> row : data){ Map<String, Number> r = new
		 * HashMap<String, Number>(); r.put((String) row.get("date"), (Number)
		 * row.get("Views")); map.put((String) row.get("date"), r); }
		 */
		Map<String, Number> map = new LinkedHashMap<String, Number>();
		Collection<Map<String, Object>> data = stats.getObjectForJson();
		for (Map<String, Object> row : data) {
			String date = (String) row.get("date");
			Number value = (Number) row.get("Views");
			map.put(date, value);
		}

		logger.debug("Map is {}", map);
		return map;
	}

	public Map<String, Number> generateTotalDownloadsChartData(BillingAccount billingAccount, StatsResultObject stats) {
		Map<String, Number> map = new LinkedHashMap<String, Number>();
		Collection<Map<String, Object>> data = stats.getObjectForJson();
		for (Map<String, Object> row : data) {
			String date = (String) row.get("date");
			Number value = (Number) row.get("Downloads");
			map.put(date, value);
		}

		/*
		 * Collection<Map<String, Object>> data = stats.getObjectForJson();
		 * 
		 * for(Map<String, Object> row : data){ Map<String, Number> r = new
		 * HashMap<String, Number>(); r.put((String) row.get("date"), (Number)
		 * row.get("Downloads")); map.put((String) row.get("date"), r); }
		 */

		logger.debug("Map is {}", map);
		return map;
	}

	public List<Resource> getTopResources(BillingAccount billingAccount) {
		return resourceService.getMostPopularResourcesForBillingAccount(billingAccount, 10);
	}

	/**
	 * Calculate the start and end dates of the range from a set of resources.
	 * Used to determine the granularity of
	 * 
	 * @param resources
	 * @return
	 */
	public Date getStartDate(Set<Resource> resources) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(1900, 1, 1);
		Date startDate = new Date();

		for (Resource r : resources) {
			if (r.getDateCreated().before(startDate)) {
				startDate = r.getDateCreated();
			}
		}

		return startDate;
	}

	/**
	 * Compares the start date to the current date to determine the interval
	 * that should be used for date granularity. If the start date is in the
	 * current month, the granularity is by day. If it is the same year, it is
	 * by month, otherwise it is by year.
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public DateGranularity getDateGranularity(Date startDate) {
		GregorianCalendar calendar = new GregorianCalendar();

		logger.debug("Start date is {}", startDate);

		calendar.setTime(startDate);
		int minYear = calendar.get(Calendar.YEAR);
		int minMonth = calendar.get(Calendar.MONTH);

		calendar.setTime(new Date());
		int maxYear = calendar.get(Calendar.YEAR);
		int maxMonth = calendar.get(Calendar.MONTH);

		if (minYear == maxYear) {
			logger.debug("Years are the same ({})", maxYear);
			// If the stats are for the same month, then aggregate by day.
			if (minMonth == maxMonth) {
				logger.debug("Months are the same ({})", maxMonth);
				return DateGranularity.DAY;
			}
			// If the data available is for one year, then aggregate by month.
			else {
				logger.debug("Months are different");
				return DateGranularity.MONTH;
			}
		}
		// If there's more than one year of data available, then aggregate by
		// year.
		else {
			logger.debug("Years are different. {}, {}", minYear, maxYear);
			return DateGranularity.YEAR;
		}
	}

	public StatisticsService getStatisticsService() {
		return statisticsService;
	}

	public void setStatisticsService(StatisticsService statisticsService) {
		this.statisticsService = statisticsService;
	}

	public ResourceService getResourceService() {
		return resourceService;
	}

	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}
}
