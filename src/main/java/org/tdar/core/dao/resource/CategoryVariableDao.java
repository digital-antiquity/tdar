package org.tdar.core.dao.resource;

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CategoryType;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.dao.Dao;

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
    
    /**
     * FIXME: should use interval encoding or dewey encoding in encoded_parent_ids for simpler / faster search. 
     * <p>
     * Returns all subcategories of the CategoryVariable identified by id.
     * Utilizes the encoded_parent_ids field for efficiency.  There are a few cases
     * that can occur:
     * <ul>
     * <li>only parent id</li>
     * <li>
     * </ul>
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<CategoryVariable> findAllSubcategories(Long id) {
        if (id == null) return Collections.emptyList();
        Query query = getCurrentSession().getNamedQuery(QUERY_CATEGORYVARIABLE_SUBCATEGORIES);
        query.setString("parentId", id.toString());
        query.setString("firstParent", String.format("%s.%%", id));
        query.setString("middleParent", String.format("%%.%s.%%", id));
        query.setString("lastParent", String.format("%%.%s", id));
        return (List<CategoryVariable>) query.list();
//        return findByCriteria(criteria.add(Restrictions.like("encodedParentIds", addWildCards(id.toString()))));
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
