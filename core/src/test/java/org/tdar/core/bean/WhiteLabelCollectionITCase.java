package org.tdar.core.bean;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionDisplayProperties;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.GenericService;

/**
 * Created by jimdevos on 3/23/15.
 */
public class WhiteLabelCollectionITCase extends AbstractIntegrationTestCase {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    GenericService genericService;
    @Autowired
    ResourceCollectionDao resourceCollectionDao;

    /**
     * Try to save a new 'white label' collection with all default values;
     */
    @Test
    @Rollback
    public void testSave() {
        setup();
    }

    private Long setup() {
        ListCollection rc = new ListCollection();
        rc.setProperties(new CollectionDisplayProperties(false,false,false,false,false,false));
        rc.getProperties().setWhitelabel(true);
        rc.setName("default white label collection");
        rc.markUpdated(getAdminUser());
//        Institution institution = new Institution("Bob's burgers");
        genericService.saveOrUpdate(rc);
        // Note: if you remove @Cascade annotation from WhiteLabelCollection.institution, you must also uncomment the next line.
        // genericService.save(institution);

//        rc.getProperties().setInstitution(institution);
        genericService.save(rc);
        // todo: see if same is true for genericService.saveOrUpdate()
        assertThat(rc.getId(), not(nullValue()));
        assertThat(rc.getId(), not(-1L));
        logger.debug("collection: {}", rc);
        return rc.getId();
    }

    /**
     * Confirm that loading a list of resourceCollection will potentially contain white-label collections
     */
    @Test
    @Rollback
    public void testLoad() {
        Long id = setup();

        List<ListCollection> rcs = new ArrayList<>();
        // if configured correctly, hibernate should know to construct sql that includes both ResourceCollection & WhiteLabelCollection objects.
        for (ListCollection rc : genericService.findAll(ListCollection.class)) {
            if (rc != null && rc.getProperties() != null && rc.getProperties().getWhitelabel()) {
                if (id.equals(rc.getId())) {
                    rcs.add(rc);
                }
            }
        }

        assertEquals("expecting 1 resource collection",1, rcs.size());
    }

    @Test
    @Rollback
    public void testAddFeaturedRsourceSuccessful() {
        // fixme: stop being so lazy and just write a createWhiteLabelCollection() method.
        testSave();

        SharedCollection wlc = genericService.findAll(SharedCollection.class).iterator().next();

        Document document1 = createAndSaveNewResource(Document.class);
        Document document2 = createAndSaveNewResource(Document.class);
        Long wlcId = wlc.getId();

        document2.setTitle("my featured document");
        genericService.saveOrUpdate(document2);
        wlc.getUnmanagedResources().add(document1);
        wlc.getUnmanagedResources().add(document2);
        genericService.saveOrUpdate(wlc);
        document1.getUnmanagedResourceCollections().add(wlc);
        document2.getUnmanagedResourceCollections().add(wlc);
        genericService.saveOrUpdate(document2);
        genericService.saveOrUpdate(document1);
        Long i1 = document1.getId();
        Long i2 = document2.getId();
        document1 = null;
        document2 = null;
        genericService.synchronize();
        Document featuredDocument = genericService.find(Document.class, i2);
        wlc = genericService.find(SharedCollection.class, wlcId);
        logger.debug("wlcid:{},  resources:{}", wlcId, wlc.getUnmanagedResources());
        assertThat(wlc.getUnmanagedResources().size(), greaterThan(0));
        if (wlc.getProperties() == null) {
            wlc.setProperties(new CollectionDisplayProperties(false,false,false,false,false,false));
        }
        wlc.getProperties().setWhitelabel(true);
        wlc.getProperties().getFeaturedResources().add(featuredDocument);
        genericService.save(wlc);
    }

    @SuppressWarnings("unused")
    private ListCollection createAndSaveWhiteLabelCollection() {
        ListCollection rc = new ListCollection();
        rc.setProperties(new CollectionDisplayProperties(false,false,false,false,false,false));
        rc.getProperties().setWhitelabel(true);
        rc.setName("default white label collection");
        rc.markUpdated(getAdminUser());
//        Institution institution = new Institution("Bob's burgers");
//        rc.getProperties().setInstitution(institution);
        genericService.save(rc);
        return rc;
    }
    
    

    @Test
    @Rollback
    public void testConvertToWhitelabelCollection() {
        ListCollection resourceCollection = createAndSaveNewResourceCollection("normal collection", ListCollection.class);
        ListCollection whitelabelCollection = resourceCollectionDao.convertToWhitelabelCollection(resourceCollection);

        assertThat(whitelabelCollection, is(not(nullValue())));
        assertThat(resourceCollection.getId(), is(whitelabelCollection.getId()));
        assertThat(resourceCollection.getTitle(), is(whitelabelCollection.getTitle()));
    }

    @Test
    @Rollback
    public void testWhitelabelsetup() {
        CollectionDisplayProperties props = new CollectionDisplayProperties(false,false,false,false,false,false);
        ListCollection c = new ListCollection();
        c.setName("test");
        c.markUpdated(getAdminUser());
        c.setProperties(props);
        props.setWhitelabel(true);
        genericService.saveOrUpdate(c);
    }

    @Test
    @Rollback
    public void testConvertToResourceCollection() {
        ListCollection wlc = createAndSaveNewWhiteLabelCollection("fancy collection");
        ListCollection rc = resourceCollectionDao.convertToResourceCollection(wlc);

        assertThat(rc, is(not(nullValue())));
        assertThat(rc, hasProperty("title", is("fancy collection")));
    }

}
