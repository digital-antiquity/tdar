package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.TdarUser;

public class BookmarkedResourceITCase extends AbstractIntegrationTestCase {

    private List<BookmarkedResource> list = new ArrayList<BookmarkedResource>();
    private HashSet<BookmarkedResource> set = new HashSet<BookmarkedResource>();
    private Map<BookmarkedResource, Integer> map = new HashMap<BookmarkedResource, Integer>();

    private Dataset dataset;
    // using arrays to avoid contamination with hashcode/equals
    private TdarUser[] savedPersons;

    private Dataset getDataset() {
        if (dataset == null) {
            dataset = createAndSaveNewDataset();
        }
        return dataset;
    }

    private BookmarkedResource[] createNewUnsavedBookmarkedResources() {
        return createNewUnsavedBookmarkedResources(10);
    }

    // using arrays to avoid contamination with hashcode/equals
    private BookmarkedResource[] createNewUnsavedBookmarkedResources(int numberOfBookmarks) {
        Dataset dataset = getDataset();
        BookmarkedResource[] array = new BookmarkedResource[numberOfBookmarks];
        savedPersons = new TdarUser[numberOfBookmarks];
        for (int i = 0; i < array.length; i++) {
            BookmarkedResource br = new BookmarkedResource();
            TdarUser newPerson = createAndSaveNewPerson("test" + i + "@example.com", "" + i);
            savedPersons[i] = newPerson;
            br.setPerson(newPerson);
            br.setResource(dataset);
            // dataset.getBookmarks().add(br);
            array[i] = br;
        }
        return array;
    }

    private void saveAll(BookmarkedResource... resources) {
        genericService.save(Arrays.asList(resources));
    }

    @Test
    @Rollback
    public void testTransientCollections() {
        BookmarkedResource[] array = createNewUnsavedBookmarkedResources();
        verifyCollectionOperations(array);
    }

    public TdarUser[] getSavedPersons() {
        for (int i = 0; i < savedPersons.length; i++) {
            savedPersons[i] = entityService.findUserByEmail(savedPersons[i].getEmail());
        }
        return savedPersons;
    }

    private void verifyCollectionOperations(BookmarkedResource[] array) {
        // test transients
        for (int i = 0; i < array.length; i++) {
            set.add(array[i]);
            list.add(array[i]);
            map.put(array[i], Integer.valueOf(i));
        }
        for (BookmarkedResource br : array) {
            assertTrue(set.contains(br));
            assertTrue(list.contains(br));
            assertTrue(map.containsKey(br));
            assertEquals(map.get(br), Integer.valueOf(list.indexOf(br)));
        }
        // check removal
        for (int index = 0; index < array.length; index++) {
            BookmarkedResource br = array[index];
            assertTrue(set.remove(br));
            assertTrue(list.remove(br));
            assertEquals(map.remove(br), Integer.valueOf(index));
            int remainingEntries = array.length - (index + 1);
            assertEquals(set.size(), remainingEntries);
            assertEquals(list.size(), remainingEntries);
            assertEquals(map.size(), remainingEntries);
        }
        for (BookmarkedResource br : array) {
            assertFalse(set.contains(br));
            assertFalse(list.contains(br));
            assertFalse(map.containsKey(br));
        }
        assertTrue(set.isEmpty());
        assertTrue(list.isEmpty());
        assertTrue(map.isEmpty());
    }

    @Test
    @Rollback
    public void testDuplicateBookmarks() {
        BookmarkedResource[] bookmarks = createNewUnsavedBookmarkedResources();
        assertEquals(10, bookmarks.length);
        saveAll(bookmarks);
        try {
            saveAll(createNewUnsavedBookmarkedResources());
            fail("Should not be able to save duplicate bookmarked resources.");
        } catch (Exception exception) {
            // expected exception
            logger.debug("{}", exception);
        }
    }

    @Test
    @Rollback
    public void testPersistedCollections() {
        BookmarkedResource[] array = createNewUnsavedBookmarkedResources();
        saveAll(array);
        verifyCollectionOperations(array);
        // these are the unsaved / unmanaged resources..
        // set = new HashSet<BookmarkedResource>(Arrays.asList(array));
        // assertEquals(dataset.getBookmarks().size(), set.size());
        // Dataset mergedDataset = genericService.merge(dataset);
        // Set<BookmarkedResource> persistedDatasetBookmarks = mergedDataset.getBookmarks();
        // assertTrue(set.containsAll(persistedDatasetBookmarks));
        // assertTrue(persistedDatasetBookmarks.containsAll(set));
        // for (BookmarkedResource pbr : persistedDatasetBookmarks) {
        // assertTrue(set.contains(pbr));
        // }
        // for (BookmarkedResource br : set) {
        // logger.debug("Examining: " + br.getId() + ":" + br);
        // assertTrue(persistedDatasetBookmarks.contains(br));
        // }
        // // test existence
        // for (BookmarkedResource br : set) {
        // boolean equal = false;
        // for (BookmarkedResource pbr : persistedDatasetBookmarks) {
        // logger.debug("comparing " + pbr + " with " + br);
        // if (pbr.equals(br) && br.equals(pbr) && (pbr.hashCode() == br.hashCode())) {
        // equal = true;
        // break;
        // }
        // }
        // assertTrue(equal);
        // }
        // // test removal
        // for (BookmarkedResource br : set) {
        // assertTrue(mergedDataset.getBookmarks().remove(br));
        // }
        // assertTrue(persistedDatasetBookmarks.isEmpty());
    }

    @Test
    @Rollback
    public void testBookmarkedResourceService() {
        BookmarkedResource[] array = createNewUnsavedBookmarkedResources();
        saveAll(array);
        Dataset mergedDataset = dataset;
        for (TdarUser person : savedPersons) {
            logger.debug("person:{} ds: {}", person, dataset);
            assertTrue(bookmarkedResourceService.removeBookmark(mergedDataset, person));
        }
        // assertTrue(mergedDataset.getBookmarks().isEmpty());
    }

}
