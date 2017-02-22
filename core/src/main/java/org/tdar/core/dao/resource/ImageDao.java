package org.tdar.core.dao.resource;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Image;

/**
 * $Id$
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component
public class ImageDao extends ResourceDao<Image> {

    public ImageDao() {
        super(Image.class);
    }

}
