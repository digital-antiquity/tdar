package org.tdar.core.dao.keyword;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.SiteNameKeyword;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class SiteNameKeywordDao extends KeywordDao<SiteNameKeyword> {

    public SiteNameKeywordDao() {
        super(SiteNameKeyword.class);
    }
}
