package org.tdar.core.bean;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionDisplayProperties;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.GenericService;

/**
 * Created by jimdevos on 3/23/15.
 */
public class WhiteLabelCollectionITCase extends AbstractIntegrationTestCase {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    GenericService genericService;

    /**
     * Try to save a new 'white label' collection with all default values;
     */
    @Test
    @Rollback
    public void testSave() {
        setup();
    }

    private Long setup() {
        SharedCollection rc = new SharedCollection();
        rc.setProperties(new CollectionDisplayProperties());
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

        List<SharedCollection> rcs = new ArrayList<>();
        // if configured correctly, hibernate should know to construct sql that includes both ResourceCollection & WhiteLabelCollection objects.
        for (SharedCollection rc : genericService.findAll(SharedCollection.class)) {
            if (rc != null && rc.getProperties() != null && rc.getProperties().isWhitelabel()) {
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
        wlc.getResources().add(document1);
        wlc.getResources().add(document2);
        genericService.saveOrUpdate(wlc);

        Document featuredDocument = genericService.find(Document.class, document2.getId());
        wlc = genericService.find(SharedCollection.class, wlcId);
        assertThat(wlc.getResources().size(), greaterThan(0));
        logger.debug("wlcid:{},  resources:{}", wlcId, wlc.getResources());
        if (wlc.getProperties() == null) {
            wlc.setProperties(new CollectionDisplayProperties());
        }
        wlc.getProperties().setWhitelabel(true);
        wlc.getProperties().getFeaturedResources().add(featuredDocument);
        genericService.save(wlc);
    }

    @SuppressWarnings("unused")
    private SharedCollection createAndSaveWhiteLabelCollection() {
        SharedCollection rc = new SharedCollection();
        rc.setProperties(new CollectionDisplayProperties());
        rc.getProperties().setWhitelabel(true);
        rc.setName("default white label collection");
        rc.markUpdated(getAdminUser());
//        Institution institution = new Institution("Bob's burgers");
//        rc.getProperties().setInstitution(institution);
        genericService.save(rc);
        return rc;
    }
}
