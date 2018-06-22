/**
 * 
 */
package org.tdar.db.model.abstracts;

import java.util.Set;

import org.tdar.db.Database;

/**
 * Marker interface for source DBs, the input of DBConvertor.
 * 
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
public interface SourceDatabase extends Database {
    SourceDatabase open(final String filename);

    Set<String> getTableNames();

}
