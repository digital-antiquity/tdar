package org.tdar.core.dao.citation;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.dao.Dao;

@Component
public class RelatedComparativeCollectionDao extends Dao.HibernateBase<RelatedComparativeCollection>
{

	public RelatedComparativeCollectionDao()
	{
		super(RelatedComparativeCollection.class);
	}

}
