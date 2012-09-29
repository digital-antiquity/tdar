package org.tdar.core.dao.keyword;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.SiteTypeKeyword;

/**
 * $Id$
 * 
 * @version $Revision$
 */
@Component
public class SiteTypeKeywordDao extends HierarchicalKeywordDao<SiteTypeKeyword> {

    public SiteTypeKeywordDao() {
        super(SiteTypeKeyword.class);
    }
    
}
