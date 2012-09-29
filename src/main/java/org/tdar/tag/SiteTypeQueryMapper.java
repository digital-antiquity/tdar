package org.tdar.tag;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.tdar.core.service.SiteTypeKeywordService;

public class SiteTypeQueryMapper implements QueryMapper<SubjectType> {

	private Map<SubjectType, List<String>> tagTermMap;
	private SiteTypeKeywordService siteTypeKeywordService;

	public Map<SubjectType, List<String>> getTagTermMap() {
		return tagTermMap;
	}

	public void setTagTermMap(Map<SubjectType, List<String>> tagTermMap) {
		this.tagTermMap = tagTermMap;
	}

	public SiteTypeKeywordService getSiteTypeKeywordService() {
		return siteTypeKeywordService;
	}

	public void setSiteTypeKeywordService(
			SiteTypeKeywordService siteTypeKeywordService) {
		this.siteTypeKeywordService = siteTypeKeywordService;
	}

	@Override
	public List<String> findMappedValues(SubjectType sub) {
		List<String> vals = tagTermMap.get(sub);
		if (vals == null) return Collections.emptyList();
		return vals;
	}
	
}
