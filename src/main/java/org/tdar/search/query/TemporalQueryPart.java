package org.tdar.search.query;

import java.util.ArrayList;
import java.util.List;

import org.tdar.index.TdarIndexNumberFormatter;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 *
 */
public class TemporalQueryPart implements QueryPart {

    // FIXME: there's a possibility that lucene is not going to do what we think it's going to do when
    // binding to multiple values see TDAR-1163
    
    // FIXME: no coverage type here
	
    private static final String TEMPORAL_QUERY_FORMAT = QueryFieldNames.ACTIVE_START_DATE+":[00000000000 TO %2$s] AND "+QueryFieldNames.ACTIVE_END_DATE+":[%1$s TO 19999999999] AND "+QueryFieldNames.ACTIVE_COVERAGE_TYPE+":%3$s ";
	private List<TemporalLimit> temporalLimits;
	
	public TemporalQueryPart() {
		temporalLimits = new ArrayList<TemporalLimit>(); 
	}
	
	public void addTemporalLimit(TemporalLimit limit) {                                               
	    temporalLimits.add(limit);
	}
	
	@Override
	public String generateQueryString() {
		StringBuilder q = new StringBuilder();
		for (TemporalLimit temporalLimit : temporalLimits) {
			if (q.length() > 0) q.append(" AND ");
			q.append(
				String.format(
					TEMPORAL_QUERY_FORMAT, 
					TdarIndexNumberFormatter.format(temporalLimit.getFromYear()), 
					TdarIndexNumberFormatter.format(temporalLimit.getToYear()),
					temporalLimit.getDateType().name()
				)
			);
		}
		return q.toString();
	}

}
