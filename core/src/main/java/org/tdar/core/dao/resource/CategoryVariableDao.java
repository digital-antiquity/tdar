package org.tdar.core.dao.resource;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CategoryType;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.dao.base.Dao;

/**
 * $Id$
 * 
 * Provides DAO access for domain context variables in the master ontology.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class CategoryVariableDao extends Dao.HibernateBase<CategoryVariable> {

    public CategoryVariableDao() {
        super(CategoryVariable.class);
    }

    public List<CategoryVariable> findAllCategories() {
        DetachedCriteria criteria = getDetachedCriteria().add(Restrictions.eq("type", CategoryType.CATEGORY));
        return findByCriteria(criteria);
    }

    public List<CategoryVariable> findAllCategoriesSorted() {
        DetachedCriteria criteria = getOrderedDetachedCriteria().add(Restrictions.eq("type", CategoryType.CATEGORY));
        return findByCriteria(criteria);
    }

    @Override
    protected String getDefaultOrderingProperty() {
        return "name";
    }
}
