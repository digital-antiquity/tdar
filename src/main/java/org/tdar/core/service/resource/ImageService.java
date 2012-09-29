package org.tdar.core.service.resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.dao.resource.ImageDao;

/**
 * $Id$
 * 
 * Service layer access for Image entities.
 * 
 * FIXME: is this needed?  Delete and replace with GenericService if not.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Service
@Transactional
public class ImageService extends AbstractInformationResourceService<Image, ImageDao> {

}
