package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.service.resource.CategoryVariableService;
import org.tdar.struts.action.resource.ajax.AjaxController;

public class AjaxControllerITCase extends AbstractAdminControllerITCase {
    private AjaxController controller;

    @Autowired
    private CategoryVariableService categoryVariableService;

    @Before
    public void setup() {
        controller = generateNewInitializedController(AjaxController.class);
    }

    @Test
    public void testColumnMetadataSubcategories() {
        CategoryVariable categoryVariable = categoryVariableService.findAllCategories().iterator().next();
        controller.setCategoryVariableId(categoryVariable.getId());
        controller.columnMetadataSubcategories();
        List<CategoryVariable> expectedSubcategories = new ArrayList<CategoryVariable>(categoryVariable.getSortedChildren());
        assertEquals("categories should match", categoryVariable, controller.getCategoryVariable());
        assertEquals("subcat lists should match", expectedSubcategories, controller.getSubcategories());
    }

}
