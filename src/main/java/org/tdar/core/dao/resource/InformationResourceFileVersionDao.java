package org.tdar.core.dao.resource;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.dao.Dao.HibernateBase;

@Component
public class InformationResourceFileVersionDao extends HibernateBase<InformationResourceFileVersion> {

	public InformationResourceFileVersionDao() {
		super(InformationResourceFileVersion.class);
	}
	
}
