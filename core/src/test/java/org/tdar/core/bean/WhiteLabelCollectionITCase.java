package org.tdar.core.bean;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.WhiteLabelCollection;
import org.tdar.core.bean.entity.Institution;
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
        WhiteLabelCollection rc = new WhiteLabelCollection();
        rc.setName("default white label collection");
        rc.markUpdated(getAdminUser());
        Institution institution = new Institution("Bob's burgers");

        // Note: if you remove @Cascade annotation from WhiteLabelCollection.institution, you must also uncomment the next line.
        // genericService.save(institution);

        rc.setInstitution(institution);
        genericService.save(rc);
        // todo: see if same is true for genericService.saveOrUpdate()
        assertThat(rc.getId(), not(nullValue()));
        assertThat(rc.getId(), not(-1L));
        logger.debug("collection: {}", rc);
    }

    /**
     * Confirm that loading a list of resourceCollection will potentially contain white-label collections
     */
    @Test
    @Rollback
    public void testLoad() {
        testSave();

        List<ResourceCollection> rcs = new ArrayList<>();
        // if configured correctly, hibernate should know to construct sql that includes both ResourceCollection & WhiteLabelCollection objects.
        rcs.addAll(genericService.findAll(ResourceCollection.class));

        // one of these will actually be a WhiteLabelCollection
        WhiteLabelCollection wlc = null;
        int count = 0;
        for (ResourceCollection rc : rcs) {
            if (rc instanceof WhiteLabelCollection) {
                count++;
                wlc = (WhiteLabelCollection) rc;
            }
        }
        assertThat(wlc, not(nullValue()));
        assertThat(count, is(1));
    }

    @Test
    @Rollback
    public void testAddFeaturedRsourceSuccessful() {
        // fixme: stop being so lazy and just write a createWhiteLabelCollection() method.
        testSave();

        WhiteLabelCollection wlc = genericService.findAll(WhiteLabelCollection.class).iterator().next();

        Document document1 = createAndSaveNewResource(Document.class);
        Document document2 = createAndSaveNewResource(Document.class);
        Long wlcId = wlc.getId();

        document2.setTitle("my featured document");
        genericService.saveOrUpdate(document2);
        wlc.getResources().add(document1);
        wlc.getResources().add(document2);
        genericService.saveOrUpdate(wlc);

        Document featuredDocument = genericService.find(Document.class, document2.getId());
        wlc = genericService.find(WhiteLabelCollection.class, wlcId);
        assertThat(wlc.getResources().size(), greaterThan(0));
        logger.debug("wlcid:{},  resources:{}", wlcId, wlc.getResources());
        wlc.getFeaturedResources().add(featuredDocument);
        genericService.save(wlc);
    }

    @SuppressWarnings("unused")
    private WhiteLabelCollection createAndSaveWhiteLabelCollection() {
        WhiteLabelCollection rc = new WhiteLabelCollection();
        rc.setName("default white label collection");
        rc.markUpdated(getAdminUser());
        Institution institution = new Institution("Bob's burgers");
        rc.setInstitution(institution);
        genericService.save(rc);
        return rc;
    }
}
