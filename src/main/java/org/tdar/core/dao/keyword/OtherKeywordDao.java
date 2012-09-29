package org.tdar.core.dao.keyword;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.OtherKeyword;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class OtherKeywordDao extends KeywordDao<OtherKeyword> {

    public OtherKeywordDao() {
        super(OtherKeyword.class);
    }
}
