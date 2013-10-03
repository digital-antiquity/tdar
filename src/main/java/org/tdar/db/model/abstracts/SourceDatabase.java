/**
 * 
 */
package org.tdar.db.model.abstracts;

import java.util.Set;



/**
 * Marker interface for source DBs, the input of DBConvertor.    
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
public interface SourceDatabase extends Database {
	public SourceDatabase open(final String filename);
	public Set<String> getTableNames();
	
}
