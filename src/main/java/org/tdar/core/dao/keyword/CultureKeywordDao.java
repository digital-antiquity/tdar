package org.tdar.core.dao.keyword;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.CultureKeyword;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class CultureKeywordDao extends HierarchicalKeywordDao<CultureKeyword> {

    public CultureKeywordDao() {
        super(CultureKeyword.class);
    }
    
}
