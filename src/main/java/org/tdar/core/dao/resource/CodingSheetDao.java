/**
 * 
 */
package org.tdar.core.dao.resource;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingSheet;

/**
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 * @latest $Id$
 */
@Component
public class CodingSheetDao extends ResourceDao<CodingSheet> {

    public CodingSheetDao() {
        super(CodingSheet.class);
    }

}
