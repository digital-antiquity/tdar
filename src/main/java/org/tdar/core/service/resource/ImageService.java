package org.tdar.core.service.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.dao.resource.ImageDao;

/**
 * $Id$
 * 
 * Service layer access for Image entities.
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Service
@Transactional
public class ImageService extends AbstractInformationResourceService<Image, ImageDao> {

    @Autowired
    public void setDao(ImageDao dao) {
	super.setDao(dao);
    }

}
