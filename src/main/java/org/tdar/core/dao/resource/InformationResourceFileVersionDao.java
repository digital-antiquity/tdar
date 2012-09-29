package org.tdar.core.dao.resource;



import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.dao.Dao.HibernateBase;
import org.tdar.core.dao.TdarNamedQueries;

@Component
public class InformationResourceFileVersionDao extends HibernateBase<InformationResourceFileVersion> {

	public InformationResourceFileVersionDao() {
		super(InformationResourceFileVersion.class);
	}
	
	public int deleteDerivatives(InformationResourceFileVersion version) {
	    Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_DELETE_INFORMATION_RESOURCE_FILE_DERIVATIVES);
	    query.setParameter("informationResourceFileId", version.getInformationResourceFileId());
	    query.setParameterList("derivativeFileVersionTypes", VersionType.getDerivativeVersionTypes());
	    return query.executeUpdate();
	}
	
}
