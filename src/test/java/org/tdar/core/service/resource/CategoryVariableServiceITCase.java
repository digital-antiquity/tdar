package org.tdar.core.service.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.CategoryType;
import org.tdar.core.bean.resource.CategoryVariable;

public class CategoryVariableServiceITCase extends AbstractIntegrationTestCase {
    @Autowired
    private CategoryVariableService categoryVariableService;

    // make sure a category doesn't include itself in getChildren() (i.e. a parent records was loaded w/ it's parent_id pointing to itself)
    @Test
    public void testCategoriesNotInSubcategories() {
        List<CategoryVariable> parents = categoryVariableService.findAllCategories();
        assertTrue("parents list should have items", CollectionUtils.isNotEmpty(parents));

        for (CategoryVariable parent : parents) {
            assertFalse("parent categoryVariable can't contain itself as a child:" + parent, parent.getChildren().contains(parent));
        }

    }

    @Test
    public void testFindAllCategories() {
        assertTrue("cat list shouldn't be empty", CollectionUtils.isNotEmpty(categoryVariableService.findAllCategories()));
    }

    @Test
    public void testFindAllSorted() {
        List<CategoryVariable> expectedSortedCategories = new ArrayList<CategoryVariable>(categoryVariableService.findAllCategories());
        Collections.sort(expectedSortedCategories); // have Collections.sort() sort the first list, confirm that it matches the list sorted by our service
        assertEquals("list should be sorted", expectedSortedCategories, categoryVariableService.findAllCategoriesSorted());

    }

    @Test
    // other checks on the category variables
    public void testFindAllCategories2() {
        List<CategoryVariable> parents = categoryVariableService.findAllCategories();
        List<CategoryVariable> allChildren = new ArrayList<CategoryVariable>();
        List<CategoryVariable> parentsAndChildren = new ArrayList<CategoryVariable>();
        for (CategoryVariable parent : parents) {
            allChildren.addAll(parent.getChildren());
            parentsAndChildren.add(parent);
            parentsAndChildren.addAll(parent.getChildren());
            assertEquals(CategoryType.CATEGORY, parent.getType());
        }
        for (CategoryVariable child : allChildren) {
            assertEquals(CategoryType.SUBCATEGORY, child.getType());
        }

        List<CategoryVariable> allCategories = categoryVariableService.findAll();
        for (CategoryVariable cat : allCategories) {
            assertTrue("list should contain category:" + cat, parentsAndChildren.contains(cat));
        }
        for (CategoryVariable cat : parentsAndChildren) {
            assertTrue("list should contain category:" + cat, allCategories.contains(cat));
        }
    }

    @Test
    // based on our test data load, some categoryVariables should have at least one related coding sheet, related ontology, and/or synonym
    public void testRelatedEntities() {
        List<CategoryVariable> allCats = categoryVariableService.findAll();
        allCats.get(0).getSynonyms().add("test");
        allCats.get(0).getSynonyms().add("other");
        allCats.get(0).getSynonyms().add("another");
        categoryVariableService.saveOrUpdate(allCats.get(0));
        List<String> synonyms = new ArrayList<String>();
        allCats = categoryVariableService.findAll();
        logger.info("{}", allCats);
        for (CategoryVariable cat : allCats) {
            assertNotNull(cat.getSortedSynonyms());

            synonyms.addAll(cat.getSortedSynonyms());
        }
        logger.debug("related synonyms:" + synonyms);
        assertTrue("we should have at least one related synonym", synonyms.size() > 0);
    }
}
