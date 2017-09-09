package org.tdar.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.resource.stats.DateGranularity;

public class EmailStatisticsHelper {
	public Map<String, Integer> generateUserResourcesPieChartData(BillingAccount billingAccount){
		Set<Resource> resources = billingAccount.getResources();
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(Resource r: resources){
			String mediaType = r.getResourceType().toDcmiTypeString();
			
			if(!map.containsKey(mediaType)){
				map.put(mediaType, 1);
			}
			else { 
				Integer count = map.get(mediaType); 
				count++;
				map.put(mediaType, count);
			}
		}
		return map;
	}
	
	/**
	 * Calculate the start and end dates of the range from a set of resources. Used to determine the granularity of 
	 * @param resources
	 * @return
	 */
	public Date[] getStartEndDates(Set<Resource> resources){
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(1900, 1, 1);
		Date startDate = new Date();
		Date endDate = calendar.getTime();
		
		for(Resource r : resources){
			if(r.getDateCreated().before(startDate)){
				startDate = r.getDateCreated();
			}
			
			if(r.getDateCreated().after(endDate)){
				endDate = r.getDateCreated();
			}
		}
		
		return new Date[]{startDate, endDate};
	}
	
	/**
	 * Given a start date and end date, determine the granularity that statistics should aggregate to.
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public DateGranularity getDateGranularity(Date startDate, Date endDate){
		GregorianCalendar calendar = new GregorianCalendar();

		calendar.setTime(startDate);
		int minYear  = calendar.get(Calendar.YEAR);
		int minMonth = calendar.get(Calendar.MONTH);
		
		calendar.setTime(endDate);
		int maxYear  = calendar.get(Calendar.YEAR);
		int maxMonth = calendar.get(Calendar.MONTH);
		
		
		if(minYear == maxYear){
			//If the stats are for the same month, then aggregate by day.
			if(minMonth==maxMonth){
				return DateGranularity.DAY;
			}
			//If the data available is for one year, then aggregate by month.
			else {
				return DateGranularity.MONTH;
			}
		}
		//If there's more than one year of data available, then aggregate by year. 
		else{
			return DateGranularity.YEAR;
		}
	}
}
