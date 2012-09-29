package org.tdar.core.dao.keyword;

import java.util.List;

import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.dao.Dao;

/**
 * $Id$
 * 
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 * @param <K>
 */
public abstract class KeywordDao<K extends Keyword>  extends Dao.HibernateBase<K> {
    
    public KeywordDao(Class<K> keywordClass) {
        super(keywordClass);
    }
    
    public K findByLabel(String label) {
        // FIXME: turn this into a generic named query?
        return findByProperty("label", label);
    }
    
    public List<K> findAllByLabels(List<String> labels) {
    	return findAllFromList("label", labels);
    }
}
