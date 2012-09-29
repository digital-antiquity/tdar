package org.tdar.core.dao.keyword;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.tdar.core.bean.keyword.HierarchicalKeyword;

public abstract class HierarchicalKeywordDao<K extends HierarchicalKeyword<K>> extends KeywordDao<K> {

    public HierarchicalKeywordDao(Class<K> keywordClass) {
	super(keywordClass);
    }

    public List<K> findAllDescendants(K keyword) {
	String index = keyword.getIndex();
	if (StringUtils.isBlank(index)) return Collections.emptyList();
	index += ".%";
	DetachedCriteria criteria = getDetachedCriteria();
	criteria.add(Restrictions.ilike("index", index));
	return findByCriteria(criteria);
    }

}
