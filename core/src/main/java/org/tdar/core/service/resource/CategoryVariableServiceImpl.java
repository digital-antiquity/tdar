package org.tdar.core.service.resource;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.dao.resource.CategoryVariableDao;
import org.tdar.core.service.ServiceInterface;

/**
 * $Id$
 * 
 * Provides access to the category variables that can be associated with a given column in a CodingSheet.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Service
public class CategoryVariableServiceImpl  extends ServiceInterface.TypedDaoBase<CategoryVariable, CategoryVariableDao> implements CategoryVariableService {

    /*
     * Returns all category variables in unknown order
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.CategoryVariableService#findAllCategories()
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryVariable> findAllCategories() {
        return getDao().findAllCategories();
    }

    /*
     * Returns all category variables sorted by Id
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.CategoryVariableService#findAllCategoriesSorted()
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryVariable> findAllCategoriesSorted() {
        return getDao().findAllCategoriesSorted();
    }

}
