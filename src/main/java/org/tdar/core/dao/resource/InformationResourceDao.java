package org.tdar.core.dao.resource;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;

/**
 * $Id$ 
 * 
 * Generic InformationResource DAO for finding generic InformationResources.  
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component("informationResourceDao")
public class InformationResourceDao extends ResourceDao<InformationResource> {

    public InformationResourceDao() {
	super(InformationResource.class);
    }

    public InformationResourceFile findFileByFilename(InformationResource resource, String filename) {
	Query query = getCurrentSession().getNamedQuery(QUERY_INFORMATIONRESOURCE_FIND_BY_FILENAME);
	query.setString("filename", filename).setEntity("resource", resource);
	return (InformationResourceFile) query.uniqueResult();
    }
}
