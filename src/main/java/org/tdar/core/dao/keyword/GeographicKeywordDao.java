package org.tdar.core.dao.keyword;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.GeographicKeyword;

/**
 * $Id$
 *  
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class GeographicKeywordDao extends KeywordDao<GeographicKeyword> {
    
    public GeographicKeywordDao() {
        super(GeographicKeyword.class);
    }

}
