package org.tdar.search.query;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.ResourceType;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 *
 */
public class ResourceTypeQueryPart implements QueryPart {
	
	List<String> resourceTypeLimits;
	
	public ResourceTypeQueryPart() {
		resourceTypeLimits = new ArrayList<String>();
	}
	
	public void addResourceTypeLimit(ResourceType type) {
		if (resourceTypeLimits == null) resourceTypeLimits = new ArrayList<String>();
		resourceTypeLimits.add(type.toString());
	}

	@Override
	public String generateQueryString() {
		if (resourceTypeLimits.isEmpty()) return "";
		StringBuilder rq = new StringBuilder();
		for (String rtLimit : resourceTypeLimits) {
			if (rq.length() > 1) rq.append(" OR ");
			rq.append("resourceType:" + rtLimit);
		}
		return rq.toString();
	}

}
