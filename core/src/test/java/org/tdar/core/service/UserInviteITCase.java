package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.core.service.external.auth.UserRegistration;

import com.google.common.base.Objects;

public class UserInviteITCase extends AbstractIntegrationTestCase {

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    @Rollback
    public void testRedmption() {
        Dataset dataset = createAndSaveNewDataset();
        dataset.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), getBasicUser(), Permissions.MODIFY_RECORD));
        Person person = new Person("Penelope", "Davies", "pd@janson.abc");
        genericService.save(dataset);
        genericService.save(person);
        UserInvite invite = new UserInvite();
        invite.setAuthorizer(getBasicUser());
        invite.setDateCreated(new Date());
        invite.setNote("test");
        invite.setResource(dataset);
        invite.setPerson(person);
        invite.setPermissions(Permissions.VIEW_ALL);
        genericService.saveOrUpdate(invite);
        UserRegistration reg = new UserRegistration(new AntiSpamHelper());
        reg.setAcceptTermsOfUse(true);
        reg.setPerson(new TdarUser(person, "janson"));
        reg.setConfirmEmail(person.getEmail());
        reg.setPassword("pass");
        reg.setConfirmPassword("pass");
        authenticationService.addAndAuthenticateUser(reg, new MockHttpServletRequest("POST", "/"), new MockHttpServletResponse(), getSessionData());
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    /**
     * Check that draft and normal rights can be applied properly
     * 
     * @throws Exception
     */
    public void testDraftResourceIssue() throws Exception {
        String email = System.currentTimeMillis() + "a243@basda.com";
        entityService.delete(entityService.findByEmail(email));

        // create a person
        final TdarUser testPerson = createAndSaveNewPerson(email, "1234");
        String name = "test collection";
        String description = "test description";

        // create a collection
        ResourceCollection collection = new ResourceCollection(name, description, getBasicUser());
        collection.markUpdated(getBasicUser());
        collection.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION));
        genericService.saveOrUpdate(collection);
        Long id = collection.getId();
        genericService.synchronize();
        CollectionSaveObject cso = new CollectionSaveObject(collection, getBasicUser(), -1L);
        resourceCollectionService.saveCollectionForController(cso);
        genericService.synchronize();

        ResourceCollection myCollection = genericService.find(ResourceCollection.class, id);

        // grant basic user and admin user rights on the collection
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.ADMINISTER_COLLECTION),
                new AuthorizedUser(getAdminUser(), getAdminUser(), Permissions.MODIFY_RECORD)));

        List<UserRightsProxy> aus = new ArrayList<>();
        for (AuthorizedUser user : users) {
            aus.add(new UserRightsProxy(user));
        }

        // invite our test person too
        UserInvite invite = new UserInvite();
        invite.setPerson(testPerson);
        invite.setPermissions(Permissions.VIEW_ALL);
        aus.add(new UserRightsProxy(invite));

        resourceCollectionService.saveCollectionForRightsController(myCollection, getBasicUser(), aus, -1L);
        myCollection = null;
        genericService.synchronize();
        myCollection = genericService.find(ResourceCollection.class, id);
        logger.debug("au: {}", myCollection.getAuthorizedUsers());
        AuthorizedUser user = null;
        logger.debug("{}", testPerson);
        for (AuthorizedUser au : myCollection.getAuthorizedUsers()) {
            logger.debug("  : {} - {}", au, au.getUser().getId());
            if (Objects.equal(au.getUser().getId(), testPerson.getId())) {
                user = au;
            }
        }
        assertNotNull(user);
        assertEquals(Permissions.VIEW_ALL, user.getGeneralPermission());

    }

}
