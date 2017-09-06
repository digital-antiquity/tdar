package org.tdar.core.service.resource;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CategoryVariable;

public interface CategoryVariableService {

    /*
     * Returns all category variables in unknown order
     */
    List<CategoryVariable> findAllCategories();

    /*
     * Returns all category variables sorted by Id
     */
    List<CategoryVariable> findAllCategoriesSorted();

    List<CategoryVariable> findAll();

    CategoryVariable find(Long categoryId);

}