package org.tdar.core.service;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.core.service.external.auth.UserRegistration;

public class UserInviteITCase extends AbstractIntegrationTestCase {


    @Autowired
    private AuthenticationService authenticationService;
    
    @Test
    @Rollback
    public void testRedmption() {
        Dataset dataset = createAndSaveNewDataset();
        dataset.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        Person person = new Person("Penelope", "Davies", "pd@janson.abc");
        genericService.save(dataset);
        genericService.save(person);
        UserInvite invite = new UserInvite();
        invite.setAuthorizer(getBasicUser());
        invite.setDateCreated(new Date());
        invite.setNote("test");
        invite.setResource(dataset);
        invite.setPerson(person);
        invite.setPermissions(GeneralPermissions.VIEW_ALL);
        genericService.saveOrUpdate(invite);
        UserRegistration reg = new UserRegistration(new AntiSpamHelper());
        reg.setAcceptTermsOfUse(true);
        reg.setPerson(new TdarUser(person, "janson"));
        reg.setConfirmEmail(person.getEmail());
        reg.setPassword("pass");
        reg.setConfirmPassword("pass");
        authenticationService.addAndAuthenticateUser(reg,  new MockHttpServletRequest("POST", "/"), new MockHttpServletResponse() ,getSessionData());
    }
    
}
